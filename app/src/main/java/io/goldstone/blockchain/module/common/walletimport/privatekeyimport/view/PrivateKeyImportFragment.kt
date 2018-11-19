package io.goldstone.blockchain.module.common.walletimport.privatekeyimport.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.AgreementView
import io.goldstone.blockchain.common.component.button.RoundButton
import io.goldstone.blockchain.common.component.edittext.RoundInput
import io.goldstone.blockchain.common.component.edittext.WalletEditText
import io.goldstone.blockchain.common.component.title.AttentionTextView
import io.goldstone.blockchain.common.component.title.ExplanationTitle
import io.goldstone.blockchain.common.language.*
import io.goldstone.blockchain.common.thread.launchUI
import io.goldstone.blockchain.common.utils.NetworkUtil
import io.goldstone.blockchain.common.utils.UIUtils
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.utils.safeShowError
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.WebUrl
import io.goldstone.blockchain.crypto.multichain.AddressType
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.presenter.CreateWalletPresenter
import io.goldstone.blockchain.module.common.walletimport.privatekeyimport.presenter.PrivateKeyImportPresenter
import io.goldstone.blockchain.module.common.walletimport.walletimport.view.WalletImportFragment
import io.goldstone.blockchain.module.common.walletimport.walletimportcenter.view.SupportedChainMenu
import io.goldstone.blockchain.module.common.webview.view.WebViewFragment
import io.goldstone.blockchain.module.entrance.splash.view.SplashActivity
import org.jetbrains.anko.*

/**
 * @date 23/03/2018 2:13 AM
 * @author KaySaith
 */
class PrivateKeyImportFragment : BaseFragment<PrivateKeyImportPresenter>() {

	override val pageTitle: String = ImportMethodText.privateKey
	private val attentionText by lazy { AttentionTextView(context!!) }
	private val supportedChainMenu by lazy { SupportedChainMenu(context!!) }
	private val privateKeyInput by lazy { WalletEditText(context!!) }
	private val passwordHintInput by lazy { RoundInput(context!!) }
	private val nameInput by lazy { RoundInput(context!!) }
	private val passwordInput by lazy { RoundInput(context!!) }
	private val repeatPassword by lazy { RoundInput(context!!) }
	private val agreementView by lazy { AgreementView(context!!) }
	private val confirmButton by lazy { RoundButton(context!!) }
	override val presenter = PrivateKeyImportPresenter(this)

	override fun AnkoContext<Fragment>.initView() {
		scrollView {
			verticalLayout {
				gravity = Gravity.CENTER_HORIZONTAL
				lparams(matchParent, matchParent)
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

				nameInput.apply {
					hint = UIUtils.generateDefaultName()
					setMargins<LinearLayout.LayoutParams> { topMargin = 20.uiPX() }
					title = CreateWalletText.name
				}.into(this)

				passwordInput.apply {
					setPasswordInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 5.uiPX() }
					title = CreateWalletText.password
					setPasswordSafeLevel()
				}.into(this)

				repeatPassword.apply {
					setPasswordInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 5.uiPX() }
					title = CreateWalletText.repeatPassword
				}.into(this)

				passwordHintInput.apply {
					title = CreateWalletText.hint
					setTextInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 5.uiPX() }
				}.into(this)

				agreementView
					.click {
						getParentFragment<WalletImportFragment> {
							presenter.showTargetFragment<WebViewFragment>(
								Bundle().apply {
									putString(ArgumentKey.webViewUrl, WebUrl.terms)
									putString(ArgumentKey.webViewName, ProfileText.terms)
								}
							)
						}
					}
					.into(this)

				confirmButton.apply {
					text = CommonText.confirm.toUpperCase()
					setBlueStyle()
					y += 10.uiPX()
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
				}.into(this)

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

	companion object {
		fun showWalletTypeDashboard(
			context: Context,
			type: String,
			updateCurrentType: (String) -> Unit
		) {
			val data = arrayListOf(
				AddressType.ETHSeries.value,
				AddressType.BTC.value,
				AddressType.BTCSeriesTest.value,
				AddressType.LTC.value,
				AddressType.BCH.value,
				AddressType.EOS.value,
				AddressType.EOSJungle.value
			)
			val defaultIndex = data.indexOf(type)
			MaterialDialog(context)
				.title(text = "Wallet Type")
				.listItemsSingleChoice(items = data, initialSelection = defaultIndex) { _, _, item ->
					updateCurrentType(item)
				}
				.positiveButton(text = CommonText.confirm)
				.negativeButton(text = CommonText.cancel)
				.show()
		}
	}
}