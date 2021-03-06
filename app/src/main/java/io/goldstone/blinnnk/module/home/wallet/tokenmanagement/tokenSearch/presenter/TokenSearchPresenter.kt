package io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenSearch.presenter

import android.support.annotation.WorkerThread
import com.blinnnk.extension.getParentFragment
import com.blinnnk.extension.isNotNull
import com.blinnnk.extension.isNull
import com.blinnnk.extension.toArrayList
import com.blinnnk.util.TinyNumber
import io.goldstone.blinnnk.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blinnnk.common.error.RequestError
import io.goldstone.blinnnk.common.sharedpreference.SharedChain
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.thread.launchUI
import io.goldstone.blinnnk.common.utils.NetworkUtil
import io.goldstone.blinnnk.common.utils.safeShowError
import io.goldstone.blinnnk.common.value.Current
import io.goldstone.blinnnk.crypto.multichain.CryptoValue
import io.goldstone.blinnnk.crypto.multichain.TokenContract
import io.goldstone.blinnnk.crypto.multichain.isBTCSeries
import io.goldstone.blinnnk.kernel.commontable.MyTokenTable
import io.goldstone.blinnnk.kernel.network.common.GoldStoneAPI
import io.goldstone.blinnnk.kernel.network.ethereum.ETHJsonRPC
import io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenSearch.view.TokenSearchAdapter
import io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenSearch.view.TokenSearchFragment
import io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenmanagement.view.TokenManagementFragment
import io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable
import io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenmanagementlist.presenter.TokenManagementListPresenter

/**
 * @date 27/03/2018 11:23 AM
 * @author KaySaith
 */
class TokenSearchPresenter(
	override val fragment: TokenSearchFragment
) : BaseRecyclerPresenter<TokenSearchFragment, DefaultTokenTable>() {

	override fun updateData() {
		fragment.asyncData = arrayListOf()
	}

	override fun onFragmentViewCreated() {
		super.onFragmentViewCreated()
		fragment.getParentFragment<TokenManagementFragment> {
			if (SharedWallet.getCurrentWalletType().isBTCSeries())
				showSearchButton(false) {}
			else {
				MyTokenTable.getMyTokens { myTokens ->
					searchInputListener { inputContent ->
						if (NetworkUtil.hasNetwork()) getSearchResult(inputContent, myTokens)
					}
				}
			}
		}
	}

	private fun getSearchResult(searchContent: String, myTokens: List<MyTokenTable>) {
		fragment.showLoadingView(true)
		myTokens.searchTokenByContractOrSymbol(searchContent) { result, error ->
			launchUI {
				if (result.isNotNull() && error.isNone()) {
					if (SharedWallet.getCurrentWalletType().isETHSeries())
					// 如果是以太坊钱包 Only 那么过滤掉比特币系列链的 Coin
						diffAndUpdateSingleCellAdapterData<TokenSearchAdapter>(
							result.filterNot {
								TokenContract(it.contract, it.symbol, it.decimals).isBTCSeries()
							}.toArrayList()
						)
					else diffAndUpdateSingleCellAdapterData<TokenSearchAdapter>(result.toArrayList())
					fragment.showLoadingView(false)
				} else fragment.safeShowError(error)
			}
		}
	}

	fun setMyTokenStatus(searchToken: DefaultTokenTable, isChecked: Boolean, callback: () -> Unit) {
		DefaultTokenTable.getCurrentChainToken(
			TokenContract(searchToken.contract, searchToken.symbol, searchToken.decimals)
		) { localToken ->
			// 通过拉取账单获取的 `Token` 很可能没有名字, 这里在添加的时候顺便更新名字
			if (localToken.isNotNull()) localToken.updateDefaultStatus(
				TokenContract(localToken.contract, localToken.symbol, localToken.decimals),
				isChecked,
				searchToken.name,
				searchToken.iconUrl
			) {
				TokenManagementListPresenter.addOrCloseMyToken(isChecked, localToken)
				callback()
			} else searchToken.apply { isDefault = isChecked } insertThen {
				TokenManagementListPresenter.addOrCloseMyToken(isChecked, searchToken)
				callback()
			}
		}
	}

	private fun List<MyTokenTable>.searchTokenByContractOrSymbol(
		content: String,
		@WorkerThread hold: (data: List<DefaultTokenTable>?, error: RequestError) -> Unit
	) {
		val isSearchingSymbol = content.length != CryptoValue.contractAddressLength
		GoldStoneAPI.getTokenInfoBySymbol(content, Current.supportChainIDs()) { result, error ->
			if (!result.isNullOrEmpty() && error.isNone()) {
				// 从服务器请求目标结果
				hold(
					result.map { serverToken ->
						// 更新使用中的按钮状态
						DefaultTokenTable(serverToken).apply {
							val status = any {
								it.contract.equals(serverToken.contract, true) &&
									it.symbol.equals(serverToken.symbol, true)
							}
							isDefault = status
							isUsed = status
						}
					},
					RequestError.None
				)
			} else {
				if (isSearchingSymbol) hold(arrayListOf(), error)
				// 如果服务器没有结果返回, 那么确认是否是 `ContractAddress` 搜索, 如果是就从 `ethereum` 搜索结果
				// 判断搜索出来的 `Token` 是否是正在使用的 `Token`
				else searchERCTokenByContractFromChain(content, this, hold)
			}
		}
	}

	private fun searchERCTokenByContractFromChain(
		contract: String,
		myTokens: List<MyTokenTable>,
		hold: (data: List<DefaultTokenTable>?, error: RequestError) -> Unit
	) {
		ETHJsonRPC.getTokenInfoByContractAddress(
			contract,
			SharedChain.getCurrentETH()
		) { symbol, name, decimal, error ->
			if (symbol.isNullOrEmpty() || name.isNullOrEmpty() || decimal.isNull() || error.hasError())
				hold(arrayListOf(), RequestError.NullResponse("empty symbol and name"))
			else {
				val status = myTokens.any {
					it.contract.equals(contract, true)
				}
				hold(
					listOf(
						DefaultTokenTable(
							"",
							contract,
							"",
							symbol,
							TinyNumber.False.value,
							0.0,
							name,
							decimal,
							null,
							status,
							0,
							SharedChain.getCurrentETH().chainID.id,
							isUsed = status
						)
					),
					RequestError.None
				)
			}
		}
	}
}