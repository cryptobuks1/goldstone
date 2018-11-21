package io.goldstone.blockchain.module.common.tokenpayment.gaseditor.view

import android.support.v4.app.Fragment
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.button.RoundButton
import io.goldstone.blockchain.common.component.edittext.RoundInput
import io.goldstone.blockchain.common.error.TransferError
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.language.TokenDetailText
import io.goldstone.blockchain.common.language.TransactionText
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.utils.safeShowError
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blockchain.module.common.tokenpayment.gaseditor.presenter.GasEditorPresenter
import io.goldstone.blockchain.module.home.home.view.MainActivity
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.verticalLayout

/**
 * @date 2018/5/8 3:22 PM
 * @author KaySaith
 */
class GasEditorFragment : BaseFragment<GasEditorPresenter>() {

	override val pageTitle: String = TokenDetailText.customGas
	val getGasSize: () -> Long? = {
		arguments?.getLong(ArgumentKey.gasSize)
	}
	val isBTCSeries by lazy {
		arguments?.getBoolean(ArgumentKey.isBTCSeries).orFalse()
	}
	private val gasPriceInput by lazy { RoundInput(context!!) }
	private val gasLimitInput by lazy { RoundInput(context!!) }
	private val confirmButton by lazy { RoundButton(context!!) }
	private val speedLevelBar by lazy { GasSpeedLevelBar(context!!) }
	override val presenter = GasEditorPresenter(this)

	override fun AnkoContext<Fragment>.initView() {
		verticalLayout {
			gravity = Gravity.CENTER_HORIZONTAL
			lparams(matchParent, matchParent)
			gasPriceInput.apply {
				setNumberInput(false)
				setMargins<LinearLayout.LayoutParams> { topMargin = 50.uiPX() }
				title = if (isBTCSeries) TransactionText.satoshiValue else TransactionText.gasPrice
			}.into(this)
			// 只有 `ETH ERC20 or ETC` 才有 `GasLimit` 的概念
			if (!isBTCSeries) {
				gasLimitInput.apply {
					setNumberInput(false)
					setText(getGasSize().toString())
					setMargins<LinearLayout.LayoutParams> { topMargin = 15.uiPX() }
					title = TransactionText.gasLimit
				}.into(this)
			}

			speedLevelBar.apply {
				setMargins<RelativeLayout.LayoutParams> { topMargin = 30.uiPX() }
			}.into(this)

			confirmButton.apply {
				text = CommonText.confirm
				setBlueStyle(20.uiPX())
			}.click {
				presenter.confirmGasCustom(gasPrice, dataSize)
			}.into(this)
			setProcessValue()
		}
	}

	override fun onStart() {
		super.onStart()
		val defaultPrice =
			if (isBTCSeries) 50L else 30L
		gasPriceInput.setText(defaultPrice.toString())
		gasPrice = defaultPrice
		getGasSize()?.let { dataSize = it }
	}

	private val currentValue: (gasPrice: Long, gasSize: Long) -> Double = { gasPrice, gasSize ->
		val fast = 100 * gasSize
		(gasPrice * gasSize) / fast.toDouble()
	}
	private var gasPrice: Long by observing(0L) {
		speedLevelBar.setProgressValue(currentValue(gasPrice, dataSize))
	}
	private var dataSize: Long by observing(0L) {
		speedLevelBar.setProgressValue(currentValue(gasPrice, dataSize))
	}

	// 第三方键盘无法被设置为纯数字输入, 这里做额外的检测,
	// 保证输入的值是数字格式.
	private var hasShowError = false
	private fun setProcessValue() {
		with(gasPriceInput) {
			afterTextChanged = Runnable {
				checkNumberValue(false) {
					// 因为 `Input` 是 `String` 格式输入, 用户可能输入超过 `21` 个 `0` 导致转 `Long` 类型出问题
					// 用 `Try Catch` 捕捉
					gasPrice = if (getContent().isEmpty()) 0L else try {
						getContent().toLong()
					} catch (error: Exception) {
						if (!hasShowError) {
							safeShowError(Throwable(TransferError.InvalidBigNumber))
							hasShowError = true
						}
						0L
					}
				}
			}
		}
		if (isBTCSeries) {
			dataSize = getGasSize().orElse(0L)
		} else {
			with(gasLimitInput) {
				afterTextChanged = Runnable {
					checkNumberValue(false) {
						dataSize = if (getContent().isEmpty()) getGasSize() ?: 0L else getContent().toLong()
					}
				}
			}
		}
	}

	override fun setBaseBackEvent(activity: MainActivity?, parent: Fragment?) {
		getParentFragment<TokenDetailOverlayFragment> {
			headerTitle = TokenDetailText.customGas
			presenter.popFragmentFrom<GasEditorFragment>()
		}
	}
}