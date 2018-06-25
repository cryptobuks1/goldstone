package io.goldstone.blockchain.module.home.profile.profileoverlay.presenter

import com.blinnnk.extension.findChildFragmentByTag
import com.blinnnk.util.addFragmentAndSetArgument
import io.goldstone.blockchain.common.base.baseoverlayfragment.BaseOverlayPresenter
import io.goldstone.blockchain.common.utils.getMainActivity
import io.goldstone.blockchain.common.value.*
import io.goldstone.blockchain.module.common.webview.view.WebViewFragment
import io.goldstone.blockchain.module.home.profile.chain.chainselection.view.ChainSelectionFragment
import io.goldstone.blockchain.module.home.profile.contacts.contractinput.view.ContactInputFragment
import io.goldstone.blockchain.module.home.profile.contacts.contracts.view.ContactFragment
import io.goldstone.blockchain.module.home.profile.currency.view.CurrencyFragment
import io.goldstone.blockchain.module.home.profile.lanaguage.view.LanguageFragment
import io.goldstone.blockchain.module.home.profile.pincode.view.PinCodeEditorFragment
import io.goldstone.blockchain.module.home.profile.profile.view.ProfileFragment
import io.goldstone.blockchain.module.home.profile.profileoverlay.view.ProfileOverlayFragment

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
		showTargetFragment<ContactInputFragment>(ProfileText.contactsInput, ProfileText.contacts)
	}
	
	fun showTargetFragmentByTitle(title: String) {
		when (title) {
			ProfileText.contacts -> showContactsFragment()
			ProfileText.contactsInput -> showContactInput()
			ProfileText.currency -> showCurrencyFragment()
			ProfileText.language -> showLanguageFragment()
			ProfileText.pinCode -> showPinCodeEditorFragment()
			ProfileText.chain -> showChainSelectionFragment()
			ProfileText.privacy -> showPrivacyFragment()
			ProfileText.terms -> showTermsFragment()
			ProfileText.support -> showSupportFragment()
			ProfileText.aboutUs -> showAboutFragment()
			ProfileText.helpCenter -> showHelpCenterFragment()
		}
	}
	
	private fun showAboutFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.aboutUs)
		}
	}
	
	private fun showPrivacyFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.privacy)
		}
	}
	
	private fun showTermsFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.terms)
		}
	}
	
	private fun showSupportFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.support)
		}
	}
	
	private fun showHelpCenterFragment() {
		fragment.addFragmentAndSetArgument<WebViewFragment>(ContainerID.content) {
			putString(ArgumentKey.webViewUrl, WebUrl.helpCenter)
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