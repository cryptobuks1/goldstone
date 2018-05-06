package io.goldstone.blockchain.module.home.quotation.quotation.presenter

import com.blinnnk.extension.*
import com.db.chart.model.Point
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.utils.GoldStoneWebSocket
import io.goldstone.blockchain.common.utils.getMainActivity
import io.goldstone.blockchain.common.utils.toJsonArray
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.ContainerID
import io.goldstone.blockchain.common.value.QuotationText
import io.goldstone.blockchain.crypto.daysAgoInMills
import io.goldstone.blockchain.crypto.getObjectMD5HexString
import io.goldstone.blockchain.module.home.quotation.quotation.model.CurrencyPriceInfoModel
import io.goldstone.blockchain.module.home.quotation.quotation.model.QuotationModel
import io.goldstone.blockchain.module.home.quotation.quotation.view.QuotationAdapter
import io.goldstone.blockchain.module.home.quotation.quotation.view.QuotationFragment
import io.goldstone.blockchain.module.home.quotation.quotationoverlay.view.QuotationOverlayFragment
import io.goldstone.blockchain.module.home.quotation.quotationsearch.model.QuotationSelectionTable
import io.goldstone.blockchain.module.home.quotation.quotationsearch.presenter.QuotationSearchPresenter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.json.JSONArray
import org.json.JSONObject

/**
 * @date 26/03/2018 8:56 PM
 * @author KaySaith
 */

private var latestSelectionMD5: String? = null
private var memoryData: ArrayList<QuotationModel>? = null

class QuotationPresenter(
	override val fragment: QuotationFragment
) : BaseRecyclerPresenter<QuotationFragment, QuotationModel>() {

	override fun updateData() {
		QuotationSelectionTable.getMySelections { selections ->
			// 每次更新数据的时候重新执行长连接, 因为是 `?` 驱动初始化的时候这个不会执行
			currentSocket?.runSocket()

			// 判断是否更新数据或直接从内存读取数据显示
			if (latestSelectionMD5 == selections.getObjectMD5HexString() && !memoryData.isNull()) {
				fragment.asyncData = memoryData
				return@getMySelections
			}

			// 把最近一次数据的 MD5 值存入内存, 任何需要更新数据的逻辑会先行比对是否需要更新
			latestSelectionMD5 = selections.getObjectMD5HexString()
			selections.map { selection ->
				val linechart = convertDataToChartData(selection.lineChart)
				linechart.checkTimeStampIfNeedUpdateBy(selection.pair)
				QuotationModel(selection, "--", "0", linechart)
			}.sortedByDescending {
				it.orderID
			}.toArrayList().let {
				memoryData = it
				fragment.asyncData.isNull() isTrue {
					fragment.asyncData = it
					fragment.setEmptyViewBy(it)
				} otherwise {
					fragment.setEmptyViewBy(it)
					diffAndUpdateAdapterData<QuotationAdapter>(it)
				}
				// 设定 `Socket` 并执行
				setSocket { currentSocket?.runSocket() }
			}
		}
	}

	private fun ArrayList<Point>.checkTimeStampIfNeedUpdateBy(pair: String) {
		sortedByDescending {
			it.label.toLong()
		}.let {
			// 服务端传入的最近的事件会做减1处理, 从服务器获取的事件是昨天的事件.
			// 本地的当天 `lineChart` 的值是通过长连接实时更新的.
			if (it.first().label.toLong() + 1L < 0.daysAgoInMills()) {
				fragment.getMainActivity()?.showLoadingView()
				QuotationSearchPresenter.getLineChartDataByPair(pair) { newChart ->
					QuotationSelectionTable.updateLineChartDataBy(pair, newChart) {
						updateData()
						fragment.getMainActivity()?.removeLoadingView()
					}
				}
			}
		}
	}

	private var currentSocket: GoldStoneWebSocket? = null
	private fun setSocket(callback: () -> Unit) {
		fragment.asyncData?.isEmpty()?.isTrue { return }
		getPriceInfoBySocket(fragment.asyncData?.map { it.pair }?.toArrayList(), {
			currentSocket = it
			callback()
		}) {
			fragment.updateAdapterDataset(it)
		}
	}

	override fun onFragmentHiddenChanged(isHidden: Boolean) {
		if (isHidden) {
			currentSocket?.isSocketConnected()?.isTrue {
				currentSocket?.closeSocket()
			}
		} else {
			currentSocket?.isSocketConnected()?.isFalse {
				currentSocket?.runSocket()
			}
		}
	}

	private fun QuotationFragment.updateAdapterDataset(data: CurrencyPriceInfoModel) {
		doAsync {
			var index: Int? = null
			asyncData?.forEachOrEnd { item, isEnd ->
				if (item.pair == data.pair) {
					item.price = data.price
					item.percent = data.percent
					index = asyncData?.indexOf(item).orZero() + 1
				}
				if (isEnd) {
					context?.runOnUiThread {
						index?.let { recyclerView.adapter.notifyItemChanged(it) }
					}
				}
			}
		}
	}

	fun showQuotationManagement() {
		fragment.activity?.addFragmentAndSetArguments<QuotationOverlayFragment>(ContainerID.main) {
			putString(ArgumentKey.quotationOverlayTitle, QuotationText.management)
		}
	}

	fun showMarketTokenDetailFragment(model: QuotationModel) {
		fragment.activity?.addFragmentAndSetArguments<QuotationOverlayFragment>(ContainerID.main) {
			putSerializable(ArgumentKey.quotationOverlayInfo, model)
		}
	}

	private fun convertDataToChartData(data: String): ArrayList<Point> {
		val jsonarray = JSONArray(data)
		(0 until jsonarray.length()).map {
			val timeStamp = jsonarray.getJSONObject(it)["time"].toString().toLong()
			Point(timeStamp.toString(), jsonarray.getJSONObject(it)["price"].toString().toFloat())
		}.reversed().let {
			return it.toArrayList()
		}
	}

	companion object {
		fun getPriceInfoBySocket(
			pairList: ArrayList<String>?,
			holdSocket: (GoldStoneWebSocket) -> Unit,
			hold: (CurrencyPriceInfoModel) -> Unit
		) {
			/**
			 * 准备长连接, 发送参数. 并且在返回结果的地方异步更新界面上的 `UI`.
			 */
			object : GoldStoneWebSocket() {
				override fun onOpened() {
					pairList?.toJsonArray {
						sendMessage("{\"t\":\"sub_tick\", \"pair_list\":$it}")
					}
				}

				override fun getServerBack(content: JSONObject) {
					hold(CurrencyPriceInfoModel(content))
				}
			}.apply(holdSocket)
		}
	}
}