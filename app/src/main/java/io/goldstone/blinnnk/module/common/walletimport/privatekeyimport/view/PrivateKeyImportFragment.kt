package io.goldstone.blinnnk.module.common.walletimport.privatekeyimport.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.widget.LinearLayout
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.getParentFragment
import io.goldstone.blinnnk.common.base.basefragment.BaseFragment
import io.goldstone.blinnnk.common.component.AgreementView
import io.goldstone.blinnnk.common.component.button.RoundButton
import io.goldstone.blinnnk.common.component.button.roundButton
import io.goldstone.blinnnk.common.component.edittext.RoundInput
import io.goldstone.blinnnk.common.component.edittext.WalletEditText
import io.goldstone.blinnnk.common.component.edittext.roundInput
import io.goldstone.blinnnk.common.component.title.AttentionTextView
import io.goldstone.blinnnk.common.component.title.ExplanationTitle
import io.goldstone.blinnnk.common.language.*
import io.goldstone.blinnnk.common.thread.launchUI
import io.goldstone.blinnnk.common.utils.NetworkUtil
import io.goldstone.blinnnk.common.utils.UIUtils
import io.goldstone.blinnnk.common.utils.click
import io.goldstone.blinnnk.common.utils.safeShowError
import io.goldstone.blinnnk.common.value.ArgumentKey
import io.goldstone.blinnnk.common.value.PaddingSize
import io.goldstone.blinnnk.common.value.WebUrl
import io.goldstone.blinnnk.module.common.walletgeneration.createwallet.presenter.CreateWalletPresenter
import io.goldstone.blinnnk.module.common.walletimport.privatekeyimport.presenter.PrivateKeyImportPresenter
import io.goldstone.blinnnk.module.common.walletimport.walletimport.view.WalletImportFragment
import io.goldstone.blinnnk.module.common.walletimport.walletimportcenter.view.SupportedChainMenu
import io.goldstone.blinnnk.module.common.webview.view.WebViewFragment
import io.goldstone.blinnnk.module.entrance.splash.view.SplashActivity
import io.goldstone.blinnnk.module.home.home.view.MainActivity
import org.jetbrains.anko.*

/**
 * @date 23/03/2018 2:13 AM
 * @author KaySaith
 */
class PrivateKeyImportFragment : BaseFragment<PrivateKeyImportPresenter>() {

	override val pageTitle: String = ImportMethodText.privateKey
	private lateinit var passwordHintInput: RoundInput
	private lateinit var nameInput: RoundInput
	private lateinit var passwordInput: RoundInput
	private lateinit var repeatPassword: RoundInput
	private lateinit var confirmButton: RoundButton
	private val attentionText by lazy { AttentionTextView(context!!) }
	private val supportedChainMenu by lazy { SupportedChainMenu(context!!) }
	private val privateKeyInput by lazy { WalletEditText(context!!) }
	private val agreementView by lazy { AgreementView(context!!) }
	override val presenter = PrivateKeyImportPresenter(this)

	override fun AnkoContext<Fragment>.initView() {
		scrollView {
			lparams(matchParent, wrapContent)
			verticalLayout {
				gravity = Gravity.CENTER_HORIZONTAL
				lparams(matchParent, wrapContent)
				attentionText.apply {
					isCenter()
					setPadding(15.uiPX(), 30.uiPX(), 15.uiPX(), 20.uiPX())
					layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
					text = ImportWalletText.importWalletDescription
				}.into(this)
				supportedChainMenu.into(this)

				privateKeyInput.apply {
					hint = ImportWalletText.privateKeyHint
				}.into(this)

				nameInput = roundInput {
					horizontalPaddingSize = PaddingSize.gsCard
					hint = UIUtils.generateDefaultName()
					setMargins<LinearLayout.LayoutParams> { topMargin = 20.uiPX() }
					title = CreateWalletText.name
				}

				passwordInput = roundInput {
					horizontalPaddingSize = PaddingSize.gsCard
					setPasswordInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 5.uiPX() }
					title = CreateWalletText.password
					setPasswordSafeLevel()
				}

				repeatPassword = roundInput {
					horizontalPaddingSize = PaddingSize.gsCard
					setPasswordInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 5.uiPX() }
					title = CreateWalletText.repeatPassword
				}

				passwordHintInput = roundInput {
					horizontalPaddingSize = PaddingSize.gsCard
					title = CreateWalletText.hint
					setTextInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 5.uiPX() }
				}

				agreementView.click {
					getParentFragment<WalletImportFragment> {
						presenter.showTargetFragment<WebViewFragment>(
							Bundle().apply {
								putString(ArgumentKey.webViewUrl, WebUrl.terms)
								putString(ArgumentKey.webViewName, ProfileText.terms)
							}
						)
					}
				}.into(this)

				confirmButton = roundButton {
					text = CommonText.confirm.toUpperCase()
					setBlueStyle(10.uiPX())
				}.click { button ->
					button.showLoadingStatus()
					presenter.importWalletByPrivateKey(
						privateKeyInput,
						passwordInput,
						repeatPassword,
						agreementView.radioButton.isChecked,
						nameInput,
						passwordHintInput
					) {
						launchUI {
							button.showLoadingStatus(false)
							if (it.hasError()) safeShowError(it)
							else activity?.jump<SplashActivity>()
						}
					}
				}

				ExplanationTitle(context).apply {
					text = QAText.whatIsPrivateKey.setUnderline()
				}.click {
					getParentFragment<WalletImportFragment> {
						NetworkUtil.hasNetworkWithAlert(context) isTrue {
							presenter.showTargetFragment<WebViewFragment>(
								Bundle().apply {
									putString(ArgumentKey.webViewUrl, WebUrl.whatIsPrivatekey)
									putString(ArgumentKey.webViewName, QAText.whatIsPrivateKey)
								}
							)
						}
					}
				}.into(this)
			}
		}
	}

	private fun RoundInput.setPasswordSafeLevel() {
		afterTextChanged = Runnable {
			CreateWalletPresenter.showPasswordSafeLevel(passwordInput)
		}
	}

	override fun setBaseBackEvent(activity: MainActivity?, parent: Fragment?) {
		getParentFragment<WalletImportFragment>()?.presenter
			?.popFragmentFrom<PrivateKeyImportFragment>()
	}

}