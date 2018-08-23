package io.goldstone.blockchain.module.common.tokendetail.tokendetail.view

import android.content.Context
import android.widget.RelativeLayout
import com.blinnnk.extension.*
import com.blinnnk.uikit.ScreenSize
import com.blinnnk.uikit.uiPX
import com.github.mikephil.charting.data.Entry
import io.goldstone.blockchain.R
import io.goldstone.blockchain.common.component.button.ButtonMenu
import io.goldstone.blockchain.common.component.chart.line.LineChart
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.value.Spectrum
import io.goldstone.blockchain.common.value.TokenDetailSize
import io.goldstone.blockchain.module.home.quotation.quotation.model.ChartPoint
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent

/**
 * @date 27/03/2018 3:20 PM
 * @author KaySaith
 */
class TokenDetailHeaderView(context: Context) : RelativeLayout(context) {
	
	val menu = ButtonMenu(context)
	
	private val blinnnkLineChart = TokenDetaiHeaderLineChart(context)
	
	init {
		layoutParams = RelativeLayout.LayoutParams(matchParent, TokenDetailSize.headerHeight)
		blinnnkLineChart.apply {
			layoutParams = RelativeLayout.LayoutParams(
				ScreenSize.Width - 20.uiPX(),
				TokenDetailSize.headerHeight - menu.layoutParams.height - 40.uiPX()
			)
			setMargins<RelativeLayout.LayoutParams> { margin = 10.uiPX() }
			
			blinnnkLineChart.setChartColorAndShadowResource(Spectrum.darkBlue, R.drawable.fade_green)
		}.into(this)
		
		menu.apply {
			y -= 10.uiPX()
			titles = arrayListOf(
				CommonText.all,
				CommonText.deposit,
				CommonText.send,
				CommonText.failed
			)
		}
			.into(this)
		menu.setAlignParentBottom()
		menu.setCenterInParent()
		menu.selected(0)
	}
	
	fun setCharData(data: ArrayList<ChartPoint>) {
		blinnnkLineChart.resetData(
			data.mapIndexed {
					index, chartPoint ->
					Entry(index.toFloat(), chartPoint.value, chartPoint.label)
			}.toArrayList(),
			true
		)
	}
}