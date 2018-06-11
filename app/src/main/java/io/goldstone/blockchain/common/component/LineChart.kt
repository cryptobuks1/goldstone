package io.goldstone.blockchain.common.component

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import com.blinnnk.extension.setMargins
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import com.db.chart.animation.Animation
import com.db.chart.model.LineSet
import com.db.chart.view.LineChartView
import io.goldstone.blockchain.common.component.LineChart.Companion.Style.LineStyle
import io.goldstone.blockchain.common.component.LineChart.Companion.Style.PointStyle
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.LogUtil
import io.goldstone.blockchain.common.value.ElementID
import io.goldstone.blockchain.common.value.GrayScale
import io.goldstone.blockchain.common.value.Spectrum
import io.goldstone.blockchain.module.home.quotation.quotation.model.ChartPoint
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.toast

/**
 * @date 2018/6/10 5:39 PM
 * @author KaySaith
 */
abstract class LineChart(context: Context) : LineChartView(context) {
	
	abstract fun setChartStyle(): LineChart.Companion.Style
	abstract fun hasAnimation(): Boolean
	abstract fun setChartValueType(): LineChart.Companion.ChartType
	
	open fun updateData(data: ArrayList<ChartPoint>) {
		chartData = data
	}
	
	open fun modifyLineDataSet(chartData: ArrayList<ChartPoint>, dataSet: LineSet) {
		// override something
	}
	
	open fun setEvnetWhenDataIsEmpty(chartData: ArrayList<ChartPoint>): Boolean {
		return false
	}
	
	abstract fun canClickPoint(): Boolean
	
	fun setRedColor() {
		chartColor = Spectrum.lightRed
		chartLineColor = Spectrum.red
	}
	
	fun setGreenColor() {
		chartColor = Spectrum.lightGreen
		chartLineColor = Spectrum.green
	}
	
	private fun setStyle() {
		chartLineColor = when (setChartStyle()) {
			LineStyle -> Spectrum.green
			
			PointStyle -> {
				hasPoint = true
				Spectrum.darkBlue
			}
		}
	}
	
	private var chartLineColor = Spectrum.green
	private var chartColor = Spectrum.lightGreen
	private var hasPoint = false
	private var chartData: ArrayList<ChartPoint> by observing(arrayListOf()) {
		if (chartData.isEmpty()) {
			setEvnetWhenDataIsEmpty(chartData).let {
				if (!it) return@observing
			}
		}
		// 当只有一条数据的时候插入一条数据画出曲线
		if (chartData.size == 1) {
			chartData.add(0, ChartPoint("0", 0f))
		}
		
		if (data.isNotEmpty()) {
			data.clear()
		}
		val dataSet = LineSet()
		
		dataSet.apply {
			chartData.forEach {
				addPoint(it)
			}
			// 对家在数据的时候可以进行任意的修改和调度
			modifyLineDataSet(chartData, dataSet)
			// 这个是线的颜色
			color = chartLineColor
			
			if (hasPoint) {
				// 这个是点的颜色 `circle` 和 `border`
				setDotsColor(Spectrum.white)
				setDotsStrokeColor(Spectrum.blue)
				setDotsRadius(5.uiPX().toFloat())
				setDotsStrokeThickness(3.uiPX().toFloat())
			}
			// 渐变色彩
			setGradientFill(intArrayOf(chartColor, Color.argb(0, 255, 255, 255)), floatArrayOf(0.28f, 1f))
			// 线条联动用贝塞尔曲线
			isSmooth = true
			// 线条的粗细
			thickness = 3.uiPX().toFloat()
			// 设定字体
			setTypeface(GoldStoneFont.heavy(context))
			setFontSize(8.uiPX())
			val maxValue = chartData.max()?.value ?: 0f
			val minValue = chartData.min()?.value ?: 0f
			LineChart.generateChardGridValue(
				maxValue,
				minValue,
				setChartValueType()
			) { min, max, step ->
				setAxisBorderValues(min, max, step)
			}
		}
		addData(dataSet)
		if (canClickPoint()) {
			setClickablePointRadius(30.uiPX().toFloat())
			setOnEntryClickListener { _, entryIndex, _ ->
				context.toast(chartData[entryIndex].value.toString())
			}
		}
		
		try {
			notifyDataUpdate()
			if (setChartValueType() != ChartType.MarketTokenDetail) {
				// 决定是否显示动画
				if (hasAnimation()) {
					val animation = Animation(1000)
					animation.setInterpolator(OvershootInterpolator())
					show(animation)
				} else {
					show()
				}
			}
		} catch (error: Exception) {
			LogUtil.error(this.javaClass.simpleName, error)
		}
		
		if (setChartValueType() == ChartType.MarketTokenDetail) {
			// 决定是否显示动画
			if (hasAnimation()) {
				val animation = Animation(1000)
				animation.setInterpolator(OvershootInterpolator())
				show(animation)
			} else {
				show()
			}
		}
	}
	
	init {
		id = ElementID.chartView
		layoutParams = RelativeLayout.LayoutParams(matchParent, 150.uiPX())
		setMargins<RelativeLayout.LayoutParams> { topMargin = 20.uiPX() }
		// 设置样式
		setStyle()
		// 设定背景的网格
		this.setGrid(5, 10, Paint().apply {
			isAntiAlias = true
			style = Paint.Style.FILL
			color = GrayScale.lightGray
		})
		// 设定便捷字体颜色
		this.setLabelsColor(GrayScale.midGray)
		// 设定外界 `Border` 颜色
		this.setAxisColor(Color.argb(0, 0, 0, 0))
		// 设定外边的 `Border` 的粗细
		this.setAxisThickness(0f)
		this.setClickablePointRadius(30.uiPX().toFloat())
	}
	
	companion object {
		enum class Style {
			LineStyle, PointStyle
		}
		
		enum class ChartType {
			Assets, Quotation, MarketTokenDetail
		}
		
		fun generateChardGridValue(
			maxValue: Float,
			minValue: Float,
			chartType: ChartType,
			hold: (min: Float, max: Float, step: Float) -> Unit
		) {
			// 最低点 = min - (max - min) * minRate
			// 最低点 = min + (max - min) *
			val minRate = when (chartType) {
				ChartType.Assets -> 0.8
				ChartType.Quotation, ChartType.MarketTokenDetail -> 0.1
			}
			val stepsCount = 5 //代表希望分成几个阶段
			val max =
				if (maxValue == minValue) {
					if (maxValue == 0f) (stepsCount - 1).toDouble()
					else maxValue + Math.abs(maxValue * 0.5)
				} else if (maxValue < minValue) {
					minValue.toDouble()
				} else {
					maxValue.toDouble()
				}
			val min = when {
				maxValue == minValue -> maxValue - Math.abs(maxValue * 0.5)
				maxValue < minValue -> maxValue - (minValue - maxValue) * minRate
				else -> minValue - (maxValue - minValue) * minRate
			}
			val roughStep = (max - min) / (stepsCount - 1)
			val stepLevel = Math.pow(10.0, Math.floor(Math.log10(roughStep))) //代表gap的数量级
			val step = (Math.ceil(roughStep / stepLevel) * stepLevel)
			val minChartHeight = (Math.floor(min / step) * step).toFloat()
			val maxChartHeight = ((1.0 + Math.floor(max / step)) * step).toFloat()
			hold(minChartHeight, maxChartHeight, step.toFloat())
		}
	}
}