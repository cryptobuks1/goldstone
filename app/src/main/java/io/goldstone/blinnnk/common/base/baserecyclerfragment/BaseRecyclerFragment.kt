package io.goldstone.blinnnk.common.base.baserecyclerfragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blinnnk.util.SoftKeyboard
import com.umeng.analytics.MobclickAgent
import io.goldstone.blinnnk.common.base.gsfragment.GSRecyclerFragment
import io.goldstone.blinnnk.common.component.EmptyView

/**
 * @date 23/03/2018 3:46 PM
 * @author KaySaith
 */
abstract class BaseRecyclerFragment<out T : BaseRecyclerPresenter<BaseRecyclerFragment<T, D>, D>, D>
	: GSRecyclerFragment<D>() {

	/**
	 * @description
	 * 每一个 `Fragment` 都会配备一个 `Presenter` 来进行数据及UI 的控制, 这个 `Presenter`
	 * 必须是配套的 [BaseRecyclerPresenter] `Fragment` 和 `Presenter` 之间
	 * 有固定的约定实现协议, 来更方便安全和便捷的使用.
	 */
	abstract val presenter: T

	override fun onAttach(context: Context?) {
		super.onAttach(context)
		presenter.onFragmentAttach()
	}

	override fun afterUpdateAdapterDataSet(recyclerView: BaseRecyclerView) {
		super.afterUpdateAdapterDataSet(recyclerView)
		presenter.afterUpdateAdapterDataSet(recyclerView)
	}

	override fun onStart() {
		super.onStart()
		presenter.onFragmentStart()
	}

	override fun onDetach() {
		super.onDetach()
		presenter.onFragmentDetach()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		presenter.onFragmentDestroy()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		presenter.onFragmentCreate()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		presenter.onFragmentCreateView()
		return super.onCreateView(inflater, container, savedInstanceState)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		presenter.onFragmentViewCreated()
	}

	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		presenter.onFragmentHiddenChanged(hidden)
		if (!isHidden) presenter.onFragmentShowFromHidden()
	}

	override fun onDestroy() {
		super.onDestroy()
		// 如果键盘在显示那么销毁键盘
		activity?.apply { SoftKeyboard.hide(this) }
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

	private var emptyLayout: EmptyView? = null
}