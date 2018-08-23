package io.goldstone.blockchain.module.common.tokendetail.tokendetail.view

import android.content.Context
import com.github.mikephil.charting.data.Entry
import io.goldstone.blockchain.common.component.chart.line.LineChart

/**
 * @date: 2018/8/23.
 * @author: yanglihai
 * @description:
 */
class TokenDetaiHeaderLineChart(context: Context) : LineChart(context) {
	
	override fun dragEnable(): Boolean = false
	
	override fun touchEnable(): Boolean = true
	
	override fun isDrawPoints(): Boolean = true
	
	override fun isPerformBezier(): Boolean = true
	
	override fun resetData(dataRows: List<Entry>) {
		
		var maxValue = dataRows[0].y
		var minValue = dataRows[0].y
		dataRows.forEach {
			if (it.y > maxValue) {
				maxValue = it.y
			}
			if (it.y < minValue) {
				minValue = it.y
			}
		}
		if (maxValue != minValue) {
			val distance = (maxValue - minValue) / 2f
			axisLeft.axisMinimum = minValue - distance
			axisLeft.axisMaximum = maxValue + distance
		}
		
		super.resetData(dataRows)
	}
}