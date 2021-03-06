package io.goldstone.blinnnk.module.home.quotation.quotation.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import com.github.mikephil.charting.data.Entry
import io.goldstone.blinnnk.R
import io.goldstone.blinnnk.common.component.GSCard
import io.goldstone.blinnnk.common.component.title.TwoLineTitles
import io.goldstone.blinnnk.common.component.title.twoLineTitles
import io.goldstone.blinnnk.common.utils.GoldStoneFont
import io.goldstone.blinnnk.common.value.*
import io.goldstone.blinnnk.module.home.quotation.quotation.model.QuotationModel
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView

@SuppressLint("SetTextI18n")
/**
 * @date 20/04/2018 8:18 PM
 * @author KaySaith
 */
class QuotationCell(context: Context) : GSCard(context) {

	// 判断标价是否需要刷新价格及 `LineChart` 的标记
	private var previousPrice: Double? = null
	var model: QuotationModel by observing(QuotationModel()) {
		val price = if (model.price == ValueTag.emptyPrice) model.price
		else model.price.toDoubleOrNull()?.toBigDecimal()?.toPlainString()
		tokenInfo.title.text = model.pairDisplay.toUpperCase()
		tokenInfo.subtitle.text = model.name
		tokenPrice.title.text = CustomTargetTextStyle(
			model.quoteSymbol.toUpperCase(),
			model.quoteSymbol.toUpperCase() suffix price.orEmpty(),
			GrayScale.midGray,
			13.uiPX(),
			false,
			false
		)
		tokenPrice.subtitle.text = model.percent + "%"
		lineChart.resetDataWithTargetLabelCount(
			model.chartData.mapIndexed { index, chartPoint ->
				Entry(index.toFloat(), chartPoint.value, chartPoint.label)
			}.toArrayList()
		)
		exchangeName.text = model.exchangeName
		// 如果价格没有变动就不用乡下执行了
		if (price?.toDoubleOrNull().orZero() == previousPrice) return@observing
		when {
			model.isDisconnected -> {
				tokenPrice.setColorStyle(GrayScale.midGray)
				lineChart.setChartColorAndShadowResource(GrayScale.lightGray, R.drawable.fade_gray)
			}

			model.percent.toDouble() < 0 -> {
				tokenPrice.setColorStyle(Spectrum.red)
				lineChart.setChartColorAndShadowResource(Spectrum.red, R.drawable.fade_red)
			}

			else -> {
				tokenPrice.setColorStyle(Spectrum.green)
				lineChart.setChartColorAndShadowResource(Spectrum.green, R.drawable.fade_green)
			}
		}
		previousPrice = price?.toDoubleOrNull().orZero()
	}
	private var tokenInfo: TwoLineTitles
	private var tokenPrice: TwoLineTitles
	private var exchangeName: TextView
	private val lineChart = object : QuotationLineChart(context) {
		override val isDrawPoints: Boolean = false
		override val isPerformBezier: Boolean = true
		override val dragEnable: Boolean = false
		override val touchEnable: Boolean = false
		override val animateEnable: Boolean = false
		override fun lineLabelCount(): Int = 5
	}

	init {
		layoutParams = LinearLayout.LayoutParams(ScreenSize.card, 160.uiPX())
		resetCardElevation(3.uiPX().toFloat())
		tokenInfo = twoLineTitles {
			x += 20.uiPX()
			setBlackTitles()
			setQuotationStyle()
		}
		tokenPrice = twoLineTitles {
			x -= 20.uiPX()
			setQuotationStyle()
			setColorStyle(Spectrum.green)
			isFloatRight = true
		}
		exchangeName = textView {
			textSize = fontSize(12)
			textColor = GrayScale.midGray
			typeface = GoldStoneFont.medium(context)
			gravity = Gravity.END
			layoutParams = RelativeLayout.LayoutParams(matchParent, 20.uiPX())
			x -= 20.uiPX()
			y += 52.uiPX()
		}
		tokenPrice.alignParentRight()
		lineChart.apply {
			id = ElementID.chartView
			layoutParams = RelativeLayout.LayoutParams(matchParent, 88.uiPX())
			y = 45.uiPX().toFloat()
		}.into(this)
		lineChart.setMargins<FrameLayout.LayoutParams> {
			margin = 10.uiPX()
		}
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		setMargins<RecyclerView.LayoutParams> {
			leftMargin = PaddingSize.gsCard
		}
	}
}