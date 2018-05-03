package io.goldstone.blockchain.module.home.profile.pincode.view

import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.blinnnk.component.HoneyBaseSwitch
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.RoundButton
import io.goldstone.blockchain.common.component.RoundInput
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.value.*
import io.goldstone.blockchain.common.value.ScreenSize
import io.goldstone.blockchain.kernel.commonmodel.AppConfigTable
import io.goldstone.blockchain.module.home.profile.pincode.presenter.PinCodeEditorPresenter
import org.jetbrains.anko.*

/**
 * @date 23/04/2018 2:34 PM
 * @author KaySaith
 */

class PinCodeEditorFragment : BaseFragment<PinCodeEditorPresenter>() {

	private val newPinCode by lazy { RoundInput(context!!) }
	private val repeatPinCode by lazy { RoundInput(context!!) }
	val confirmButton by lazy { RoundButton(context!!) }
	private val switch by lazy { HoneyBaseSwitch(context!!) }

	override val presenter = PinCodeEditorPresenter(this)

	override fun AnkoContext<Fragment>.initView() {

		verticalLayout {
			lparams(matchParent, matchParent)
			AppConfigTable.getAppConfig {
				it?.pincode.isNotNull {
					presenter.showPinCodeFragment()
				}

				initSwitchCell()

				textView {
					text = PincodeText.description
					textSize = 5.uiPX().toFloat()
					textColor = GrayScale.midGray
					typeface = GoldStoneFont.medium(context)
					gravity = Gravity.CENTER
					layoutParams = LinearLayout.LayoutParams(matchParent, 30.uiPX())
					y += 20.uiPX()
				}

				newPinCode.apply {
					text = PincodeText.pincode
					setPinCodeInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 40.uiPX() }
				}.into(this)

				repeatPinCode.apply {
					text = PincodeText.repeat
					setPinCodeInput()
					setMargins<LinearLayout.LayoutParams> { topMargin = 10.uiPX() }
				}.into(this)

				confirmButton.apply {
					text = CommonText.confirm
					setBlueStyle()
					setMargins<LinearLayout.LayoutParams> { topMargin = 15.uiPX() }
				}.click {
					presenter.resetPinCode(newPinCode, repeatPinCode, switch)
				}.into(this)
			}
		}
	}

	private fun ViewGroup.initSwitchCell() {
		relativeLayout {

			lparams {
				width = ScreenSize.widthWithPadding
				height = 80.uiPX()
				leftMargin = PaddingSize.device
			}

			textView(PincodeText.show).apply {
				textSize = 5.uiPX().toFloat()
				textColor = GrayScale.midGray
				typeface = GoldStoneFont.heavy(context)
				gravity = Gravity.CENTER_VERTICAL
				lparams(matchParent, matchParent)
			}

			AppConfigTable.getAppConfig { config ->
				switch.apply {
					setAlignParentRight()
					isChecked = config?.showPincode.orFalse()
				}.click { switch ->
					// 点击后根据更新的数据库情况显示开关状态
					presenter.setShowPinCodeStatus(switch.isChecked) {
						AppConfigTable.getAppConfig {
							switch.isChecked = it?.showPincode.orFalse()
						}
					}
				}.into(this)
			}

			// 分割线
			View(context).apply {
				lparams {
					width = matchParent
					height = BorderSize.default.toInt()
					alignParentBottom()
				}
				backgroundColor = GrayScale.lightGray
			}.into(this)
		}
	}

}