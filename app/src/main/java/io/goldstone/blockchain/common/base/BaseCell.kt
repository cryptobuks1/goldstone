package io.goldstone.blockchain.common.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blinnnk.animation.addTouchRippleAnimation
import com.blinnnk.extension.setAlignParentRight
import com.blinnnk.extension.setCenterInVertical
import com.blinnnk.uikit.RippleMode
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blockchain.common.component.ArrowIconView
import io.goldstone.blockchain.common.value.*
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.rightPadding

/**
 * @date 23/03/2018 11:46 PM
 * @author KaySaith
 */
open class BaseCell(context: Context) : RelativeLayout(context) {
	
	var hasArrow: Boolean by observing(true) {
		if (hasArrow) arrowIcon.visibility = View.VISIBLE
		else arrowIcon.visibility = View.GONE
	}
	private val paint = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL
		color = Spectrum.opacity2White
	}
	protected val arrowIcon by lazy { ArrowIconView(context) }
	
	init {
		this.setWillNotDraw(false)
		layoutParams = LinearLayout.LayoutParams(matchParent, 60.uiPX()).apply {
			leftPadding = PaddingSize.device
			rightPadding = PaddingSize.device
		}
		
		this.addView(arrowIcon)
		arrowIcon.x += 3.uiPX()
		arrowIcon.setAlignParentRight()
		arrowIcon.setCenterInVertical()
	}
	
	private var hasTopLine = false
	
	@SuppressLint("DrawAllocation")
	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
		
		if (hasTopLine) {
			canvas?.drawLine(
				PaddingSize.device.toFloat(),
				0f,
				(width - PaddingSize.device).toFloat(),
				BorderSize.bold,
				paint
			)
		}
		
		canvas?.drawLine(
			PaddingSize.device.toFloat(),
			height - BorderSize.default,
			(width - PaddingSize.device).toFloat(),
			height - BorderSize.default,
			paint
		)
	}
	
	fun hasTopLine() {
		hasTopLine = true
		invalidate()
	}
	
	fun setGrayStyle() {
		arrowIcon.setGrayStyle()
		paint.color = GrayScale.Opacity1Black
		invalidate()
		addTouchRippleAnimation(Color.WHITE, GrayScale.lightGray, RippleMode.Square)
	}
}