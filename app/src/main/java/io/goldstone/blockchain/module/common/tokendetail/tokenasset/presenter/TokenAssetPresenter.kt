package io.goldstone.blockchain.module.common.tokendetail.tokenasset.presenter

import com.blinnnk.extension.isNull
import com.blinnnk.util.getParentFragment
import io.goldstone.blockchain.common.base.basefragment.BasePresenter
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.language.TokenDetailText
import io.goldstone.blockchain.common.utils.LogUtil
import io.goldstone.blockchain.common.utils.getGrandFather
import io.goldstone.blockchain.common.utils.suffix
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.crypto.multichain.CoinSymbol
import io.goldstone.blockchain.crypto.utils.toEOSCount
import io.goldstone.blockchain.kernel.commonmodel.eos.EOSTransactionTable
import io.goldstone.blockchain.kernel.network.GoldStoneAPI
import io.goldstone.blockchain.kernel.network.eos.EOSAPI
import io.goldstone.blockchain.module.common.tokendetail.eosactivation.accountselection.model.EOSAccountTable
import io.goldstone.blockchain.module.common.tokendetail.tokenasset.view.TokenAssetFragment
import io.goldstone.blockchain.module.common.tokendetail.tokendetailcenter.view.TokenDetailCenterFragment
import io.goldstone.blockchain.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blockchain.module.common.tokendetail.tokeninfo.presenter.TokenInfoPresenter
import io.goldstone.blockchain.module.home.wallet.walletsettings.qrcodefragment.presenter.QRCodePresenter
import org.jetbrains.anko.runOnUiThread


/**
 * @author KaySaith
 * @date  2018/09/10
 */
class TokenAssetPresenter(
	override val fragment: TokenAssetFragment
) : BasePresenter<TokenAssetFragment>() {
	private val tokenInfo by lazy {
		fragment.getParentFragment<TokenDetailCenterFragment>()?.token
	}

	private val currentAddress by lazy {
		CoinSymbol(tokenInfo?.symbol).getAddress()
	}

	override fun onFragmentViewCreated() {
		super.onFragmentViewCreated()
		checkAndSetAccountValue()
		getAccountTransactionCount()
		val info = TokenInfoPresenter.getDetailButtonInfo(tokenInfo, currentAddress)
		val code = QRCodePresenter.generateQRCode(currentAddress)
		val chainName = CoinSymbol.eos suffix TokenDetailText.chainType
		fragment.setTokenInfo(code, chainName, CommonText.calculating, info.first) {
			TokenInfoPresenter.showThirdPartyAddressDetail(
				fragment.getGrandFather<TokenDetailOverlayFragment>(),
				info.second
			)
		}
	}

	private fun getAccountTransactionCount() {
		// 先查数据库获取交易从数量, 如果数据库数据是空的那么从网络查询转账总个数
		val accountName = Config.getCurrentEOSName()
		EOSTransactionTable.getTransactionByAccountName(
			accountName,
			Config.getEOSCurrentChain()
		) { localData ->
			if (localData.isEmpty()) {
				EOSAPI.getTransactionsLastIndex(
					accountName,
					{
						fragment.setTransactionCount(CommonText.calculating)
						LogUtil.error("getTransactionsLastIndex", it)
					}
				) {
					val count = if (it.isNull()) 0 else it!! + 1
					fragment.setTransactionCount(count.toString())
				}
			} else {
				fragment.setTransactionCount(localData.size.toString())
			}
		}
	}

	private fun checkAndSetAccountValue() {
		fun EOSAccountTable.setValue() {
			fragment.setEOSBalance(balance)
			val availableRAM = ramQuota - ramUsed
			val availableCPU = cpuLimit.max - cpuLimit.used
			val cpuEOSValue = "${cpuWeight.toEOSCount()}" suffix CoinSymbol.eos
			val availableNet = netLimit.max - netLimit.used
			val netEOSValue = "${netWeight.toEOSCount()}" suffix CoinSymbol.eos
			fragment.setResourcesValue(
				availableRAM,
				ramQuota,
				availableCPU,
				cpuLimit.max,
				cpuEOSValue,
				availableNet,
				netLimit.max,
				netEOSValue
			)
		}
		EOSAccountTable.getAccountByName(Config.getCurrentEOSName()) { account ->
			if (account.isNull()) EOSAPI.getAccountInfoByName(
				Config.getCurrentEOSName(),
				{
					LogUtil.error("getAccountInfoByName", it)
				}
			) {
				EOSAccountTable.preventDuplicateInsert(it)
				GoldStoneAPI.context.runOnUiThread { it.setValue() }
			} else account?.setValue()
		}
	}


}