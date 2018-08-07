package io.goldstone.blockchain.module.home.wallet.walletsettings.privatekeyexport.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.blinnnk.extension.addCorner
import com.blinnnk.extension.getParentFragment
import com.blinnnk.extension.into
import com.blinnnk.extension.setMargins
import com.blinnnk.uikit.ScreenSize
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.clickToCopy
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.base.baseoverlayfragment.BaseOverlayFragment
import io.goldstone.blockchain.common.component.AttentionTextView
import io.goldstone.blockchain.common.component.RoundButton
import io.goldstone.blockchain.common.component.RoundInput
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.value.*
import io.goldstone.blockchain.module.home.home.view.MainActivity
import io.goldstone.blockchain.module.home.wallet.walletsettings.privatekeyexport.presenter.PrivateKeyExportPresenter
import io.goldstone.blockchain.module.home.wallet.walletsettings.walletsettings.view.WalletSettingsFragment
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.onUiThread
import org.jetbrains.anko.textColor
import org.jetbrains.anko.verticalLayout

/**
 * @date 06/04/2018 1:02 AM
 * @author KaySaith
 */
class PrivateKeyExportFragment : BaseFragment<PrivateKeyExportPresenter>() {
	
	private val attentionView by lazy { AttentionTextView(context!!) }
	private val privateKeyTextView by lazy { TextView(context) }
	private val passwordInput by lazy { RoundInput(context!!) }
	private val confirmButton by lazy { RoundButton(context!!) }
	override val presenter = PrivateKeyExportPresenter(this)
	
	override fun AnkoContext<Fragment>.initView() {
		verticalLayout {
			gravity = Gravity.CENTER_HORIZONTAL
			lparams(matchParent, matchParent)
			attentionView.apply {
				setMargins<LinearLayout.LayoutParams> { topMargin = 30.uiPX() }
				text = ImportWalletText.exportPrivateKey
			}.into(this)
			privateKeyTextView.apply {
				addCorner(CornerSize.default.toInt(), GrayScale.whiteGray)
				layoutParams = LinearLayout.LayoutParams(
					ScreenSize.Width - PaddingSize.device * 2, 120.uiPX()
				).apply {
					topMargin = 20.uiPX()
					setPadding(20.uiPX(), 16.uiPX(), 20.uiPX(), 10.uiPX())
				}
				gravity = Gravity.CENTER_VERTICAL
				textSize = fontSize(15)
				textColor = GrayScale.black
				typeface = GoldStoneFont.heavy(context)
			}.click {
				// 如果 `textview` 的内容不是默认的 `placeholder` 就可以支持点击复制
				if (it.text.isNotEmpty()) {
					context.clickToCopy(privateKeyTextView.text.toString())
				}
			}.into(this)
			
			passwordInput.apply {
				setPasswordInput()
				setMargins<LinearLayout.LayoutParams> {
					topMargin = 30.uiPX()
				}
				title = CreateWalletText.password
			}.into(this)
			
			confirmButton.apply {
				text = CommonText.confirm.toUpperCase()
				setBlueStyle()
				setMargins<LinearLayout.LayoutParams> {
					topMargin = 15.uiPX()
				}
			}.click { it ->
				it.showLoadingStatus()
				presenter.getPrivateKeyByAddress(passwordInput.text.toString()) {
					onUiThread {
						this?.let { privateKeyTextView.text = it }
						it.showLoadingStatus(false)
					}
				}
			}.into(this)
		}
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		parentFragment?.let {
			if (it is BaseOverlayFragment<*>) {
				it.showAddButton(false)
			}
		}
	}
	
	override fun setBaseBackEvent(
		activity: MainActivity?,
		parent: Fragment?
	) {
		getParentFragment<WalletSettingsFragment> {
			headerTitle = WalletSettingsText.viewAddresses
			presenter.popFragmentFrom<PrivateKeyExportFragment>()
		}
	}
}
