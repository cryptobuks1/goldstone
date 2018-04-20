package io.goldstone.blockchain.module.home.quotation.quotation.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blinnnk.extension.addCorner
import com.blinnnk.extension.into
import com.blinnnk.extension.setAlignParentRight
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import com.db.chart.animation.Animation
import com.db.chart.model.LineSet
import com.db.chart.model.Point
import com.db.chart.view.LineChartView
import io.goldstone.blockchain.common.component.TwoLineTitles
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.value.*
import io.goldstone.blockchain.module.home.quotation.quotation.model.QuotationModel
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout

@SuppressLint("SetTextI18n")
/**
 * @date 20/04/2018 8:18 PM
 * @author KaySaith
 */

class QuotationCell(context: Context) : LinearLayout(context) {

  var model: QuotationModel by observing(QuotationModel()) {
    tokenInfo.title.text = model.symbol
    tokenInfo.subtitle.text = model.name
    tokenPrice.title.text = model.price
    tokenPrice.subtitle.text = model.percent + "%"
    if (model.percent.toDouble() < 0) {
      tokenPrice.setColorStyle(Spectrum.red)
      chartColor = Spectrum.lightRed
      chartLineColor = Spectrum.red
    }
    chartData = model.chartData
  }

  private val tokenInfo by lazy {
    TwoLineTitles(context).apply {
      x += 20.uiPX()
      setBlackTitles()
      setQuotationStyle()
    }
  }

  private val tokenPrice by lazy {
    TwoLineTitles(context).apply {
      x -= 20.uiPX()
      setQuotationStyle()
      setColorStyle(Spectrum.green)
      isFloatRight = true
    }
  }

  private var chartColor = Spectrum.lightGreen
  private var chartLineColor = Spectrum.green

  private val chartView = LineChartView(context)

  private var cellLayout: RelativeLayout? = null

  private var chartData: ArrayList<Point> by observing(arrayListOf()) {
    chartView
      .apply {
        layoutParams = RelativeLayout.LayoutParams(com.blinnnk.uikit.ScreenSize.Width - 20.uiPX(), 130.uiPX()).apply {
          margin = 10.uiPX()
        }
        y += 60.uiPX()
        // 设定背景的网格
        setGrid(5, 10, Paint().apply { isAntiAlias = true; style = Paint.Style.FILL; color = GrayScale.lightGray })
        // 设定便捷字体颜色
        setLabelsColor(GrayScale.midGray)
        // 设定 `Y` 周波段
        setAxisBorderValues(0f, 100f, 25f)
        // 设定外界 `Border` 颜色
        setAxisColor(Color.argb(0, 0, 0, 0))
        // 设定外边的 `Border` 的粗细
        setAxisThickness(0f)

        val dataSet = LineSet()
        dataSet.apply {
          chartData.forEach { addPoint(it) }
          // 这个是线的颜色
          color = chartLineColor
          // 渐变色彩
          setGradientFill(intArrayOf(chartColor, Color.TRANSPARENT), floatArrayOf(0.28f, 1f))
          // 线条联动用贝塞尔曲线
          isSmooth = true
          // 线条的粗细
          thickness = 3.uiPX().toFloat()
          // 设定字体
          setTypeface(GoldStoneFont.heavy(context))
          setFontSize(9.uiPX())
        }

        addData(dataSet)

        setClickablePointRadius(30.uiPX().toFloat())

        val animation = Animation(1000)
        animation.setInterpolator(OvershootInterpolator())
        show(animation)
      }
      .into(cellLayout!!)
  }

  init {

    layoutParams = LinearLayout.LayoutParams(matchParent, 220.uiPX())

    cellLayout = relativeLayout {
      layoutParams = RelativeLayout.LayoutParams(ScreenSize.widthWithPadding, 210.uiPX())
      addCorner(CornerSize.default.toInt(), Spectrum.white)
      x += PaddingSize.device

      addView(tokenInfo)
      addView(tokenPrice)
      tokenPrice.setAlignParentRight()
    }
  }

}