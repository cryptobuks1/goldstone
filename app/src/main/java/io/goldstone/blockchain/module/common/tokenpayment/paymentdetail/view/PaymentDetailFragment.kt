package io.goldstone.blockchain.module.common.tokenpayment.paymentdetail.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blinnnk.animation.updateAlphaAnimation
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.getParentFragment
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.button.RoundButton
import io.goldstone.blockchain.common.component.cell.GraySquareCell
import io.goldstone.blockchain.common.component.cell.TopBottomLineCell
import io.goldstone.blockchain.common.component.edittext.ValueInputView
import io.goldstone.blockchain.common.component.edittext.WalletEditText
import io.goldstone.blockchain.common.component.overlay.DashboardOverlay
import io.goldstone.blockchain.common.component.title.TwoLineTitles
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.language.PrepareTransferText
import io.goldstone.blockchain.common.language.TokenDetailText
import io.goldstone.blockchain.common.sharedpreference.SharedWallet
import io.goldstone.blockchain.common.thread.launchUI
import io.goldstone.blockchain.common.utils.alert
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.utils.safeShowError
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.ElementID
import io.goldstone.blockchain.common.value.PaddingSize
import io.goldstone.blockchain.common.value.ScreenSize
import io.goldstone.blockchain.crypto.multichain.*
import io.goldstone.blockchain.crypto.utils.CryptoUtils
import io.goldstone.blockchain.crypto.utils.formatCurrency
import io.goldstone.blockchain.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blockchain.module.common.tokenpayment.paymentdetail.presenter.PaymentDetailPresenter
import io.goldstone.blockchain.module.common.tokenpayment.paymentdetail.presenter.isValidAddressOrElse
import io.goldstone.blockchain.module.common.tokenpayment.paymentdetail.presenter.isValidLTCAddressOrElse
import io.goldstone.blockchain.module.home.home.view.MainActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import kotlin.apply

/**
 * @date 2018/5/15 10:18 PM
 * @author KaySaith
 */
class PaymentDetailFragment : BaseFragment<PaymentDetailPresenter>() {

	override val pageTitle: String = TokenDetailText.transferDetail
	val address by lazy {
		arguments?.getString(ArgumentKey.paymentAddress)
	}
	val count by lazy {
		arguments?.getDouble(ArgumentKey.paymentCount).orElse(0.0)
	}
	val rootFragment by lazy {
		getParentFragment<TokenDetailOverlayFragment>()
	}
	private val inputView by lazy { ValueInputView(context!!) }
	private val sendInfo by lazy { GraySquareCell(context!!) }
	private val from by lazy { GraySquareCell(context!!) }
	private val memo by lazy { GraySquareCell(context!!) }
	private val customChangeAddressCell by lazy { GraySquareCell(context!!) }
	private val price by lazy { GraySquareCell(context!!) }
	private val confirmButton by lazy { RoundButton(context!!) }
	private var memoInputView: MemoInputView? = null
	private var memoData: String = ""
	private lateinit var changeAddress: String
	override val presenter = PaymentDetailPresenter(this)

	override fun AnkoContext<Fragment>.initView() {
		changeAddress = rootFragment?.token?.contract.getAddress()
		scrollView {
			isVerticalScrollBarEnabled = false
			lparams(matchParent, matchParent)
			verticalLayout {
				gravity = Gravity.CENTER_HORIZONTAL
				lparams(matchParent, matchParent)

				inputView.into(this)

				showAccountInfo()
				// `BTCSeries` 于 ETH, ERC20, ETC 显示不同的配置信息
				if (rootFragment?.token?.symbol.isBTCSeries())
					showCustomChangeAddressCell()
				else showMemoCell()

				showUnitPrice()

				confirmButton.apply {
					setGrayStyle(20.uiPX())
					text = CommonText.next
				}.click { button ->
					button.showLoadingStatus()
					presenter.goToGasEditorFragmentOrTransfer {
						if (it.hasError()) safeShowError(it)
						launchUI {
							button.showLoadingStatus(false)
						}
					}
				}.into(this)
				// 扫描二维码进入后的样式判断
				if (count > 0) {
					inputView.setInputValue(count)
					confirmButton.setBlueStyle(20.uiPX())
				}
			}
		}
	}

