package io.goldstone.blockchain.module.common.tokenpayment.paymentvaluedetail.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.extension.into
import com.blinnnk.extension.orElse
import com.blinnnk.extension.setAlignParentBottom
import com.blinnnk.honey.setCursorColor
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.component.GradientType
import io.goldstone.blockchain.common.component.GradientView
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.value.GrayScale
import io.goldstone.blockchain.common.value.PaddingSize
import io.goldstone.blockchain.common.value.ScreenSize
import io.goldstone.blockchain.common.value.Spectrum
import io.goldstone.blockchain.crypto.formatCurrency
import io.goldstone.blockchain.module.home.wallet.transactions.transactiondetail.view.TransactionDetailCell
import org.jetbrains.anko.*

/**
 * @date 28/03/2018 12:25 PM
 * @author KaySaith
 */
@SuppressLint("SetTextI18n")
class PaymentValueDetailHeaderView(context: Context) : RelativeLayout(context) {

  private val gradientView by lazy { GradientView(context) }
  private val description by lazy { TextView(context) }
  private val valueInput by lazy { EditText(context) }
  private val priceInfo by lazy { TextView(context) }
  private val addressRemind by lazy { TransactionDetailCell(context) }

  private val gradientViewHeight = 170.uiPX()

  init {

    layoutParams = LinearLayout.LayoutParams(matchParent, 260.uiPX())

    gradientView
      .apply {
        layoutParams = LinearLayout.LayoutParams(matchParent, gradientViewHeight)
        setStyle(GradientType.DarkGreenYellow, gradientViewHeight)
      }
      .into(this)

    verticalLayout {
      layoutParams = RelativeLayout.LayoutParams(matchParent, gradientViewHeight)
      gravity = Gravity.CENTER
      description
        .apply {
          layoutParams = LinearLayout.LayoutParams(matchParent, 20.uiPX()).apply {
            topMargin = 15.uiPX()
          }
          text = "Send ETH Count"
          textColor = Spectrum.opacity5White
          textSize = 5.uiPX().toFloat()
          typeface = GoldStoneFont.medium(context)
          gravity = Gravity.CENTER
        }
        .into(this)

      valueInput
        .apply {
          hint = "0.0"
          hintTextColor = Spectrum.opacity5White
          textColor = Spectrum.white
          textSize = 16.uiPX().toFloat()
          typeface = GoldStoneFont.heavy(context)
          gravity = Gravity.CENTER
          inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
          setCursorColor(Spectrum.blue)
          backgroundTintMode = PorterDuff.Mode.CLEAR
          y -= 3.uiPX()
        }
        .into(this)

      priceInfo
        .apply {
          layoutParams = LinearLayout.LayoutParams(matchParent, 20.uiPX()).apply {
            topMargin = -(18.uiPX())
          }
          text = "≈ 0.0 (USD)"
          textColor = Spectrum.opacity5White
          textSize = 4.uiPX().toFloat()
          typeface = GoldStoneFont.medium(context)
          gravity = Gravity.CENTER
        }
        .into(this)
    }

    verticalLayout {
      layoutParams = LinearLayout.LayoutParams(matchParent, 90.uiPX())
      addressRemind.into(this)
      addressRemind.apply {
        setGrayInfoStyle()
      }

      textView {
        text = "Miner Fee"
        textSize = 4.uiPX().toFloat()
        textColor = GrayScale.gray
        typeface = GoldStoneFont.book(context)
        layoutParams = LinearLayout.LayoutParams(ScreenSize.widthWithPadding, 20.uiPX()).apply {
          leftMargin = PaddingSize.device
          topMargin = 10.uiPX()
        }
      }
    }.setAlignParentBottom()

  }

  fun getInputValue() = valueInput.text.toString().toDouble()

  fun setInputFocus() {
    valueInput.hintTextColor = Spectrum.opacity1White
    valueInput.requestFocus()
  }

  fun showTargetAddress(address: String) {
    addressRemind.info.text = address
  }

  fun updateCurrencyValue(value: Double?) {
    priceInfo.text = "≈ ${ value.orElse(0.0).formatCurrency() } (USD)"
  }

  fun inputTextListener(hold: (String) -> Unit) {
    valueInput.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(text: Editable?) {
        text?.apply { hold(toString()) }
      }
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
  }

}