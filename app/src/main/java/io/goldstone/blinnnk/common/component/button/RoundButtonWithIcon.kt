package io.goldstone.blinnnk.common.component.button

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.animation.addTouchRippleAnimation
import com.blinnnk.extension.alignParentRight
import com.blinnnk.extension.centerInVertical
import com.blinnnk.extension.into
import com.blinnnk.extension.measureTextWidth
import com.blinnnk.uikit.RippleMode
import com.blinnnk.uikit.uiPX
import io.goldstone.blinnnk.common.component.ArrowIconView
import io.goldstone.blinnnk.common.language.HoneyLanguage
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.utils.GoldStoneFont
import io.goldstone.blinnnk.common.value.Spectrum
import io.goldstone.blinnnk.common.value.fontSize
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textColor

/**
 * @date 23/03/2018 5:06 PM
 * @author KaySaith
 */
class RoundButtonWithIcon(context: Context) : RelativeLayout(context) {

	private val titleView by lazy { TextView(context) }
	private val viewHeight = 30.uiPX()
	private val arrowIcon by lazy { ArrowIconView(context) }

	init {
		titleView.apply {
			layoutParams = RelativeLayout.LayoutParams(matchParent, matchParent)
			gravity = Gravity.CENTER
			textColor = Spectrum.white
			typeface = GoldStoneFont.heavy(context)
			textSize = fontSize(12)
		}.into(this)

		layoutParams = RelativeLayout.LayoutParams(0, viewHeight)
		backgroundColor = Color.WHITE
		addTouchRippleAnimation(Spectrum.green, Spectrum.yellow, RippleMode.Square, viewHeight / 2f)
		elevation = 4.uiPX().toFloat()

		arrowIcon
			.apply { setWhiteSytle() }
			.into(this)
		arrowIcon.alignParentRight()
		arrowIcon.centerInVertical()
	}

	fun setTitle(text: String) {
		titleView.text = text
		layoutParams.width = text.measureTextWidth(16.uiPX().toFloat()).toInt() + retWidthDependsOnLanguage()
	}

	fun removeIcon() {
		layoutParams.width -= 5.uiPX()
		removeView(arrowIcon)
	}

	private fun retWidthDependsOnLanguage(): Int {
		return when (SharedWallet.getCurrentLanguageCode()) {
			HoneyLanguage.Chinese.code -> 30.uiPX()
			HoneyLanguage.Korean.code -> 30.uiPX()
			HoneyLanguage.TraditionalChinese.code -> 30.uiPX()
			else -> 5.uiPX()
		}
	}
}