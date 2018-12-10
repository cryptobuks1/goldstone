package io.goldstone.blockchain.module.home.dapp.common

import android.view.ViewGroup
import android.widget.LinearLayout
import com.blinnnk.extension.getTargetObject
import com.blinnnk.extension.safeGet
import io.goldstone.blockchain.common.component.cell.graySquareCell
import io.goldstone.blockchain.common.component.overlay.Dashboard
import io.goldstone.blockchain.common.error.GoldStoneError
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.language.TransactionText
import io.goldstone.blockchain.common.value.PaddingSize
import io.goldstone.blockchain.crypto.eos.base.EOSResponse
import io.goldstone.blockchain.crypto.eos.transaction.EOSTransactionInfo
import io.goldstone.blockchain.crypto.multichain.CryptoValue
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.wrapContent
import org.json.JSONObject


/**
 * @author KaySaith
 * @date  2018/12/09
 */
fun ViewGroup.showQuickPaymentDashboard(
	data: JSONObject,
	isTransactionObject: Boolean, // 如果是 `isTransactionObject` 传入的 `JSONObject` 是含有全量信息的
	cancelEvent: () -> Unit,
	callback: (EOSResponse?, GoldStoneError) -> Unit
) {
	// 如果是转账操作那么 `DAPP` 都会把具体信息放在 `memo` 里面
	// 解除 `memo` 里面的 `count` 计算出当前需要转账的货币的 `Decimal` 作为参数
	val info = if (isTransactionObject) data.getTargetObject("data") else data
	val decimal = CryptoValue.eosDecimal
	val transaction =
		if (isTransactionObject) EOSTransactionInfo(data, decimal)
		else EOSTransactionInfo(data)
	val contentLayout = LinearLayout(context).apply {
		orientation = LinearLayout.VERTICAL
		layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
		padding = PaddingSize.content
		graySquareCell {
			layoutParams.width = matchParent
			setTitle(CommonText.from)
			setSubtitle(info.safeGet("from"))
		}
		graySquareCell {
			layoutParams.width = matchParent
			setTitle(CommonText.to)
			setSubtitle(info.safeGet("to"))
		}
		graySquareCell {
			layoutParams.width = matchParent
			setTitle("Quantity")
			setSubtitle(info.safeGet("quantity"))
		}
		graySquareCell {
			layoutParams.width = matchParent
			setTitle(TransactionText.memo)
			setSubtitle(info.safeGet("memo"))
		}
	}
	Dashboard(context) {
		showDashboard(
			"Quick Payment",
			contentLayout,
			"confirmation the transaction from current DAPP, then sign the data",
			hold = {
				transaction.trade(context, callback)
			},
			cancelAction = cancelEvent
		)
	}
}