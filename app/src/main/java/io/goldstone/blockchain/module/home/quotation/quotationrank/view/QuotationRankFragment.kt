package io.goldstone.blockchain.module.home.quotation.quotationrank.view

import android.os.Bundle
import android.view.View
import com.blinnnk.extension.orEmptyArray
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerView
import io.goldstone.blockchain.common.base.baserecyclerfragment.BottomLoadingView
import io.goldstone.blockchain.common.base.gsfragment.GSRecyclerFragment
import io.goldstone.blockchain.common.utils.ErrorDisplayManager
import io.goldstone.blockchain.module.home.quotation.quotationrank.contract.QuotationRankContract
import io.goldstone.blockchain.module.home.quotation.quotationrank.model.QuotationGlobalModel
import io.goldstone.blockchain.module.home.quotation.quotationrank.model.QuotationRankTable
import io.goldstone.blockchain.module.home.quotation.quotationrank.presenter.QuotationRankPresenter


/**
 * @author KaySaith
 * @date  2019/01/02
 */
class QuotationRankFragment : GSRecyclerFragment<QuotationRankTable>(), QuotationRankContract.GSView {
	
	override val pageTitle: String = "Quotation Rank"
	private var headerView: QuotationRankHeaderView? = null
	private var bottomLoadingView: BottomLoadingView? = null
	override lateinit var presenter: QuotationRankContract.GSPresenter
	
	override fun showBottomLoading(isShow: Boolean) {
		if (isShow) bottomLoadingView?.show()
		else bottomLoadingView?.hide()
		isLoadingData = false
	}
	
	override fun setRecyclerViewAdapter(
		recyclerView: BaseRecyclerView,
		asyncData: ArrayList<QuotationRankTable>?
	) {
		recyclerView.adapter = QuotationRankAdapter(
			asyncData.orEmptyArray(),
			holdHeader = {
				headerView = this
			},
			holdFooter = {
				bottomLoadingView = this
			},
			holdClickAction = {}
		)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		asyncData = arrayListOf()
		presenter = QuotationRankPresenter(this)
		presenter.start()
	}
	
	override fun showError(error: Throwable) {
		ErrorDisplayManager(error).show(context)
	}
	
	override fun showHeaderData(model: QuotationGlobalModel) {
		headerView?.model = model
	}
	
	override fun flipPage() {
		super.flipPage()
		presenter.loadMore()
	}
	
	override fun updateData(newData: List<QuotationRankTable>) {
		asyncData?.apply {
			addAll(newData)
			val dataSize = size
			if (dataSize > 0) {
				recyclerView.adapter?.notifyItemRangeChanged(dataSize - newData.size, dataSize)
				removeEmptyView()
			}
		}
	}
}