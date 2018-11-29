package io.goldstone.blockchain.module.home.profile.profileoverlay.presenter

import com.blinnnk.extension.addFragment
import com.blinnnk.extension.findChildFragmentByTag
import com.blinnnk.util.addFragmentAndSetArgument
import com.blinnnk.util.replaceFragmentAndSetArgument
import io.goldstone.blockchain.common.base.baseoverlayfragment.BaseOverlayPresenter
import io.goldstone.blockchain.common.component.overlay.Dashboard
import io.goldstone.blockchain.common.language.CreateWalletText
import io.goldstone.blockchain.common.language.ImportWalletText
import io.goldstone.blockchain.common.language.ProfileText
import io.goldstone.blockchain.common.language.WalletSettingsText
import io.goldstone.blockchain.common.utils.getMainActivity
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.ContainerID
import io.goldstone.blockchain.common.value.FragmentTag
import io.goldstone.blockchain.common.value.WebUrl
import io.goldstone.blockchain.module.common.walletgeneration.walletgeneration.view.WalletGenerationFragment
import io.goldstone.blockchain.module.common.walletimport.walletimport.view.WalletImportFragment
import io.goldstone.blockchain.module.common.walletimport.watchonly.view.WatchOnlyImportFragment
import io.goldstone.blockchain.module.common.webview.view.WebViewFragment
import io.goldstone.blockchain.module.home.dapp.eosaccountregister.view.EOSAccountRegisterFragment
import io.goldstone.blockchain.module.home.profile.chain.chainselection.view.ChainSelectionFragment
import io.goldstone.blockchain.module.home.profile.contacts.contractinput.view.ContactInputFragment
import io.goldstone.blockchain.module.home.profile.contacts.contracts.view.ContactFragment
import io.goldstone.blockchain.module.home.profile.currency.view.CurrencyFragment
import io.goldstone.blockchain.module.home.profile.lanaguage.view.LanguageFragment
import io.goldstone.blockchain.module.home.profile.pincode.view.PinCodeEditorFragment
import io.goldstone.blockchain.module.home.profile.profile.view.ProfileFragment
import io.goldstone.blockchain.module.home.profile.profileoverlay.view.ProfileOverlayFragment
import io.goldstone.blockchain.module.home.wallet.walletmanagement.walletlist.view.WalletListFragment
import io.goldstone.blockchain.module.home.wallet.walletsettings.addressmanager.model.GridIconTitleModel
import io.goldstone.blockchain.module.home.wallet.walletsettings.addressmanager.view.GridIconTitleAdapter

/**
 * @date 26/03/2018 12:56 AM
 * @author KaySaith
 */
class ProfileOverlayPresenter(
	override val fragment: ProfileOverlayFragment
) : BaseOverlayPresenter<ProfileOverlayFragment>() {

	override fun removeSelfFromActivity() {
		super.removeSelfFromActivity()
		fragment.getMainActivity()?.getHomeFragment()?.apply {
			findChildFragmentByTag<ProfileFragment>(FragmentTag.profile)?.presenter?.updateData()
		}
	}

	fun showContactInputFragment() {
		showTargetFragment<ContactInputFragment>()
	}

	fun showTargetFragmentByTitle(title: String) {
		when (title) {
			ProfileText.contacts -> showContactsFragment()
			ProfileText.contactsInput -> showContactInput()
			ProfileText.currency -> showCurrencyFragment()
			ProfileText.language -> showLanguageFragment()
			ProfileText.eosAccountRegister -> showEOSAccountRegisterFragment()
			ProfileText.pinCode -> showPinCodeEditorFragment()
			ProfileText.walletManager -> showWalletListFragment()
			ProfileText.chain -> showChainSelectionFragment()
			ProfileText.privacy -> showPrivacyFragment()
			ProfileText.terms -> showTermsFragment()
			ProfileText.support -> showSupportFragment()
			ProfileText.aboutUs -> showAboutFragment()
			ProfileText.helpCenter -> showHelpCenterFragment()
		}
	}

	private fun showWalletListFragment() {
		fragment.apply {
			showAddButton(true, true) {
				showWalletAddingMethodDashboard()
			}
			replaceFragmentAndSetArgument<WalletListFragment>(ContainerID.content)
		}
	}

	fun showWalletAddingMethodDashboard() {
		Dashboard(fragment.context!!) {
			showGrid(
				ProfileText.walletManager,
				GridIconTitleAdapter(GridIconTitleModel.getWalletManagementMenu()) {
					when (it.name) {
						CreateWalletText.create -> showCreateWalletFragment()
						ImportWalletText.importWallet -> showImportWalletFragment()
						else -> showWatchWalletImportFragment()
					}
					dismiss()
				}
			)
		}
	}

	private fun showWatchWalletImportFragment() {
		showTargetFragment<WatchOnlyImportFragment>()
	}

	private fun showAboutFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.aboutUs)
			putString(ArgumentKey.webViewName, ProfileText.aboutUs)
		}
	}

	private fun showEOSAccountRegisterFragment() {
		fragment.addFragmentAndSetArgument<EOSAccountRegisterFragment>(ContainerID.content)
	}

	private fun showImportWalletFragment() {
		// 防止重回展开的时候, 隐藏 ProfileOverlayFragment
		fragment.activity?.apply {
			supportFragmentManager.beginTransaction().hide(fragment).commit()
			addFragment<WalletImportFragment>(ContainerID.main)
		}
	}

	private fun showCreateWalletFragment() {
		// 防止重回展开的时候, 隐藏 ProfileOverlayFragment
		fragment.activity?.apply {
			supportFragmentManager.beginTransaction().hide(fragment).commit()
			addFragment<WalletGenerationFragment>(ContainerID.main)
		}
	}

	private fun showPrivacyFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.privacy)
			putString(ArgumentKey.webViewName, ProfileText.privacy)
		}
	}

	private fun showTermsFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.terms)
			putString(ArgumentKey.webViewName, ProfileText.terms)
		}
	}

	private fun showSupportFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.support)
			putString(ArgumentKey.webViewName, ProfileText.support)
		}
	}

	private fun showHelpCenterFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.helpCenter)
			putString(ArgumentKey.webViewName, ProfileText.helpCenter)
		}
	}

	private fun showChainSelectionFragment() {
		fragment.addFragmentAndSetArgument<ChainSelectionFragment>(ContainerID.content)
	}

	private fun showPinCodeEditorFragment() {
		fragment.addFragmentAndSetArgument<PinCodeEditorFragment>(ContainerID.content)
	}

	private fun showContactsFragment() {
		fragment.addFragmentAndSetArgument<ContactFragment>(ContainerID.content)
	}

	private fun showContactInput() {
		fragment.addFragmentAndSetArgument<ContactInputFragment>(ContainerID.content)
	}

	private fun showCurrencyFragment() {
		fragment.addFragmentAndSetArgument<CurrencyFragment>(ContainerID.content)
	}

	private fun showLanguageFragment() {
		fragment.addFragmentAndSetArgument<LanguageFragment>(ContainerID.content)
	}
}