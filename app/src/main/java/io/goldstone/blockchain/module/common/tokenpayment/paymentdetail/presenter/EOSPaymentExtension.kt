package io.goldstone.blockchain.module.common.tokenpayment.paymentdetail.presenter

import android.support.annotation.WorkerThread
import com.blinnnk.extension.getDecimalCount
import com.blinnnk.extension.isNotNull
import com.blinnnk.extension.orElse
import io.goldstone.blockchain.common.error.GoldStoneError
import io.goldstone.blockchain.common.error.TransferError
import io.goldstone.blockchain.common.sharedpreference.SharedAddress
import io.goldstone.blockchain.common.thread.launchUI
import io.goldstone.blockchain.crypto.eos.account.EOSAccount
import io.goldstone.blockchain.crypto.eos.transaction.EOSTransactionInfo
import io.goldstone.blockchain.crypto.multichain.CryptoValue
import io.goldstone.blockchain.crypto.multichain.TokenContract
import io.goldstone.blockchain.crypto.utils.toAmount
import io.goldstone.blockchain.module.common.tokenpayment.gasselection.view.GasSelectionFragment
import io.goldstone.blockchain.module.home.wallet.transactions.transactiondetail.model.ReceiptModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * @author KaySaith
 * @date  2018/09/14
 */

// EOS 的 `Token` 转币只需写对 `Token` 的 `Symbol` 就可以转账成功
fun PaymentDetailPresenter.transferEOS(
	count: Double,
	contract: TokenContract,
	@WorkerThread callback: (error: GoldStoneError) -> Unit
) {
	val inputValueDecimal = count.toBigDecimal().toPlainString().substringAfter(".").length
	if (inputValueDecimal != contract.decimal) GlobalScope.launch {
		callback(TransferError.IncorrectDecimal)
	} else EOSTransactionInfo(
		SharedAddress.getCurrentEOSAccount(),
		EOSAccount(fragment.address.orEmpty()),
		count.toAmount(contract.decimal.orElse(CryptoValue.eosDecimal)),
		fragment.getMemoContent(),
		contract
	).apply {
		trade(fragment.context) { response, error ->
			if (error.isNone() && response.isNotNull())
				insertPendingDataToDatabase(response) {
					launchUI {
						getToken()?.let {
							val receiptModel = ReceiptModel(this, response, it)
							GasSelectionFragment.goToTransactionDetailFragment(rootFragment, fragment, receiptModel)
						}
					}
				}
			else callback(error)
		}
	}
}

