package io.goldstone.blinnnk.module.common.walletgeneration.mnemonicconfirmation.presenter

import com.blinnnk.extension.getParentFragment
import com.blinnnk.extension.isTrue
import com.blinnnk.extension.jump
import com.blinnnk.extension.otherwise
import io.goldstone.blinnnk.common.base.basefragment.BasePresenter
import io.goldstone.blinnnk.common.base.baseoverlayfragment.BaseOverlayFragment
import io.goldstone.blinnnk.common.component.overlay.GoldStoneDialog
import io.goldstone.blinnnk.common.language.CreateWalletText
import io.goldstone.blinnnk.common.language.ImportWalletText
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.utils.alert
import io.goldstone.blinnnk.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blinnnk.module.common.walletgeneration.mnemonicconfirmation.view.MnemonicConfirmationFragment
import io.goldstone.blinnnk.module.common.walletgeneration.walletgeneration.view.WalletGenerationFragment
import io.goldstone.blinnnk.module.entrance.splash.view.SplashActivity
import io.goldstone.blinnnk.module.home.home.view.MainActivity
import io.goldstone.blinnnk.module.home.wallet.walletsettings.walletsettings.view.WalletSettingsFragment

/**
 * @date 22/03/2018 11:40 PM
 * @author KaySaith
 */
class MnemonicConfirmationPresenter(
	override val fragment: MnemonicConfirmationFragment
) : BasePresenter<MnemonicConfirmationFragment>() {

	fun clickConfirmationButton(correct: String, current: String) {
		compareMnemonicCode(correct, current) isTrue {
			WalletTable.updateHasBackupMnemonic {
				validAndContinue()
				SharedWallet.updateBackUpMnemonicStatus(true)
			}
		} otherwise {
			fragment.context?.alert(ImportWalletText.mnemonicAlert)
		}
	}

	override fun onFragmentViewCreated() {
		// 如果在窗前钱包的界面用户点击了关闭按钮那么直接切换钱包
		if (fragment.activity is MainActivity) {
			fragment.getParentFragment<WalletGenerationFragment> {
				showCloseButton(true) {
					activity?.jump<SplashActivity>()
				}
			}
		}
	}

	private fun compareMnemonicCode(correct: String, current: String): Boolean {
		return correct.equals(current, true)
	}

	private fun validAndContinue() {
		val currentActivity = fragment.activity
		when (currentActivity) {
			is MainActivity -> {
				fragment.getParentFragment<WalletSettingsFragment> {
					presenter.removeSelfFromActivity()
					GoldStoneDialog(currentActivity).showBackUpSucceed()
				}

				fragment.getParentFragment<WalletGenerationFragment> {
					presenter.removeSelfFromActivity()
					fragment.activity?.jump<SplashActivity>()
				}
			}

			is SplashActivity -> {
				fragment.activity?.jump<SplashActivity>()
			}
		}
	}

	override fun onFragmentShowFromHidden() {
		fragment.parentFragment.apply {
			fun BaseOverlayFragment<*>.resetEvent() {
				headerTitle = CreateWalletText.mnemonicConfirmation
				showCloseButton(false) {}
				showBackButton(true) {
					presenter.popFragmentFrom<MnemonicConfirmationFragment>()
				}
			}

			when (this) {
				is WalletGenerationFragment -> resetEvent()
				is WalletSettingsFragment -> resetEvent()
			}
		}
	}
}