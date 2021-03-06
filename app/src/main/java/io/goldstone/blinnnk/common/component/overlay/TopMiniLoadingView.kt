package io.goldstone.blinnnk.common.component.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.widget.RelativeLayout
import com.blinnnk.extension.addCorner
import com.blinnnk.extension.centerInParent
import com.blinnnk.uikit.uiPX
import io.goldstone.blinnnk.common.component.overlay.LoadingView.Companion.addLoadingCircle
import io.goldstone.blinnnk.common.value.ElementID
import io.goldstone.blinnnk.common.value.ShadowSize
import io.goldstone.blinnnk.common.value.Spectrum
import org.jetbrains.anko.relativeLayout

/**
 * @date 07/04/2018 12:29 AM
 * @author KaySaith
 */
@SuppressLint("SetTextI18n")
class TopMiniLoadingView(context: Context) : RelativeLayout(context) {
	private val viewSize = 60.uiPX()
	private val circleSize = 40.uiPX()

	init {
		id = ElementID.topMiniLoading
		isClickable = true
		layoutParams = RelativeLayout.LayoutParams(viewSize, viewSize).apply {
			addRule(CENTER_HORIZONTAL)
		}
		relativeLayout {
			elevation = ShadowSize.default
			lparams(circleSize, circleSize)
			addCorner(circleSize, Spectrum.white)
			addLoadingCircle(this, 30.uiPX()) {
				centerInParent()
			}
		}.centerInParent()
	}
}