	private fun setCustomHeaderTitle(status: Boolean) {
		val token = rootFragment?.token
		getParentFragment<TokenDetailOverlayFragment> {
			if (status) customHeader = {
				val titles = TwoLineTitles(context).apply {
					id = ElementID.customHeader
					isCenter = true
					layoutParams = RelativeLayout.LayoutParams(matchParent, wrapContent)
					this.title.text = token?.symbol?.symbol.orEmpty()
					this.subtitle.text = "${token?.count}" suffix token?.contract.getSymbol().symbol.orEmpty()
					setBoldTitles()
				}
				titles.into(this)
				titles.setCenterInParent()
			} else {
				recoveryOverlayHeader()
			}

		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setSymbolAndPrice()
		setCustomHeaderTitle(true)
		updateValueTotalPrice()
		resetBackButtonEvent()
	}

	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		setCustomHeaderTitle(!hidden)
	}

	override fun onDetach() {
		super.onDetach()
		setCustomHeaderTitle(false)
	}

	override fun onResume() {
		super.onResume()
		inputView.setFoucs()
	}

	fun getMemoContent(): String {
		return memoData
	}

	fun getTransferCount(): Double {
		return if (inputView.getValue().isEmpty()) 0.0 else inputView.getValue().toDouble()
	}

	fun updateChangeAddress(address: String) {
		customChangeAddressCell.setSubtitle(address)
	}

	fun getChangeAddress(): String {
		return changeAddress
	}

	override fun setBaseBackEvent(activity: MainActivity?, parent: Fragment?) {
		backEvent()
	}

	fun backEvent() {
		if (rootFragment?.token?.contract.isBTCSeries()) {
			val overlay =
				getParentContainer()?.findViewById<DashboardOverlay>(ElementID.dashboardOverlay)
			if (overlay != null) getParentContainer()?.removeView(overlay)
			else getParentFragment<TokenDetailOverlayFragment>()?.apply {
				presenter.popFragmentFrom<PaymentDetailFragment>()
			}
		} else {
			if (memoInputView.isNull()) {
				getParentFragment<TokenDetailOverlayFragment>()?.apply {
					presenter.popFragmentFrom<PaymentDetailFragment>()
				}
			} else removeMemoInputView()
		}
	}

	private fun setSymbolAndPrice() {
		val token = rootFragment?.token
		this.inputView.setHeaderSymbol(token?.symbol?.symbol.orEmpty())
		this.price.setSubtitle(token?.price?.formatCurrency().orEmpty() suffix SharedWallet.getCurrencyCode())
	}

	private fun LinearLayout.showMemoCell() {
		TopBottomLineCell(context).apply {
			layoutParams = LinearLayout.LayoutParams(matchParent, 100.uiPX())
			setHorizontalPadding(PaddingSize.device.toFloat())
			setTitle(PrepareTransferText.memoInformation)
			memo.apply {
				setTitle(PrepareTransferText.memo)
				setSubtitle(PrepareTransferText.addAMemo.scaleTo(32))
				showArrow()
			}.click {
				getParentContainer()?.showMemoInputView { content ->
					if (content.isNotEmpty()) {
						memoData = content
						memo.setSubtitle(content)
					} else {
						memo.setSubtitle(PrepareTransferText.addAMemo)
					}
				}
			}.into(this)
		}.into(this)
	}

	private fun LinearLayout.showCustomChangeAddressCell() {
		TopBottomLineCell(context).apply {
			layoutParams = LinearLayout.LayoutParams(matchParent, 100.uiPX())
			setHorizontalPadding(PaddingSize.device.toFloat())
			setTitle(PrepareTransferText.customChangeAddress)
			customChangeAddressCell.apply {
				setTitle(PrepareTransferText.changeAddress)
				setSubtitle(changeAddress.scaleTo(16))
				showArrow()
			}.click {
				showCustomChangeAddressOverlay()
			}.into(this)
		}.into(this)
	}

