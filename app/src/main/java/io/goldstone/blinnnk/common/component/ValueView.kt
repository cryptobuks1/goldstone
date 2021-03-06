package io.goldstone.blinnnk.common.component

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.TextView
import com.blinnnk.extension.addCorner
import com.blinnnk.uikit.uiPX
import io.goldstone.blinnnk.common.utils.GoldStoneFont
import io.goldstone.blinnnk.common.value.CornerSize
import io.goldstone.blinnnk.common.value.GrayScale
import io.goldstone.blinnnk.common.value.ScreenSize
import io.goldstone.blinnnk.common.value.fontSize
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView


/**
 * @author KaySaith
 * @date  2018/09/25
 */
class ValueView(context: Context) : TextView(context) {

	init {
		addCorner(CornerSize.small.toInt(), GrayScale.whiteGray)
		layoutParams = ViewGroup.LayoutParams(ScreenSize.widthWithPadding, wrapContent)
		layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
		maxLines = 8
		movementMethod = ScrollingMovementMethod()
		padding = 20.uiPX()
		gravity = Gravity.CENTER_VERTICAL
		textSize = fontSize(14)
		textColor = GrayScale.black
		typeface = GoldStoneFont.heavy(context)
	}

	fun getContent(): String {
		return text.toString()
	}

	fun setContent(text: String) {
		this.text = text
	}
}

fun ViewManager.valueView() = valueView {}
inline fun ViewManager.valueView(init: ValueView.() -> Unit) = ankoView({ ValueView(it) }, 0, init)