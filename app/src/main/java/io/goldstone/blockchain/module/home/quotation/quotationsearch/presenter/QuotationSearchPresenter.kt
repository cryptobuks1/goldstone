package io.goldstone.blockchain.module.home.quotation.quotationsearch.presenter

import com.blinnnk.extension.*
import com.google.gson.JsonArray
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.utils.getMainActivity
import io.goldstone.blockchain.kernel.network.GoldStoneAPI
import io.goldstone.blockchain.module.home.quotation.quotationoverlay.view.QuotationOverlayFragment
import io.goldstone.blockchain.module.home.quotation.quotationsearch.model.QuotationSelectionTable
import io.goldstone.blockchain.module.home.quotation.quotationsearch.view.QuotationSearchAdapter
import io.goldstone.blockchain.module.home.quotation.quotationsearch.view.QuotationSearchFragment
import org.jetbrains.anko.runOnUiThread

/**
 * @date 21/04/2018 4:32 PM
 * @author KaySaith
 */

class QuotationSearchPresenter(
	override val fragment: QuotationSearchFragment
) : BaseRecyclerPresenter<QuotationSearchFragment, QuotationSelectionTable>() {

	override fun updateData() {
		fragment.asyncData = arrayListOf()
	}

	override fun onFragmentViewCreated() {
		super.onFragmentViewCreated()
		setHeightMatchParent()
		fragment.getParentFragment<QuotationOverlayFragment> {
			overlayView.header.setKeyboardConfirmEvent {
				getMainActivity()?.showLoadingView()
				searchTokenBy(text.toString())
			}
		}
	}

	fun setQuotationSelfSelection(
		model: QuotationSelectionTable, isSelect: Boolean = true, callback: () -> Unit = {}
	) {
		isSelect isTrue {
			// 如果选中, 拉取选中的 `token` 的 `lineChart` 信息
			getLineChartDataByPair(model.pair) { chartData ->
				QuotationSelectionTable.insertSelection(model.apply {
					lineChart = chartData
					isSelecting = isSelect
				}) { callback() }
			}
		} otherwise {
			QuotationSelectionTable.removeSelectionBy(model.pair) { callback() }
		}
	}

	private fun searchTokenBy(symbol: String) {
		// 拉取搜索列表
		GoldStoneAPI.getMarketSearchList(symbol) { searchList ->
			// 获取本地自己选中的列表
			QuotationSelectionTable.getMySelections { selectedList ->
				// 如果本地没有已经选中的直接返回搜索的数据展示在界面
				selectedList.isEmpty() isTrue {
					fragment.completeQuotationTable(searchList)
				} otherwise {
					// 否则用搜索的记过查找是否有已经选中在本地的, 更改按钮的选中状态
					searchList.forEachOrEnd { item, isEnd ->
						item.isSelecting = selectedList.any { it.pair == item.pair }
						if (isEnd) {
							fragment.completeQuotationTable(searchList)
						}
					}
				}
			}
		}
	}

	private fun QuotationSearchFragment.completeQuotationTable(searchList: ArrayList<QuotationSelectionTable>) {
		context?.runOnUiThread {
			getMainActivity()?.removeLoadingView()
			diffAndUpdateSingleCellAdapterData<QuotationSearchAdapter>(searchList.map {
				QuotationSelectionTable(it, "")
			}.toArrayList())
		}
	}

	companion object {
		fun getLineChartDataByPair(pair: String, hold: (String) -> Unit) {
			val parameter = JsonArray().apply { add(pair) }
			GoldStoneAPI.getCurrencyLineChartData(parameter) {
				it.isNotEmpty() isTrue {
					hold(it[0].pairList.toString())
				} otherwise {
					hold("")
				}
			}
		}
	}
}