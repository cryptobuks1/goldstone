package io.goldstone.blinnnk.module.common.walletimport.mnemonicimport.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.getParentFragment
import io.goldstone.blinnnk.common.base.basefragment.BaseFragment
import io.goldstone.blinnnk.common.component.AgreementView
import io.goldstone.blinnnk.common.component.button.RoundButton
import io.goldstone.blinnnk.common.component.button.roundButton
import io.goldstone.blinnnk.common.component.cell.RoundCell
import io.goldstone.blinnnk.common.component.cell.roundCell
import io.goldstone.blinnnk.common.component.edittext.RoundInput
import io.goldstone.blinnnk.common.component.edittext.WalletEditText
import io.goldstone.blinnnk.common.component.edittext.roundInput
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
import io.goldstone.blinnnk.crypto.multichain.ChainPath
import io.goldstone.blinnnk.crypto.multichain.DefaultPath
import io.goldstone.blinnnk.module.common.walletgeneration.createwallet.presenter.CreateWalletPresenter
import io.goldstone.blinnnk.module.common.walletimport.mnemonicimport.presenter.MnemonicImportDetailPresenter
import io.goldstone.blinnnk.module.common.walletimport.walletimport.view.WalletImportFragment
import io.goldstone.blinnnk.module.common.webview.view.WebViewFragment
import io.goldstone.blinnnk.module.entrance.splash.view.SplashActivity
import io.goldstone.blinnnk.module.home.home.view.MainActivity
import org.jetbrains.anko.*

/**
 * @date 23/03/2018 1:46 AM
 * @author KaySaith
 */
class MnemonicImportDetailFragment : BaseFragment<MnemonicImportDetailPresenter>() {
	override val pageTitle: String = ImportMethodText.mnemonic
	private lateinit var confirmButton: RoundButton
	private lateinit var walletNameInput: RoundInput
	private lateinit var pathSettings: RoundCell
	private lateinit var passwordInput: RoundInput
	private lateinit var repeatPassword: RoundInput
	private lateinit var hintInput: RoundInput
	private val mnemonicInput by lazy { WalletEditText(context!!) }
	private val agreementView by lazy { AgreementView(context!!) }
	override val presenter = MnemonicImportDetailPresenter(this)
	// Default Value
	private val pathInfo = arrayListOf(
		PathModel(ImportWalletText.customEthereumPath, DefaultPath.ethPathHeader, DefaultPath.default),
		PathModel(ImportWalletText.customEthereumClassicPath, DefaultPath.etcPathHeader, DefaultPath.default),
		PathModel(
			ImportWalletText.customBitcoinPath(),
			DefaultPath.btcPathHeader,
			DefaultPath.default
		),
		PathModel(
			ImportWalletText.customBTCTestPath(),
			DefaultPath.testPathHeader,
			DefaultPath.default
		),
		PathModel(ImportWalletText.customLitecoinPath, DefaultPath.ltcPathHeader, DefaultPath.default),
		PathModel(ImportWalletText.customBCHPath, DefaultPath.bchPathHeader, DefaultPath.default),
		PathModel(ImportWalletText.customEOSPath, DefaultPath.eosPathHeader, DefaultPath.default)
	)

	override fun AnkoContext<Fragment>.initView() {
		scrollView {
			lparams(matchParent, wrapContent)
			verticalLayout {
				gravity = Gravity.CENTER_HORIZONTAL
				lparams(matchParent, wrapContent)
				mnemonicInput.apply {
					hint = ImportWalletText.mnemonicHint
				}.into(this)
				mnemonicInput.setMargins<LinearLayout.LayoutParams> {
					topMargin = 30.uiPX()
				}

				pathSettings = roundCell {
					setTitles(ImportWalletText.path, ImportWalletText.defaultPath)
				}.click {
					showPatSettingsDashboard()
				}
				pathSettings.setMargins<LinearLayout.LayoutParams> {
					topMargin = 20.uiPX()
					bottomMargin = 10.uiPX()
				}

				walletNameInput = roundInput {
					horizontalPaddingSize = PaddingSize.gsCard
					hint = UIUtils.generateDefaultName()
					title = CreateWalletText.name
				}
				walletNameInput.setMargins<LinearLayout.LayoutParams> {
					topMargin = 15.uiPX()
				}

				passwordInput = roundInput {
					horizontalPaddingSize = PaddingSize.gsCard
					setPasswordInput()
					title = CreateWalletText.password
					setPasswordSafeLevel()
				}
				passwordInput.setMargins<LinearLayout.LayoutParams> {
					topMargin = 5.uiPX()
				}

				repeatPassword = roundInput {
					horizontalPaddingSize = PaddingSize.gsCard
					setPasswordInput()
					title = CreateWalletText.repeatPassword
				}
				repeatPassword.setMargins<LinearLayout.LayoutParams> {
					topMargin = 5.uiPX()
				}

				hintInput = roundInput {
					horizontalPaddingSize = PaddingSize.gsCard
					setTextInput()
					title = CreateWalletText.hint
				}
				hintInput.setMargins<LinearLayout.LayoutParams> {
					topMargin = 5.uiPX()
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
					presenter.importWalletByMnemonic(
						ChainPath(
							pathInfo[0].pathHeader + pathInfo[0].defaultPath,
							pathInfo[1].pathHeader + pathInfo[1].defaultPath,
							pathInfo[2].pathHeader + pathInfo[2].defaultPath,
							pathInfo[3].pathHeader + pathInfo[3].defaultPath,
							pathInfo[4].pathHeader + pathInfo[4].defaultPath,
							pathInfo[5].pathHeader + pathInfo[5].defaultPath,
							pathInfo[6].pathHeader + pathInfo[6].defaultPath
						),
						mnemonicInput.text.toString(),
						passwordInput.text.toString(),
						repeatPassword.text.toString(),
						hintInput.text.toString(),
						agreementView.radioButton.isChecked,
						walletNameInput.text.toString()
					) {
						launchUI {
							button.showLoadingStatus(false)
							if (it.hasError()) safeShowError(it)
							else activity?.jump<SplashActivity>()
						}
					}
				}

				ExplanationTitle(context).apply {
					text = QAText.whatIsMnemonic.setUnderline()
				}.click {
					getParentFragment<WalletImportFragment> {
						NetworkUtil.hasNetworkWithAlert(context) isTrue {
							presenter.showTargetFragment<WebViewFragment>(
								Bundle().apply {
									putString(ArgumentKey.webViewUrl, WebUrl.whatIsMnemonic)
									putString(ArgumentKey.webViewName, QAText.whatIsMnemonic)
								}
							)
						}
					}
				}.into(this)
			}
		}
	}

	private fun showPatSettingsDashboard() {
		MaterialDialog(context!!)
			.title(text = "Set BIP44 Path")
			.customListAdapter(PathAdapter(pathInfo))
			.positiveButton(text = CommonText.confirm)
			.negativeButton(text = CommonText.cancel)
			.show()
	}

	private fun RoundInput.setPasswordSafeLevel() {
		afterTextChanged = Runnable {
			CreateWalletPresenter.showPasswordSafeLevel(passwordInput)
		}
	}

	override fun setBaseBackEvent(activity: MainActivity?, parent: Fragment?) {
		getParentFragment<WalletImportFragment>()?.presenter
			?.popFragmentFrom<MnemonicImportDetailFragment>()
	}
}