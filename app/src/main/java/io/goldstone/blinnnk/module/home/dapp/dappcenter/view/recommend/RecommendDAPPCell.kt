package io.goldstone.blinnnk.module.home.dapp.dappcenter.view.recommend

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import com.blinnnk.extension.addCorner
import com.blinnnk.extension.into
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blinnnk.R
import io.goldstone.blinnnk.common.component.GSCard
import io.goldstone.blinnnk.common.component.title.TwoLineTitles
import io.goldstone.blinnnk.common.utils.glideImage
import io.goldstone.blinnnk.common.value.ShadowSize
import io.goldstone.blinnnk.common.value.Spectrum
import io.goldstone.blinnnk.common.value.fontSize
import io.goldstone.blinnnk.module.home.dapp.dappcenter.model.DAPPTable
import org.jetbrains.anko.*


/**
 * @author KaySaith
 * @date  2018/12/01
 */
class RecommendDAPPCell(context: Context) : GSCard(context) {

	var model: DAPPTable? by observing(null) {
		model?.apply {
			image.glideImage(banner)
			titles.title.text = title
			titles.subtitle.text = description
		}
	}

	private lateinit var image: ImageView
	private lateinit var titles: TwoLineTitles
	private val playButton = LinearLayout(context)

	init {
		layoutParams = LinearLayout.LayoutParams(140.uiPX(), 200.uiPX())
		resetCardElevation(ShadowSize.Cell)
		verticalLayout {
			image = imageView {
				scaleType = ImageView.ScaleType.CENTER_CROP
				layoutParams = LinearLayout.LayoutParams(matchParent, 140.uiPX())
			}
			titles = TwoLineTitles(context).apply {
				padding = 10.uiPX()
				layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
				setBlackTitles(fontSize(11), 2.uiPX(), fontSize(9))
			}
			titles.into(this)
		}
		playButton.apply {
			imageView {
				imageResource = R.drawable.play_icon
				layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
				scaleType = ImageView.ScaleType.CENTER_INSIDE
				setColorFilter(Spectrum.white)
				x += 2.uiPX()
			}
			layoutParams = LinearLayout.LayoutParams(32.uiPX(), 32.uiPX())
			backgroundColor = Spectrum.green
			addCorner(16.uiPX(), Spectrum.green)
			x += 90.uiPX()
			y += 120.uiPX()
			elevation = 6.uiPX().toFloat()
		}.into(this)
	}
}