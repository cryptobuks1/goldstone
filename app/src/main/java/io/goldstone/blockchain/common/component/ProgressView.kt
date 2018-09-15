package io.goldstone.blockchain.common.component

import android.annotation.SuppressLint
import android.content.Context
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.animation.updateWidthAnimation
import com.blinnnk.extension.addCorner
import com.blinnnk.extension.into
import com.blinnnk.extension.setAlignParentRight
import com.blinnnk.extension.setCenterInVertical
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.convertToDiskUnit
import io.goldstone.blockchain.common.utils.convertToTimeUnit
import io.goldstone.blockchain.common.value.GrayScale
import io.goldstone.blockchain.common.value.Spectrum
import io.goldstone.blockchain.common.value.fontSize
import org.jetbrains.anko.*


/**
 * @author KaySaith
 * @date  2018/09/11
 */

class ProgressView(context: Context) : RelativeLayout(context) {
	private val marginSize = 15.uiPX()
	private val title = textView {
		textSize = fontSize(12)
		textColor = GrayScale.black
		typeface = GoldStoneFont.black(context)
		layoutParams = RelativeLayout.LayoutParams(wrapContent, wrapContent)
		setPadding(5.uiPX(), marginSize, 0, 10.uiPX())
	}
	private val subtitle = textView {
		textSize = fontSize(12)
		textColor = GrayScale.black
		typeface = GoldStoneFont.heavy(context)
		layoutParams = RelativeLayout.LayoutParams(wrapContent, wrapContent)
		setPadding(0, marginSize, 5.uiPX(), 10.uiPX())
	}
	private var progressTotalValueView: RelativeLayout
	private lateinit var progressValueView: RelativeLayout

	private val leftValueView = TextView(context).apply {
		layoutParams = RelativeLayout.LayoutParams(wrapContent, wrapContent)
		textSize = fontSize(11)
		typeface = GoldStoneFont.heavy(context)
		textColor = Spectrum.white
		leftPadding = marginSize
	}
	private val rightValueView = TextView(context).apply {
		layoutParams = RelativeLayout.LayoutParams(wrapContent, wrapContent)
		textSize = fontSize(11)
		typeface = GoldStoneFont.heavy(context)
		textColor = Spectrum.white
		rightPadding = marginSize
	}
	private val progressViewHeight = 26.uiPX()

	init {
		layoutParams = RelativeLayout.LayoutParams(matchParent, progressViewHeight + 45.uiPX())
		leftPadding = marginSize
		rightPadding = marginSize
		subtitle.setAlignParentRight()
		progressTotalValueView = relativeLayout {
			lparams {
				width = matchParent
				height = progressViewHeight
				alignParentBottom()
			}
			addCorner(progressViewHeight, Spectrum.grayBlue)
			progressValueView = relativeLayout {
				lparams(0, matchParent)
				addCorner(progressViewHeight, Spectrum.blue)
			}
			leftValueView.into(this)
			leftValueView.setCenterInVertical()
			rightValueView.into(this)
			rightValueView.setCenterInVertical()
			rightValueView.setAlignParentRight()
		}
	}

	fun setTitle(title: String) {
		this.title.text = title
	}

	fun setSubtitle(subtitle: String) {
		this.subtitle.text = subtitle
	}

	private var leftValue = 0L
	@SuppressLint("SetTextI18n")
	fun setLeftValue(value: Long, description: String, isTime: Boolean = false) {
		leftValue = value
		val convertedValue = if (isTime) value.convertToTimeUnit() else value.convertToDiskUnit()
		leftValueView.text = "$convertedValue $description"
		setProgressValue()
	}

	private var rightValue = 0L
	@SuppressLint("SetTextI18n")
	fun setRightValue(value: Long, description: String, isTime: Boolean = false) {
		rightValue = value
		val convertedValue = if (isTime) value.convertToTimeUnit() else value.convertToDiskUnit()
		rightValueView.text = "$convertedValue $description"
		setProgressValue()
	}

	private fun setProgressValue() {
		val percent: Double = if (rightValue == 0L) 0.0 else leftValue / rightValue.toDouble()
		progressValueView.measure(0, 0)
		val width = measuredWidth - marginSize * 2
		progressValueView.updateWidthAnimation((width * percent).toInt())
	}
}