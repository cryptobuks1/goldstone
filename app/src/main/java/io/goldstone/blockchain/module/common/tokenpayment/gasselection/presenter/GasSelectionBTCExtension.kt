package io.goldstone.blockchain.module.common.tokenpayment.gasselection.presenter

import android.widget.LinearLayout
import com.blinnnk.extension.getParentFragment
import com.blinnnk.extension.orElse
import io.goldstone.blockchain.common.utils.alert
import io.goldstone.blockchain.common.value.CommonText
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.crypto.bitcoin.BTCTransactionUtils
import io.goldstone.blockchain.crypto.utils.toSatoshi
import io.goldstone.blockchain.crypto.verifyKeystorePassword
import io.goldstone.blockchain.kernel.commonmodel.BitcoinTransactionTable
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import io.goldstone.blockchain.kernel.network.GoldStoneAPI
import io.goldstone.blockchain.kernel.network.bitcoin.BTCJsonRPC
import io.goldstone.blockchain.kernel.network.bitcoin.BitcoinApi
import io.goldstone.blockchain.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blockchain.module.common.tokenpayment.gasselection.model.GasSelectionModel
import io.goldstone.blockchain.module.common.tokenpayment.gasselection.model.MinerFeeType
import io.goldstone.blockchain.module.common.tokenpayment.gasselection.view.GasSelectionCell
import io.goldstone.blockchain.module.common.tokenpayment.gasselection.view.GasSelectionFooter
import io.goldstone.blockchain.module.common.tokenpayment.paymentprepare.model.PaymentPrepareBTCModel
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blockchain.module.home.wallet.transactions.transactiondetail.model.ReceiptModel
import org.jetbrains.anko.runOnUiThread
import java.math.BigInteger

/**
 * @date 2018/7/25 9:38 PM
 * @author KaySaith
 */
fun GasSelectionPresenter.updateBTCGasSettings(container: LinearLayout) {
	defaultSatoshiValue.forEachIndexed { index, minner ->
		container.findViewById<GasSelectionCell>(index)?.let { cell ->
			cell.model = GasSelectionModel(
				index,
				minner.toString().toLong(),
				226,
				currentMinerType
			)
		}
	}
}

fun GasSelectionPresenter.insertCustomBTCSatoshi() {
	val gasPrice =
		BigInteger.valueOf(gasFeeFromCustom()?.gasPrice.orElse(0))
	currentMinerType = MinerFeeType.Custom.content
	if (defaultSatoshiValue.size == 4) {
		defaultSatoshiValue.remove(defaultSatoshiValue.last())
	}
	defaultSatoshiValue.add(gasPrice)
	fragment.clearGasLayout()
	generateGasSelections(fragment.getGasLayout())
}

fun GasSelectionPresenter.transferBTC(
	password: String,
	callback: () -> Unit
) {
	fragment.context?.verifyKeystorePassword(password) {
		if (!it) {
			fragment.showMaskView(false)
			fragment.context.alert(CommonText.wrongPassword)
			callback()
		} else {
			prepareBTCModel?.apply model@{
				WalletTable.getBTCPrivateKey(
					fromAddress,
					Config.isTestEnvironment()
				) { secret ->
					val fee = gasUsedGasFee?.toSatoshi()!!
					BitcoinApi.getUnspentListByAddress(fromAddress) { unspents ->
						BTCTransactionUtils.generateSignedRawTransaction(
							value,
							fee,
							toAddress,
							changeAddress,
							unspents,
							secret,
							Config.isTestEnvironment()
						).let { signedModel ->
							BTCJsonRPC.sendRawTransaction(
								Config.isTestEnvironment(),
								signedModel.signedMessage
							) { hash ->
								hash?.let {
									// 插入 `Pending` 数据到本地数据库
									insertBTCPendingDataDatabase(this, fee, signedModel.messageSize, it)
									// 跳转到章党详情界面
									GoldStoneAPI.context.runOnUiThread {
										goToTransactionDetailFragment(
											prepareReceiptModelFromBTC(this@model, fee, it)
										)
										callback()
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

private fun GasSelectionPresenter.prepareReceiptModelFromBTC(
	raw: PaymentPrepareBTCModel,
	fee: Long,
	taxHash: String
): ReceiptModel {
	return ReceiptModel(
		raw.fromAddress,
		raw.toAddress,
		fee.toString(),
		raw.value.toBigInteger(),
		getToken()!!,
		taxHash,
		System.currentTimeMillis(),
		prepareModel?.memo
	)
}

fun GasSelectionPresenter.prepareToTransferBTC(
	footer: GasSelectionFooter,
	callback: () -> Unit
) {
	// 检查余额状况
	checkBTCBalanceIsValid(gasUsedGasFee!!) {
		if (!this) {
			footer.setCanUseStyle(false)
			fragment.context.alert("Your BTC balance is not enough for this transaction")
			fragment.showMaskView(false)
			callback()
			return@checkBTCBalanceIsValid
		} else {
			GoldStoneAPI.context.runOnUiThread {
				showConfirmAttentionView(footer, callback)
			}
		}
	}
}

fun GasSelectionPresenter.checkBTCBalanceIsValid(fee: Double, hold: Boolean.() -> Unit) {
	prepareBTCModel?.apply {
		BitcoinApi.getBalanceByAddress(fromAddress) {
			GoldStoneAPI.context.runOnUiThread {
				hold(it > value + fee.toSatoshi())
			}
		}
	}
}

private fun GasSelectionPresenter.insertBTCPendingDataDatabase(
	raw: PaymentPrepareBTCModel,
	fee: Long,
	size: Int,
	taxHash: String
) {
	fragment.getParentFragment<TokenDetailOverlayFragment> {
		val myAddress =
			if (Config.isTestEnvironment())
				Config.getCurrentBTCTestAddress()
			else Config.getCurrentBTCAddress()
		BitcoinTransactionTable(
			0,
			"Waiting",
			0,
			System.currentTimeMillis().toString(),
			taxHash,
			myAddress,
			raw.toAddress,
			myAddress,
			raw.value.toString(),
			fee.toString(),
			size.toString(),
			true
		).apply {
			GoldStoneDataBase.database
				.bitcoinTransactionDao()
				.insert(this)
		}
	}
}