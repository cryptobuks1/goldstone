package io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.presenter

import com.blinnnk.component.HoneyBaseSwitch
import com.blinnnk.extension.getParentFragment
import com.blinnnk.extension.isNull
import com.blinnnk.extension.orEmptyArray
import com.blinnnk.extension.toArrayList
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.utils.ConcurrentAsyncCombine
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.value.WalletType
import io.goldstone.blockchain.crypto.multichain.CoinSymbol
import io.goldstone.blockchain.crypto.utils.MultiChainUtils
import io.goldstone.blockchain.crypto.utils.getObjectMD5HexString
import io.goldstone.blockchain.kernel.commonmodel.MyTokenTable
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagement.view.TokenManagementFragment
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.view.TokenManagementListAdapter
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.view.TokenManagementListFragment

/**
 * @date 25/03/2018 5:11 PM
 * @author KaySaith
 */
var memoryTokenData: ArrayList<DefaultTokenTable>? = null

class TokenManagementListPresenter(
	override val fragment: TokenManagementListFragment
) : BaseRecyclerPresenter<TokenManagementListFragment, DefaultTokenTable>() {

	override fun updateData() {
		checkWalletType {
			// 首先显示内存中的数据
			if (fragment.asyncData.isNull()) fragment.asyncData = memoryTokenData.orEmptyArray()
			// 从异步更新数据在决定是否更新 `UI` 及内存中的数据
			fragment.getParentFragment<TokenManagementFragment> {
				prepareMyDefaultTokens(it)
			}
		}
	}

	override fun onFragmentShowFromHidden() {
		super.onFragmentShowFromHidden()
		updateData()
	}

	private fun checkWalletType(callback: (isETHERCAndETCWallet: Boolean) -> Unit) {
		when (Config.getCurrentWalletType()) {
			WalletType.BTCTestOnly.content,
			WalletType.BTCOnly.content,
			WalletType.LTCOnly.content,
			WalletType.BCHOnly.content -> {
				fragment.showAttentionView()
				callback(false)
			}

			WalletType.ETHERCAndETCOnly.content -> callback(true)
			else -> callback(false)
		}
	}

	private fun prepareMyDefaultTokens(isETHERCAndETCOnly: Boolean) {
		// 在异步线程更新数据
		DefaultTokenTable.getDefaultTokens { defaultTokens ->
			object : ConcurrentAsyncCombine() {
				override var asyncCount: Int = defaultTokens.size

				override fun concurrentJobs() {
					defaultTokens.forEach { default ->
						MyTokenTable.getMyTokens { myTokens ->
							default.isUsed = !myTokens.find {
								default.contract.equals(it.contract, true)
							}.isNull()
							completeMark()
						}
					}
				}

				override fun mergeCallBack() {
					val sortedList =
						defaultTokens.sortedByDescending { it.weight }.toArrayList()
					if (memoryTokenData?.getObjectMD5HexString() != sortedList.getObjectMD5HexString()) {
						if (isETHERCAndETCOnly) {
							sortedList.filterNot {
								CoinSymbol(it.symbol).isBTCSeries()
							}.let {
								memoryTokenData = it.toArrayList()
							}
						} else {
							memoryTokenData = sortedList
						}
						diffAndUpdateSingleCellAdapterData<TokenManagementListAdapter>(memoryTokenData.orEmptyArray())
					} else {
						return
					}
				}
			}.start()
		}
	}

	companion object {

		fun updateMyTokensInfoBy(
			switch: HoneyBaseSwitch,
			token: DefaultTokenTable
		) {
			switch.isClickable = false
			if (switch.isChecked) {
				// once it is checked then insert this symbol into `MyTokenTable` database
				MyTokenTable.insertBySymbolAndContract(token.symbol, token.contract) {
					switch.isClickable = true
				}
			} else {
				// once it is unchecked then delete this symbol from `MyTokenTable` database
				MyTokenTable.deleteByContract(
					token.contract,
					MultiChainUtils.getAddressBySymbol(token.symbol)
				) {
					switch.isClickable = true
				}
			}
		}
	}
}