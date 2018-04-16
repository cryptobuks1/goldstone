package io.goldstone.blockchain.module.common.walletimport.keystoreimport.view

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.view.Gravity
import android.widget.LinearLayout
import com.blinnnk.extension.into
import com.blinnnk.extension.setMargins
import com.blinnnk.uikit.ScreenSize
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.*
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.value.CommonText
import io.goldstone.blockchain.common.value.CreateWalletText
import io.goldstone.blockchain.common.value.Spectrum
import io.goldstone.blockchain.module.common.walletimport.keystoreimport.presenter.KeystoreImportPresenter
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

/**
 * @date 23/03/2018 1:49 AM
 * @author KaySaith
 */

class KeystoreImportFragment : BaseFragment<KeystoreImportPresenter>() {

  private val attentionView by lazy { AttentionTextView(context!!) }
  private val keystoreEditText by lazy { WalletEditText(context!!) }
  private val nameInput by lazy { RoundInput(context!!) }
  private val passwordInput by lazy { RoundInput(context!!) }
  private val agreementView by lazy { AgreementView(context!!) }
  private val confirmButton by lazy { RoundButton(context!!) }

  override val presenter = KeystoreImportPresenter(this)

  @SuppressLint("SetTextI18n")
  override fun AnkoContext<Fragment>.initView() {

    verticalLayout {
      attentionView
        .apply {
          setMargins<LinearLayout.LayoutParams> { topMargin = 80.uiPX() }
          text = "Kevin Federline has two kids with Britney Spears and four more with other women -- and if he's not spending his child support checks properly, that could pose some new problems for him in court."
        }
        .into(this)

      keystoreEditText
        .apply {
          setMargins<LinearLayout.LayoutParams> { topMargin = 30.uiPX() }
        }
        .into(this)

      nameInput
        .apply {
          setMargins<LinearLayout.LayoutParams> { topMargin = 30.uiPX() }
          text = CreateWalletText.name
        }
        .into(this)

      passwordInput
        .apply {
          setPasswordInput()
          setMargins<LinearLayout.LayoutParams> { topMargin = 30.uiPX() }
          text = CreateWalletText.password
        }
        .into(this)

      agreementView
        .apply {
          setMargins<LinearLayout.LayoutParams> { topMargin = 20.uiPX() }
        }
        .into(this)

      confirmButton
        .apply {
          setBlueStyle()
          text = CommonText.confirm.toUpperCase()
          y += 10.uiPX()
        }
        .click {
          presenter.importKeystoreWallet(
            keystoreEditText.text.toString(),
            passwordInput,
            nameInput,
            agreementView.radioButton.isChecked
          )
        }
        .into(this)

      textView("What is keystore?") {
        textSize = 5.uiPX().toFloat()
        typeface = GoldStoneFont.heavy(context)
        layoutParams = LinearLayout.LayoutParams(ScreenSize.Width, 30.uiPX()).apply {
          topMargin = 20.uiPX()
        }
        textColor = Spectrum.blue
        gravity = Gravity.CENTER
      }
    }

  }

}