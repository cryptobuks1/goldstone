package io.goldstone.blinnnk.module.common.tokendetail.eosresourcetrading.common.basetradingfragment.presenter

import android.content.Context
import android.support.annotation.WorkerThread
import com.blinnnk.extension.*
import com.blinnnk.util.getParentFragment
import io.goldstone.blinnnk.common.base.basefragment.BasePresenter
import io.goldstone.blinnnk.common.component.overlay.GoldStoneDialog
import io.goldstone.blinnnk.common.component.overlay.LoadingView
import io.goldstone.blinnnk.common.error.AccountError
import io.goldstone.blinnnk.common.error.GoldStoneError
import io.goldstone.blinnnk.common.error.TransferError
import io.goldstone.blinnnk.common.sharedpreference.SharedAddress
import io.goldstone.blinnnk.common.sharedpreference.SharedChain
import io.goldstone.blinnnk.common.sharedpreference.SharedValue
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.thread.launchUI
import io.goldstone.blinnnk.common.utils.safeShowError
import io.goldstone.blinnnk.crypto.eos.account.EOSAccount
import io.goldstone.blinnnk.crypto.eos.account.EOSPrivateKey
import io.goldstone.blinnnk.crypto.eos.base.EOSResponse
import io.goldstone.blinnnk.crypto.eos.transaction.EOSAuthorization
import io.goldstone.blinnnk.crypto.eos.transaction.ExpirationType
import io.goldstone.blinnnk.crypto.multichain.ChainType
import io.goldstone.blinnnk.crypto.multichain.CoinSymbol
import io.goldstone.blinnnk.crypto.multichain.CryptoValue
import io.goldstone.blinnnk.crypto.multichain.TokenContract
import io.goldstone.blinnnk.crypto.utils.formatCount
import io.goldstone.blinnnk.crypto.utils.toEOSCount
import io.goldstone.blinnnk.crypto.utils.toEOSUnit
import io.goldstone.blinnnk.kernel.network.eos.EOSAPI
import io.goldstone.blinnnk.kernel.network.eos.EOSBandWidthTransaction
import io.goldstone.blinnnk.kernel.network.eos.eosram.EOSBuyRamTransaction
import io.goldstone.blinnnk.kernel.network.eos.eosram.EOSSellRamTransaction
import io.goldstone.blinnnk.module.common.tokendetail.eosactivation.accountselection.model.EOSAccountTable
import io.goldstone.blinnnk.module.common.tokendetail.eosresourcetrading.common.basetradingfragment.view.BaseTradingFragment
import io.goldstone.blinnnk.module.common.tokendetail.eosresourcetrading.common.basetradingfragment.view.StakeType
import io.goldstone.blinnnk.module.common.tokendetail.eosresourcetrading.common.basetradingfragment.view.TradingType
import io.goldstone.blinnnk.module.common.tokendetail.tokendetailoverlay.presenter.TokenDetailOverlayPresenter
import io.goldstone.blinnnk.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blinnnk.module.common.tokenpayment.paymentdetail.presenter.PaymentDetailPresenter
import io.goldstone.blinnnk.module.common.tokenpayment.paymentdetail.presenter.PrivatekeyActionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigInteger

/**
 * @author KaySaith
 * @date  2018/09/18
 */
