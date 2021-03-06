package io.goldstone.blinnnk.module.common.tokenpayment.gasselection.view

/**
 * @date 2018/5/16 11:43 PM
 * @author KaySaith
 */

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import com.blinnnk.extension.centerInVertical
import com.blinnnk.extension.into
import com.blinnnk.uikit.uiPX
import io.goldstone.blinnnk.common.base.basecell.BaseCell
import io.goldstone.blinnnk.common.component.button.RoundButton
import io.goldstone.blinnnk.common.language.CommonText
import io.goldstone.blinnnk.common.language.TokenDetailText
import io.goldstone.blinnnk.common.utils.GoldStoneFont
import io.goldstone.blinnnk.common.value.GrayScale
import io.goldstone.blinnnk.common.value.fontSize
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView

/**
 * @date 28/03/2018 2:34 PM
 * @author KaySaith
 */

class GasSelectionFooter(context: Context) : LinearLayout(context) {

	private val customButton by lazy { BaseCell(context) }
	private val confirmButton by lazy { RoundButton(context) }

	init {
		orientation = VERTICAL
		gravity = Gravity.CENTER_HORIZONTAL
		layoutParams = LinearLayout.LayoutParams(matchParent, 120.uiPX())
		customButton.apply {
			setHorizontalPadding()
			layoutParams = LinearLayout.LayoutParams(matchParent, 40.uiPX())
			textView {
				setGrayStyle()
				text = TokenDetailText.customMiner
				textColor = GrayScale.gray
				textSize = fontSize(15)
				typeface = GoldStoneFont.book(context)
			}.centerInVertical()
		}.into(this)

		confirmButton.apply {
			setBlueStyle(20.uiPX())
			text = CommonText.next.toUpperCase()
		}.into(this)
	}

	fun getConfirmButton(hold: RoundButton.() -> Unit) {
		hold(confirmButton)
	}

	fun getCustomButton(hold: BaseCell.() -> Unit) {
		hold(customButton)
	}

}