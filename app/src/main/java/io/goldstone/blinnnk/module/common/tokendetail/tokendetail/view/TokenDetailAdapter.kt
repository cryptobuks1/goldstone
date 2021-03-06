package io.goldstone.blinnnk.module.common.tokendetail.tokendetail.view

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.blinnnk.base.HoneyBaseAdapterWithHeaderAndFooter
import com.blinnnk.extension.preventDuplicateClicks
import com.blinnnk.uikit.uiPX
import io.goldstone.blinnnk.common.base.baserecyclerfragment.BottomLoadingView
import io.goldstone.blinnnk.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.model.TransactionListModel
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * @date 27/03/2018 3:36 PM
 * @author KaySaith
 */
class TokenDetailAdapter(
	override var dataSet: ArrayList<TransactionListModel>,
	private val callback: (TransactionListModel) -> Unit,
	private val holdHeader: TokenDetailHeaderView.() -> Unit,
	private val holdBottom: BottomLoadingView.() -> Unit
) : HoneyBaseAdapterWithHeaderAndFooter<TransactionListModel, TokenDetailHeaderView, TokenDetailCell, BottomLoadingView>() {

	override fun generateCell(context: Context) = TokenDetailCell(context)
	override fun generateFooter(context: Context) = BottomLoadingView(context).apply {
		// 让出 覆盖在上面的 `Footer` 的高度
		setGrayDescription()
		addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(matchParent, 100.uiPX()) })
		holdBottom(this)
	}

	override fun generateHeader(context: Context) = TokenDetailHeaderView(context).apply(holdHeader)

	override fun TokenDetailCell.bindCell(data: TransactionListModel, position: Int) {
		model = data
		onClick {
			callback(data)
			preventDuplicateClicks()
		}
	}
}