open class BaseTradingPresenter(
	override val fragment: BaseTradingFragment
) : BasePresenter<BaseTradingFragment>() {

	open fun gainConfirmEvent(
		cancelAction: () -> Unit,
		callback: (response: EOSResponse?, error: GoldStoneError) -> Unit
	) {
		showMnemonicBackUpDialogOrElse { hasBackUp ->
			when {
				!hasBackUp -> {
					callback(null, AccountError.BackUpMnemonic)
				}
				fragment.tradingType == TradingType.RAM -> with(fragment) {
					buyRam(
						context!!,
						getInputValue(StakeType.BuyRam).first,
						getInputValue(StakeType.BuyRam).second,
						cancelAction,
						callback
					)
				}
				else -> with(fragment) {
					stakeResource(
						context!!,
						getInputValue(StakeType.Delegate).first,
						getInputValue(StakeType.Delegate).second,
						tradingType,
						StakeType.Delegate,
						isTransfer(StakeType.Delegate),
						cancelAction,
						callback
					)
				}
			}
		}
	}

	open fun refundOrSellConfirmEvent(
		cancelAction: () -> Unit,
		callback: (response: EOSResponse?, error: GoldStoneError) -> Unit
	) {
		showMnemonicBackUpDialogOrElse { hasBackUp ->
			when {
				!hasBackUp -> callback(null, AccountError.BackUpMnemonic)
				fragment.tradingType.isRAM() -> with(fragment) {
					val sellAmount = getInputValue(StakeType.SellRam).second
					if (!sellAmount.toString().substringBefore(".").toLongOrNull().hasValue()) {
						callback(null, TransferError.InvalidRAMNumber)
					} else sellRAM(
						context!!,
						sellAmount.toLong(),
						cancelAction,
						callback
					)
				}
				else -> with(fragment) {
					stakeResource(
						context!!,
						getInputValue(StakeType.Refund).first,
						getInputValue(StakeType.Refund).second,
						tradingType,
						if (tradingType.isCPU()) StakeType.RefundCPU else StakeType.RefundNET,
						isTransfer(StakeType.Refund),
						cancelAction,
						callback
					)
				}
			}
		}
	}

	override fun onFragmentViewCreated() {
		super.onFragmentViewCreated()
		GlobalScope.launch(Dispatchers.Default) {
			fragment.setUsageValue()
		}
	}

	@WorkerThread
	private fun BaseTradingFragment.setUsageValue() {
		val account =
			EOSAccountTable.dao.getAccount(
				SharedAddress.getCurrentEOSAccount().name,
				SharedChain.getEOSCurrent().chainID.id
			)
		launchUI {
			when (tradingType) {
				TradingType.CPU -> {
					val cpuEOSValue = "${account?.cpuWeight?.toEOSCount()}" suffix CoinSymbol.eos
					val availableCPU = account?.cpuLimit?.max.orZero() - account?.cpuLimit?.used.orZero()
					setProcessUsage(cpuEOSValue, availableCPU, account?.cpuLimit?.max.orZero(), SharedValue.getCPUUnitPrice())
				}
				TradingType.NET -> {
					val netEOSValue = "${account?.netWeight?.toEOSCount()}" suffix CoinSymbol.eos
					val availableNET = account?.netLimit?.max.orZero() - account?.netLimit?.used.orZero()
					setProcessUsage(netEOSValue, availableNET, account?.netLimit?.max.orZero(), SharedValue.getNETUnitPrice())
				}
				TradingType.RAM -> {
					val loadingView = LoadingView(fragment.context!!)
					loadingView.show()
					val availableRAM = account?.ramQuota.orZero() - account?.ramUsed.orZero()
					// 因为这里只需显示大概价格, 并且这里需要用到两次, 所以直接取用了 `EOS` 个数买 `KB`` 并反推 `Price` 的方法减少网络请求
					val price = SharedValue.getRAMUnitPrice()
					val amountKBInEOS = 1.0 / price
					val ramEOSAccount = "≈ " + (availableRAM.toDouble() * price / 1024).formatCount(4) suffix CoinSymbol.eos
					setProcessUsage(ramEOSAccount, availableRAM, account?.ramQuota.orZero(), amountKBInEOS)
					loadingView.remove()
				}
			}
		}
	}

	fun updateLocalDataAndUI() {
		val currentAccount = SharedAddress.getCurrentEOSAccount()
		EOSAPI.getAccountInfo(currentAccount) { newData, error ->
			if (newData.isNotNull() && error.isNone()) {
				// 新数据标记为老数据的 `主键` 值
				EOSAccountTable.dao.update(newData)
				fragment.setUsageValue()
			} else fragment.safeShowError(error)
		}
	}

	private fun showMnemonicBackUpDialogOrElse(callback: (hasBackUp: Boolean) -> Unit) {
		if (!SharedWallet.hasBackUpMnemonic()) {
			GoldStoneDialog(fragment.context!!).showBackUpMnemonicStatus {
				fragment.getParentFragment<TokenDetailOverlayFragment>()?.apply {
					TokenDetailOverlayPresenter.showMnemonicBackupFragment(this)
				}
			}
			callback(false)
		} else callback(true)
	}

	companion object {

		fun stakeResource(
			context: Context,
			toAccount: EOSAccount,
			transferCount: Double,
			tradingType: TradingType,
			stakeType: StakeType,
			isTransfer: Boolean,
			cancelAction: () -> Unit,
			@WorkerThread callback: (response: EOSResponse?, error: GoldStoneError) -> Unit
		) {
			val fromAccount = SharedAddress.getCurrentEOSAccount()
			if (toAccount.isValid(false)) {
				prepareTransaction(
					context,
					transferCount,
					TokenContract.EOS,
					stakeType,
					fromAccount.isSame(toAccount),
					cancelAction
				) { privateKey, error ->
					if (error.hasError()) {
						callback(null, error)
					} else {
						val chainID = SharedChain.getEOSCurrent().chainID
						val permission = EOSAccountTable.getValidPermission(fromAccount, chainID)
						when {
							permission.isNull() -> callback(null, TransferError.WrongPermission)
							error.isNone() && privateKey.isNotNull() -> EOSBandWidthTransaction(
								chainID,
								EOSAuthorization(fromAccount.name, permission),
								toAccount.name,
								transferCount.toEOSUnit(),
								tradingType,
								stakeType,
								isTransfer,
								ExpirationType.FiveMinutes
							).send(
								EOSPrivateKey(privateKey),
								SharedChain.getEOSCurrent().getURL(),
								callback
							)
							else -> callback(null, error)
						}
					}
				}
			} else callback(null, AccountError.InvalidAccountName)
		}

		fun sellRAM(
			context: Context,
			tradingCount: Long,
			cancelAction: () -> Unit,
			@WorkerThread callback: (response: EOSResponse?, GoldStoneError) -> Unit
		) {
			val fromAccount = SharedAddress.getCurrentEOSAccount()
			val chainID = SharedChain.getEOSCurrent().chainID
			prepareTransaction(
				context,
				tradingCount,
				TokenContract.EOS,
				StakeType.SellRam,
				cancelAction = cancelAction
			) { privateKey, error ->
				if (error.hasError()) {
					callback(null, error)
				} else {
					val permission = EOSAccountTable.getValidPermission(fromAccount, chainID)
					when {
						permission.isNull() -> callback(null, TransferError.WrongPermission)
						error.isNone() && privateKey.isNotNull() -> EOSSellRamTransaction(
							chainID,
							EOSAuthorization(fromAccount.name, permission),
							BigInteger.valueOf(tradingCount),
							ExpirationType.FiveMinutes
						).send(
							EOSPrivateKey(privateKey),
							SharedChain.getEOSCurrent().getURL(),
							callback
						)
						else -> callback(null, error)
					}
				}
			}
		}

		fun buyRam(
			context: Context,
			toAccount: EOSAccount,
			tradingCount: Double,
			cancelAction: () -> Unit,
			@WorkerThread callback: (response: EOSResponse?, GoldStoneError) -> Unit
		) {
			val fromAccount = SharedAddress.getCurrentEOSAccount()
			val chainID = SharedChain.getEOSCurrent().chainID
			// 检查接收内存的用户名是否正常如果不正常就返回
			if (toAccount.isValid(false)) prepareTransaction(
				context,
				tradingCount,
				TokenContract.EOS,
				StakeType.BuyRam,
				cancelAction = cancelAction
			) { privateKey, error ->
				if (error.hasError()) {
					callback(null, error)
				} else {
					val permission = EOSAccountTable.getValidPermission(fromAccount, chainID)
					when {
						permission.isNull() -> callback(null, TransferError.WrongPermission)
						error.isNone() && privateKey.isNotNull() -> EOSBuyRamTransaction(
							chainID,
							EOSAuthorization(fromAccount.name, permission),
							toAccount.name,
							tradingCount.toEOSUnit(),
							ExpirationType.FiveMinutes
						).send(
							EOSPrivateKey(privateKey),
							SharedChain.getEOSCurrent().getURL(),
							callback
						)
						else -> callback(null, error)
					}
				}
			} else callback(null, AccountError.InvalidAccountName)
		}

		fun <T : Number> prepareTransaction(
			context: Context,
			tradingCount: T,
			contract: TokenContract,
			type: StakeType,
			isMySelf: Boolean = true,
			cancelAction: () -> Unit,
			@WorkerThread hold: (privateKey: String?, error: GoldStoneError) -> Unit
		) {
			val fromAccount = SharedAddress.getCurrentEOSAccount()
			val chain = SharedChain.getEOSCurrent().chainID
			// 检出用户的输入值是否合规
			isValidInputValue(tradingCount, type.isSellRam()) { error ->
				if (error.hasError()) GlobalScope.launch(Dispatchers.Default) {
					hold(null, error)
				} else when {
					type.isSellRam() -> EOSAPI.getAvailableRamBytes(fromAccount) { ramAvailable, ramError ->
						// 检查发起账户的 `RAM` 余额是否足够
						when {
							!ramError.isNone() -> hold(null, ramError)
							ramAvailable.isNull() -> hold(null, ramError)
							ramAvailable < BigInteger.valueOf(tradingCount.toLong()) ->
								hold(null, TransferError.BalanceIsNotEnough)
							tradingCount == 1.0 -> hold(null, TransferError.SellRAMTooLess)
							else -> PaymentDetailPresenter.getPrivatekey(
								context,
								ChainType.EOS,
								PrivatekeyActionType.Transfer,
								cancelEvent = cancelAction,
								hold = hold
							)
						}
					}
					// 检查代理的资源的余额状态
					type.isRefund() -> GlobalScope.launch(Dispatchers.Default) {
						val accountInfo =
							EOSAccountTable.dao.getAccount(fromAccount.name, chain.id)
						val selfDelegate =
							accountInfo?.totalDelegateBandInfo?.filter {
								it.fromName.equals(fromAccount.name, true)
							}
						val count = if (type.isRefundCPU()) selfDelegate?.sumByDouble {
							it.cpuWeight.substringBefore(" ").toDoubleOrZero()
						} ?: 0.0 else selfDelegate?.sumByDouble {
							it.netWeight.substringBefore(" ").toDoubleOrZero()
						} ?: 0.0
						// 如果不是 `Refund` 自己的, 那么直接进入私钥获取面板, 不再做本地检查,
						// 交由 `EOS` 链处理
						if ((tradingCount is Double && count >= tradingCount) || isMySelf) {
							PaymentDetailPresenter.getPrivatekey(
								context,
								ChainType.EOS,
								PrivatekeyActionType.Transfer,
								hold = hold
							)
						} else hold(null, TransferError.RefundMoreThenExisted)
					}
					else -> EOSAPI.getAccountBalanceBySymbol(
						fromAccount,
						CoinSymbol(contract.symbol),
						contract.contract
					) { balance, balanceError ->
						if (balance.isNotNull() && balanceError.isNone() && tradingCount is Double) {
							// 检查发起账户的余额是否足够
							if (balance < tradingCount) hold(null, TransferError.BalanceIsNotEnough)
							else PaymentDetailPresenter.getPrivatekey(
								context,
								ChainType.EOS,
								PrivatekeyActionType.Transfer,
								hold = hold
							)
						} else hold(null, balanceError)
					}
				}
			}
		}

		private fun <T : Number> isValidInputValue(
			tradingNumber: T,
			isSellRam: Boolean,
			callback: (GoldStoneError) -> Unit
		) {
			when {
				tradingNumber == 0 || tradingNumber == 0.0 -> {
					callback(TransferError.TradingInputIsEmpty)
				}
				isSellRam && tradingNumber !is Long -> // 检查输入的卖出的 `EOS` 的值是否正确
					callback(TransferError.WrongRAMInputValue)
				tradingNumber.toDouble().getDecimalCount().orZero() > CryptoValue.eosDecimal -> // 检查输入值的精度是否正确
					callback(TransferError.IncorrectDecimal)
				else -> callback(GoldStoneError.None)
			}
		}
	}
}