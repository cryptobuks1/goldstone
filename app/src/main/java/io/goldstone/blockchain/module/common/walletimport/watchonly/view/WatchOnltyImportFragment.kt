package io.goldstone.blockchain.module.common.walletimport.watchonly.view

import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.*
import io.goldstone.blockchain.common.utils.UIUtils
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.value.*
import io.goldstone.blockchain.module.common.walletimport.walletimport.view.WalletImportFragment
import io.goldstone.blockchain.module.common.walletimport.watchonly.presenter.WatchOnlyImportPresenter
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.verticalLayout

/**
 * @date 23/03/2018 2:15 AM
 * @author KaySaith
 */

class WatchOnlyImportFragment : BaseFragment<WatchOnlyImportPresenter>() {

	private val attentionView by lazy { AttentionTextView(context!!) }

	private val nameInput by lazy { RoundInput(context!!) }
	private val addressInput by lazy { WalletEditText(context!!) }
	private val confirmButton by lazy { RoundButton(context!!) }

	override val presenter = WatchOnlyImportPresenter(this)

	override fun AnkoContext<Fragment>.initView() {

		verticalLayout {
			gravity = Gravity.CENTER_HORIZONTAL
			lparams(matchParent, matchParent)

			(attentionView.parent as? ViewGroup)?.apply {
				findViewById<AttentionTextView>(ElementID.attentionText).isNotNull {
					/** 临时解决异常的 `The specified child already has a parent` 错误 */
					removeAllViews()
				}
			}

			attentionView.apply {
				setMargins<LinearLayout.LayoutParams> { topMargin = 80.uiPX() }
				text = WatchOnlyText.intro
			}.into(this)


			nameInput.apply {
				hint = UIUtils.generateDefaultName()
				setMargins<LinearLayout.LayoutParams> { topMargin = 30.uiPX() }
				title = CreateWalletText.name
			}.into(this)

			addressInput.apply {
				setMargins<LinearLayout.LayoutParams> { topMargin = 30.uiPX() }
				hint = WatchOnlyText.enterDescription
			}.into(this)

			confirmButton.apply {
				marginTop = 20.uiPX()
				setBlueStyle()
				text = CommonText.startImporting.toUpperCase()
			}.click {
				it.showLoadingStatus()
				presenter.importWatchOnlyWallet(addressInput, nameInput) {
					it.showLoadingStatus(false)
				}
			}.into(this)
			
			ExplanationTitle(context).apply {
				text = QAText.whatIsWatchOnlyWallet.setUnderline()
			}.click {
				getParentFragment<WalletImportFragment> {
					presenter.showWebViewFragment(WebUrl.whatIsWatchOnly, QAText.whatIsWatchOnlyWallet)
				}
			}.into(this)
		}
	}

}