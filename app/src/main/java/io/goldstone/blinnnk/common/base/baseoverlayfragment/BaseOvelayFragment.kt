package io.goldstone.blinnnk.common.base.baseoverlayfragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.blinnnk.extension.hideStatusBar
import com.blinnnk.extension.isNotNull
import com.blinnnk.extension.setMargins
import com.blinnnk.util.observing
import com.umeng.analytics.MobclickAgent
import io.goldstone.blinnnk.common.base.basefragment.BaseFragment
import io.goldstone.blinnnk.common.base.baseoverlayfragment.overlayview.OverlayHeaderLayout
import io.goldstone.blinnnk.common.base.baseoverlayfragment.overlayview.OverlayView
import io.goldstone.blinnnk.common.base.baserecyclerfragment.BaseRecyclerFragment
import io.goldstone.blinnnk.common.component.title.TwoLineTitles
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.utils.getMainActivity
import io.goldstone.blinnnk.common.utils.setTransparentStatusBar
import io.goldstone.blinnnk.common.value.ElementID
import io.goldstone.blinnnk.common.value.HomeSize
import io.goldstone.blinnnk.module.entrance.splash.view.SplashActivity
import io.goldstone.blinnnk.module.home.home.view.HomeFragment
import io.goldstone.blinnnk.module.home.home.view.MainActivity
import io.goldstone.blinnnk.module.home.wallet.walletdetail.view.WalletDetailFragment
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.UI

/**
 * @date 22/03/2018 2:28 AM
 * @author KaySaith
 */
