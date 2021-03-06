package io.goldstone.blinnnk.common.base.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.uikit.uiPX
import io.goldstone.blinnnk.common.component.cell.TopBottomLineCell
import io.goldstone.blinnnk.common.utils.GoldStoneFont
import io.goldstone.blinnnk.common.value.GrayScale
import io.goldstone.blinnnk.common.value.PaddingSize
import io.goldstone.blinnnk.common.value.fontSize
import org.jetbrains.anko.*


/**
 * @author KaySaith
 * @date  2018/09/22
 */
@SuppressLint("ViewConstructor")
open class CardTitleCell(context: Context) : TopBottomLineCell(context) {

	private val subtitleView = TextView(context).apply {
		leftPadding = PaddingSize.card
		rightPadding = PaddingSize.card
		bottomPadding = 10.uiPX()
		textSize = fontSize(14)
		typeface = GoldStoneFont.heavy(context)
		textColor = GrayScale.black
		layoutParams = RelativeLayout.LayoutParams(matchParent, matchParent)
		gravity = Gravity.CENTER_VERTICAL
	}

	init {
		layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
		setHorizontalPadding(PaddingSize.card.toFloat())
		this.addView(subtitleView)
	}


	fun setSubtitle(text: String) {
		subtitleView.visibility = View.VISIBLE
		subtitleView.text = text
	}

}