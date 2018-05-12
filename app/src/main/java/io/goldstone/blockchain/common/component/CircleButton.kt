package io.goldstone.blockchain.common.component

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.animation.updateOriginYAnimation
import com.blinnnk.extension.addCorner
import com.blinnnk.extension.into
import com.blinnnk.uikit.Size
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.value.Spectrum
import org.jetbrains.anko.centerInParent
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textColor

/**
 * @date 24/03/2018 12:54 AM
 * @author KaySaith
 */

class CircleButton(context: Context) : LinearLayout(context) {

	var title: String by observing("") {
		buttonTitle.text = title
	}

	var src: Int by observing(0) {
		icon.imageResource = src
	}

	private var iconView: RelativeLayout = RelativeLayout(context)

	private val icon by lazy {
		ImageView(context).apply {
			layoutParams = LinearLayout.LayoutParams(matchParent, iconSize)
			scaleType = ImageView.ScaleType.CENTER_INSIDE
		}
	}
	private val buttonTitle by lazy {
		TextView(context).apply {
			layoutParams = LinearLayout.LayoutParams(matchParent, 25.uiPX())
			typeface = GoldStoneFont.medium(context)
			textColor = Spectrum.opacity5White
			gravity = Gravity.CENTER_HORIZONTAL
			y += 5.uiPX()
		}
	}

	private var viewSize = Size(
		30.uiPX(), 65.uiPX()
	)

	private var iconSize = 30.uiPX()

	fun setStyleParameter(
		viewSize: Size = this.viewSize,
		iconSize: Int = this.iconSize,
		backgroundColor: Int = Spectrum.opacity2White,
		iconColor: Int = Spectrum.white
	) {
		this.viewSize = viewSize
		this.iconSize = iconSize
		layoutParams = LinearLayout.LayoutParams(viewSize.width, viewSize.height)
		iconView.layoutParams = LinearLayout.LayoutParams(viewSize.width, viewSize.width)
		setIconViewColor(backgroundColor)
		icon.setColorFilter(iconColor)
		icon.layoutParams = RelativeLayout.LayoutParams(iconSize, iconSize).apply {
			centerInParent()
		}
	}

	fun setTitleStyle(
		titleSize: Float = 3.uiPX().toFloat(),
		color: Int = Spectrum.white,
		typeFace: Typeface = GoldStoneFont.medium(context)
	) {
		buttonTitle.textSize = titleSize
		buttonTitle.textColor = color
		buttonTitle.typeface = typeFace
	}

	init {
		setStyleParameter()
		orientation = VERTICAL

		// 背景色的 `Layout`
		iconView.into(this)
		// ICON 图形
		icon.into(iconView)

		buttonTitle.into(this)
		setTitleStyle()
	}

	private fun setIconViewColor(color: Int) {
		iconView.addCorner(viewSize.width / 2, color)
	}

	fun setUnTransparent() {
		buttonTitle.textColor = Spectrum.white
		updateOriginYAnimation(17.uiPX().toFloat())
		buttonTitle.updateOriginYAnimation(28.uiPX().toFloat())
		setIconViewColor(Color.TRANSPARENT)
	}

	fun setDefaultStyle() {
		buttonTitle.textColor = Spectrum.opacity5White
		updateOriginYAnimation(27.uiPX().toFloat())
		buttonTitle.updateOriginYAnimation(35.uiPX().toFloat())
		setIconViewColor(Spectrum.opacity2White)
	}

}