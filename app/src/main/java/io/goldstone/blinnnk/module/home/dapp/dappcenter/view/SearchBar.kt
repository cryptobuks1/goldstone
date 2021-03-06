package io.goldstone.blinnnk.module.home.dapp.dappcenter.view

import android.content.Context
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.uikit.uiPX
import io.goldstone.blinnnk.R
import io.goldstone.blinnnk.common.component.GSCard
import io.goldstone.blinnnk.common.language.DappCenterText
import io.goldstone.blinnnk.common.value.GrayScale
import io.goldstone.blinnnk.common.value.ScreenSize
import io.goldstone.blinnnk.common.value.fontSize
import org.jetbrains.anko.*


/**
 * @author KaySaith
 * @date  2018/12/01
 */
class SearchBar(context: Context) : GSCard(context) {

	private lateinit var input: TextView

	init {
		layoutParams = LinearLayout.LayoutParams(ScreenSize.card, wrapContent)
		relativeLayout {
			lparams(matchParent, 45.uiPX())
			imageView {
				imageResource = R.drawable.search_icon
				scaleType = ImageView.ScaleType.CENTER_INSIDE
				layoutParams = RelativeLayout.LayoutParams(45.uiPX(), matchParent)
				setColorFilter(GrayScale.midGray)
			}
			input = textView {
				layoutParams = RelativeLayout.LayoutParams(matchParent, matchParent)
				leftPadding = 45.uiPX()
				text = DappCenterText.dappSearchBarPlaceholderText
				textColor = GrayScale.midGray
				textSize = fontSize(14)
				gravity = Gravity.CENTER_VERTICAL
			}
		}
	}
}