	private fun showCustomChangeAddressOverlay() {
		getParentContainer()?.apply {
			val addressInput = WalletEditText(context)
			DashboardOverlay(context) {
				addressInput.apply {
					setMargins<LinearLayout.LayoutParams> {
						width = ScreenSize.widthWithPadding - 40.uiPX()
						topMargin = 10.uiPX()
					}
					hint = changeAddress
				}
				addressInput.into(this)
			}.apply {
				confirmEvent = Runnable {
					val customAddress = addressInput.text?.toString().orEmpty()
					when {
						presenter.getToken()?.contract.isBTC() || presenter.getToken()?.contract.isBCH() ->
							presenter.isValidAddressOrElse(customAddress) isTrue {
								// 更新默认的自定义找零地址
								changeAddress = customAddress
							}
						presenter.getToken()?.contract.isLTC() ->
							presenter.isValidLTCAddressOrElse(customAddress) isTrue {
								// 更新默认的自定义找零地址
								changeAddress = customAddress
							}
					}
				}
			}.showTitle(PrepareTransferText.customChangeAddress).into(this)
		}
	}

	private fun LinearLayout.showAccountInfo() {
		TopBottomLineCell(context).apply {
			layoutParams =
				LinearLayout.LayoutParams(matchParent, 150.uiPX()).apply { topMargin = 10.uiPX() }
			setHorizontalPadding(PaddingSize.device.toFloat())
			setTitle(PrepareTransferText.accountInfo)

			sendInfo.apply {
				setTitle(PrepareTransferText.send)
				setSubtitle(CryptoUtils.scaleMiddleAddress(address.orEmpty()))
			}.into(this)

			from.apply {
				setTitle(PrepareTransferText.from)
			}.into(this)

			setFromAddress()
		}.into(this)
	}

	private fun LinearLayout.showUnitPrice() {
		TopBottomLineCell(context).apply {
			layoutParams = LinearLayout.LayoutParams(matchParent, 100.uiPX())
			setHorizontalPadding(PaddingSize.device.toFloat())
			setTitle(PrepareTransferText.currentPrice)
			price.apply {
				setTitle(PrepareTransferText.price)
			}.into(this)
		}.into(this)
	}

	private fun setFromAddress() {
		from.setSubtitle(CryptoUtils.scaleMiddleAddress(presenter.getToken()?.contract.getAddress()))
	}

	private fun ViewGroup.showMemoInputView(hold: (String) -> Unit) {
		val isEOSTransfer = rootFragment?.token?.contract.isEOSSeries()
		if (memoInputView.isNull()) {
			// 禁止上下滚动
			memoInputView = MemoInputView(context).apply {
				updateConfirmButtonEvent { button ->
					button.onClick {
						if (isValidMemoByChain(isEOSTransfer)) {
							removeMemoInputView()
							hold(getMemoContent())
						} else {
							this@apply.context.alert(PrepareTransferText.invalidEOSMemoSize)
						}
						button.preventDuplicateClicks()
					}
				}
			}
			memoInputView?.into(this)
		}
	}

	private fun removeMemoInputView() {
		memoInputView?.updateAlphaAnimation(0f) {
			getParentContainer()?.removeView(memoInputView)
			memoInputView = null
		}
	}

	private fun updateValueTotalPrice() {
		val price = rootFragment?.token?.price ?: 0.0
		inputView.inputTextListener {
			inputView.updateCurrencyValue(price)
			if (it.isNotEmpty()) {
				confirmButton.setBlueStyle(20.uiPX())
			} else {
				confirmButton.setGrayStyle(20.uiPX())
			}
			confirmButton.setMargins<LinearLayout.LayoutParams> {
				bottomMargin = 30.uiPX()
			}
		}
	}

	private fun resetBackButtonEvent() {
		// 从下一个页面返回后通过显示隐藏监听重设回退按钮的事件
		rootFragment?.apply {
			showBackButton(true) {
				if (memoInputView.isNull()) {
					presenter.popFragmentFrom<PaymentDetailFragment>()
				} else {
					removeMemoInputView()
				}
			}
		}
	}
}