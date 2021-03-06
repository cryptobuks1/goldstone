package io.goldstone.blinnnk.module.home.quotation.markettokendetail.view

import android.content.Context
import android.view.Gravity
import android.widget.RelativeLayout
import com.blinnnk.extension.alignParentBottom
import com.blinnnk.extension.into
import com.blinnnk.extension.safeGet
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blinnnk.common.component.cell.GraySquareCell
import io.goldstone.blinnnk.common.component.cell.TopBottomLineCell
import io.goldstone.blinnnk.common.language.DateAndTimeText
import io.goldstone.blinnnk.common.language.QuotationText
import io.goldstone.blinnnk.common.value.PaddingSize
import io.goldstone.blinnnk.module.home.quotation.quotationsearch.model.QuotationSelectionTable
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.verticalLayout
import org.json.JSONObject

/**
 * @date 25/04/2018 9:04 AM
 * @author KaySaith
 */
data class PriceHistoryModel(
	val dayHighest: String,
	val dayLow: String,
	val totalHighest: String,
	val totalLow: String,
	var baseSymbol: String
) {

	constructor(
		data: JSONObject,
		symbol: String
	) : this(
		data.safeGet("high_24"),
		data.safeGet("low_24"),
		data.safeGet("high_total"),
		data.safeGet("low_total"),
		symbol
	)

	constructor(
		data: QuotationSelectionTable,
		symbol: String
	) : this(
		data.high24,
		data.low24,
		data.highTotal,
		data.lowTotal,
		symbol
	)
}

class PriceHistoryView(context: Context) : TopBottomLineCell(context) {

	var model: PriceHistoryModel? by observing(null) {
		model?.apply {
			dayPrice.setPriceSubtitle("$dayHighest / $dayLow", baseSymbol)
			totalPrice.setPriceSubtitle("$totalHighest / $totalLow", baseSymbol)
		}
	}
	private val dayPrice = GraySquareCell(context)
	private val totalPrice = GraySquareCell(context)

	init {
		setTitle(QuotationText.priceHistory)
		layoutParams = RelativeLayout.LayoutParams(matchParent, 150.uiPX())
		setHorizontalPadding(PaddingSize.content.toFloat())
		dayPrice.setPriceTitle(DateAndTimeText.hours)
		totalPrice.setPriceTitle(DateAndTimeText.total)

		verticalLayout {
			lparams(matchParent, matchParent)
			gravity = Gravity.CENTER_HORIZONTAL
			dayPrice.into(this)
			totalPrice.into(this)
		}.alignParentBottom()
	}
}