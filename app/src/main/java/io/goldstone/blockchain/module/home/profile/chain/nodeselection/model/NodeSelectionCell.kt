package io.goldstone.blockchain.module.home.profile.chain.nodeselection.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.extension.into
import com.blinnnk.extension.setAlignParentRight
import com.blinnnk.extension.setCenterInVertical
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blockchain.common.sharedpreference.SharedWallet
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.isDefaultStyle
import io.goldstone.blockchain.common.value.GrayScale
import io.goldstone.blockchain.common.value.fontSize
import io.goldstone.blockchain.crypto.multichain.CoinSymbol
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.textColor
import org.jetbrains.anko.wrapContent

/**
 * @date 2018/6/20 9:00 PM
 * @author KaySaith
 */
class NodeSelectionCell(context: Context) : RelativeLayout(context) {

	private var radio: RadioButton
	private val title = TextView(context).apply {
		textSize = fontSize(14)
		textColor = GrayScale.gray
		typeface = GoldStoneFont.heavy(context)
		layoutParams = LinearLayout.LayoutParams(wrapContent, wrapContent)
	}
	private val paint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
		color = GrayScale.midGray
	}

	var isLast by observing(false) {
		invalidate()
	}

	private val leftPadding = 50.uiPX()

	init {
		layoutParams = LinearLayout.LayoutParams(matchParent, 40.uiPX())
		setWillNotDraw(false)
		title.into(this)
		title.x = leftPadding.toFloat()
		title.setCenterInVertical()
		radio = radioButton {
			isDefaultStyle()
			isClickable = false
		}
		radio.setAlignParentRight()
		radio.setCenterInVertical()
	}

	@SuppressLint("DrawAllocation")
	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)

		canvas?.drawLine(
			leftPadding.toFloat(),
			height.toFloat(),
			width.toFloat(),
			height.toFloat(),
			paint
		)

		canvas?.drawLine(
			20.uiPX().toFloat(),
			height / 2f,
			45.uiPX().toFloat(),
			height / 2f,
			paint
		)

		canvas?.drawLine(
			20.uiPX().toFloat(),
			0f,
			20.uiPX().toFloat(),
			if (isLast) height / 2f else height.toFloat(),
			paint
		)
	}

	fun selectRadio() {
		radio.isChecked = true
	}

	fun clearRadio() {
		radio.isChecked = false
	}

	fun isChecked() = radio.isChecked

	fun setData(name: String, isSelected: Boolean, id: Int? = null): NodeSelectionCell {
		title.text =
			if (SharedWallet.getYingYongBaoInReviewStatus() && name.contains(CoinSymbol.pureBTCSymbol, true))
				CoinSymbol.btc() + " " + name.substringAfter(" ")
			else name
		radio.isChecked = isSelected
		id?.let { this.id = it }
		return this
	}
}