abstract class BaseOverlayFragment<out T : BaseOverlayPresenter<BaseOverlayFragment<T>>>
	: Fragment() {

	abstract val presenter: T
	abstract fun ViewGroup.initView()

	open var headerTitle: String by observing("") {
		overlayView.header.setTitle(headerTitle)
	}
	private lateinit var overlayView: OverlayView
	/**
	 * 通过对 `Runnable` 的变化监控, 重新定制控件的 `Header`
	 */
	open var customHeader: (OverlayHeaderLayout.() -> Unit)? by observing(null) {
		overlayView.header.apply {
			showTitle(false)
			customHeader?.let {
				it()
				overlayView.contentLayout.setMargins<RelativeLayout.LayoutParams> {
					topMargin = layoutParams.height
				}
			}
		}
	}

	fun getContainer(): OverlayView = overlayView

	fun getSearchContent(): String = overlayView.header.getSearchContent()

	fun showBackButton(isShow: Boolean, action: ImageView.() -> Unit) {
		overlayView.header.showBackButton(isShow, action)
	}

	fun showFilterButton(isShow: Boolean, action: () -> Unit) {
		overlayView.header.showFilterIcon(isShow, action)
	}

	fun showCloseButton(isShow: Boolean, action: () -> Unit) {
		overlayView.header.showCloseButton(isShow, action)
	}

	fun showSearchButton(isShow: Boolean, setClickEvent: () -> Unit) {
		overlayView.header.showSearchButton(isShow, setClickEvent)
	}

	fun showSearchInput(
		isShow: Boolean = true,
		cancelEvent: () -> Unit,
		enterKeyEvent: () -> Unit,
		hint: String
	) {
		overlayView.isNotNull()
		overlayView.header.showSearchInput(isShow, cancelEvent, enterKeyEvent)
		overlayView.header.setSearchInputHint(hint)
	}

	fun showAddButton(status: Boolean, isLeft: Boolean = true, clickEvent: () -> Unit) {
		overlayView.header.showAddButton(status, isLeft, clickEvent)
	}

	fun setTitle(title: String) {
		overlayView.header.setTitle(title)
	}

	fun setSearchHint(hint: String) {
		overlayView.header.setSearchInputHint(hint)
	}

	fun showTitle(status: Boolean) {
		overlayView.header.showTitle(status)
	}

	fun showFilterImage(status: Boolean) {
		overlayView.header.showFilterImage(status)
	}

	fun showScanButton(
		isShow: Boolean,
		isLeft: Boolean = false,
		action: () -> Unit
	) {
		overlayView.header.showScanButton(isShow, isLeft, action)
	}

	fun resetFilterStatus(filtered: Boolean) {
		overlayView.header.resetFilterStatus(filtered)
	}

	fun setFilterEvent(action: () -> Unit) {
		overlayView.header.setFilterEvent(action)
	}

	fun searchInputListener(action: (String) -> Unit) {
		overlayView.header.apply {
			searchTextChangedEvent = Runnable { action(getSearchContent()) }
		}
	}

	// 这个是用来还原 `Header` 的边界方法, 当自定义 `Header` 后还原的操作
	fun recoveryOverlayHeader() {
		overlayView.apply {
			header.removeView(header.findViewById<TwoLineTitles>(ElementID.customHeader))
			header.showTitle(true)
			header.layoutParams.height = HomeSize.headerHeight
			contentLayout.setMargins<RelativeLayout.LayoutParams> {
				topMargin = HomeSize.headerHeight
			}
		}
	}

	override fun onAttach(context: Context?) {
		super.onAttach(context)
		presenter.onFragmentAttach()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// 全屏幕悬浮层展开的时候隐藏 `StatusBar`
		activity?.apply {
			if (!SharedWallet.isNotchScreen()) {
				hideStatusBar()
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		presenter.onFragmentCreateView()
		return UI {
			overlayView = OverlayView(context!!)
			overlayView.contentLayout.initView()
			addView(overlayView, RelativeLayout.LayoutParams(matchParent, matchParent))
		}.view
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		/** 设定标题的时机 */
		overlayView.header.setTitle(headerTitle)
		overlayView.header.showCloseButton(true) {
			presenter.removeSelfFromActivity()
		}
		presenter.onFragmentViewCreated()
		showHomeFragment(false)
		hideTabBarToAvoidOverdraw()
	}

	private fun showHomeFragment(isShow: Boolean) {
		activity?.apply {
			if (this is MainActivity) {
				if (isShow) showHomeFragment()
				else hideHomeFragment()
			}
		}
	}

	override fun onDetach() {
		super.onDetach()
		presenter.onFragmentDetach()
		showHomeFragment(true)
		showTabBarView()
	}

	open fun setTransparentStatus() {
		activity?.apply {
			if (!SharedWallet.isNotchScreen()) {
				setTransparentStatusBar()
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		presenter.onFragmentDestroy()
		setBackEvent()
		setTransparentStatus()
	}

	override fun onResume() {
		super.onResume()
		presenter.onFragmentResume()
		MobclickAgent.onPageStart(this.javaClass.simpleName)
	}

	override fun onPause() {
		super.onPause()
		MobclickAgent.onPageEnd(this.javaClass.simpleName)
	}

	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		if (!hidden) {
			presenter.onFragmentShowFromHidden()
		}
	}

	open fun setBackEvent() {
		// 恢复 `HomeFragment` 的 `ChildFragment` 的回退栈事件
		val currentActivity = activity ?: return
		val lastChild = currentActivity.supportFragmentManager.fragments.last() ?: return
		when (currentActivity) {
			is MainActivity -> try {
				val currentFragment = lastChild.childFragmentManager.fragments.last() ?: return
				when (currentFragment) {
					is HomeFragment -> {
						currentActivity.backEvent =
							if (currentFragment !is WalletDetailFragment) {
								Runnable { currentFragment.presenter.showWalletDetailFragment() }
							} else null
					}
					is BaseFragment<*> -> currentFragment.recoveryBackEvent()
					is BaseRecyclerFragment<*, *> -> currentFragment.recoveryBackEvent()
				}
			} catch (error: Exception) {
				Log.e(javaClass.simpleName, error.message)
			}
			is SplashActivity -> currentActivity.backEvent = null
		}
	}

	private fun hideTabBarToAvoidOverdraw() {
		getMainActivity()?.getHomeFragment()?.hideTabBarView()
	}

	private fun showTabBarView() {
		getMainActivity()?.getHomeFragment()?.showTabBarView()
	}
}