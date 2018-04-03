package io.goldstone.blockchain.common.component

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.component.HoneyRadioButton
import com.blinnnk.extension.CustomTargetTextStyle
import com.blinnnk.extension.into
import com.blinnnk.uikit.ScreenSize
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.measureTextWidth
import io.goldstone.blockchain.common.value.GrayScale
import io.goldstone.blockchain.common.value.Spectrum
import org.jetbrains.anko.textColor

/**
 * @date 22/03/2018 3:52 PM
 * @author KaySaith
 */

@SuppressLint("SetTextI18n")
class AgreementView(context: Context) : RelativeLayout(context) {

  val radioButton = HoneyRadioButton(context)
  val textView = TextView(context)

  private var isChecked = false

  init {

    setWillNotDraw(false)

    layoutParams = LinearLayout.LayoutParams(ScreenSize.Width, 30.uiPX()).apply {
      topMargin = 20.uiPX()
    }

    val terms = "Service and privacy policy"
    textView
      .apply {
        layoutParams = LinearLayout.LayoutParams(ScreenSize.Width, 30.uiPX())
        text = CustomTargetTextStyle(terms, "Agree on Terms of $terms", Spectrum.blue, 9.uiPX())
        textSize = 3.uiPX().toFloat() + 1f
        textColor = GrayScale.midGray
        typeface = GoldStoneFont.book(context)
        gravity = Gravity.CENTER
        x += 15.uiPX()
      }
      .into(this)

    radioButton
      .apply {
        scaleX = 0.7f
        scaleY = 0.7f
        setColorStyle(GrayScale.midGray, Spectrum.green)
      }
      .into(this)

    radioButton.x =
      (ScreenSize.Width - textView.text.measureTextWidth(9.uiPX().toFloat())) / 2f - 15.uiPX()

  }

  fun setRadioStatus() {
    isChecked = !isChecked
    radioButton.isChecked = isChecked
  }

}