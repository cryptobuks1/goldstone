package io.goldstone.blockchain.module.common.walletimport.mnemonicimport.view

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.view.Gravity
import android.widget.LinearLayout
import com.blinnnk.extension.*
import com.blinnnk.uikit.ScreenSize
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.*
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.value.*
import io.goldstone.blockchain.module.common.walletimport.mnemonicimport.presenter.MnemonicImportDetailPresenter
import io.goldstone.blockchain.module.common.walletimport.walletimport.view.WalletImportFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * @date 23/03/2018 1:46 AM
 * @author KaySaith
 */

class MnemonicImportDetailFragment : BaseFragment<MnemonicImportDetailPresenter>() {

	private val confirmButton by lazy { RoundButton(context!!) }
	private val mnemonicInput by lazy { WalletEditText(context!!) }
	private val pathInput by lazy { RoundInput(context!!) }
	private val walletNameInput by lazy { RoundInput(context!!) }
	private val passwordInput by lazy { RoundInput(context!!) }
	private val repeatPassword by lazy { RoundInput(context!!) }
	private val hintInput by lazy { RoundInput(context!!) }
	private val agreementView by lazy { AgreementView(context!!) }

	override val presenter = MnemonicImportDetailPresenter(this)

	@SuppressLint("SetTextI18n")
	override fun AnkoContext<Fragment>.initView() {
		scrollView {
			verticalLayout {
				gravity = Gravity.CENTER_HORIZONTAL
				lparams(matchParent, matchParent)
				mnemonicInput.apply {
					hint = ImportWalletText.mnemonicHint
					setMargins<LinearLayout.LayoutParams> { topMargin = 80.uiPX() }
				}.into(this)

				pathInput.apply {
					title = "Path"
					hint = "m/44'/60'/0'/0/0"
					setText("m/44'/60'/0'/0/0")
					setMargins<LinearLayout.LayoutParams> { topMargin = 30.uiPX() }
				}.into(this)

				walletNameInput.apply {
					setMargins<LinearLayout.LayoutParams> { topMargin = 10.uiPX() }
					title = CreateWalletText.name
				}.into(this)

				passwordInput.apply {
					setPasswordInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 10.uiPX() }
					title = CreateWalletText.password
				}.into(this)

				repeatPassword.apply {
					setPasswordInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 10.uiPX() }
					title = CreateWalletText.repeatPassword
				}.into(this)

				hintInput.apply {
					setTextInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 10.uiPX() }
					title = CreateWalletText.hint
				}.into(this)

				agreementView.into(this)

				confirmButton.apply {
					text = CommonText.confirm.toUpperCase()
					setBlueStyle()
					y += 10.uiPX()
				}.click {
					it.showLoadingStatus()
					presenter.importWalletByMnemonic(
						pathInput, mnemonicInput, passwordInput, repeatPassword, hintInput,
						agreementView.radioButton.isChecked, walletNameInput
					) {
						it.showLoadingStatus(false)
					}
				}.into(this)
				
				
				ExplanationTitle(context).apply {
					text = QAText.whatIsMnemonic.setUnderline()
				}.click {
					getParentFragment<WalletImportFragment> {
						presenter.showWebViewFragment(WebUrl.whatIsMnemonic, QAText.whatIsMnemonic)
					}
				}.into(this)
			}
		}
	}

}