package io.goldstone.blinnnk.module.common.tokendetail.tokendetail.presenter

import android.support.annotation.WorkerThread
import com.blinnnk.extension.hasValue
import com.blinnnk.extension.isNotNull
import io.goldstone.blinnnk.common.error.RequestError
import io.goldstone.blinnnk.common.value.DataValue
import io.goldstone.blinnnk.common.value.PageInfo
import io.goldstone.blinnnk.crypto.multichain.ChainType
import io.goldstone.blinnnk.crypto.multichain.getAddress
import io.goldstone.blinnnk.crypto.multichain.getChainType
import io.goldstone.blinnnk.kernel.commontable.BTCSeriesTransactionTable
import io.goldstone.blinnnk.kernel.network.btcseries.insight.InsightApi
import io.goldstone.blinnnk.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.model.TransactionListModel

/**
 * @date 2018/8/14 4:59 PM
 * @author KaySaith
 */

fun TokenDetailPresenter.loadBTCSeriesData(
	chainType: ChainType,
	localMaxIndex: Int,
	loadNew: Boolean
) {
	val address = chainType.getContract().getAddress()
	InsightApi.getTransactionCount(chainType, address) { transactionCount, error ->
		if (transactionCount.isNotNull() && error.isNone()) {
			// 如果本地的最大 Index 与 Count 相同意味着不需要拉取账单
			when {
				loadNew && transactionCount != localMaxIndex -> loadDataFromChain(
					chainType,
					address,
					PageInfo(0, DataValue.pageCount, localMaxIndex, transactionCount),
					true
				) {
					if (it.isNone()) getBTCSeriesData()
					else {
						detailView.showBottomLoading(false)
						detailView.showError(it)
					}
				}
				!loadNew -> loadOldData(chainType, localMaxIndex, transactionCount)
				else -> getBTCSeriesData()
			}
		} else getBTCSeriesData()
	}
}

private fun TokenDetailPresenter.loadOldData(
	chainType: ChainType,
	minIndex: Int,
	totalCount: Int
) {
	val address = chainType.getContract().getAddress()
	loadDataFromChain(
		chainType,
		address,
		PageInfo(
			totalCount - minIndex + 1,
			totalCount - minIndex + DataValue.pageCount + 1,
			minIndex,
			totalCount
		),
		false
	) {
		if (it.isNone()) getBTCSeriesData()
		else {
			detailView.showBottomLoading(false)
			if (!it.isEmptyResult()) detailView.showError(it)
		}
	}
}

@WorkerThread
private fun loadDataFromChain(
	chainType: ChainType,
	address: String,
	pageInfo: PageInfo,
	loadNew: Boolean,
	callback: (error: RequestError) -> Unit
) {
	// 意味着网络没有更新的数据直接返回
	InsightApi.getTransactions(
		chainType,
		address,
		pageInfo.from,
		pageInfo.to
	) { transactions, error ->
		// Calculate All Inputs to get transfer value
		// 转换数据格式
		val dataIndex: (index: Int) -> Int = {
			if (pageInfo.maxDataIndex == 0) pageInfo.total + it * -1
			else pageInfo.maxDataIndex + (it + 1) * if (loadNew) 1 else -1
		}
		if (!transactions.isNullOrEmpty() && error.isNone()) {
			transactions.asSequence().mapIndexed { index, item ->
				BTCSeriesTransactionTable(
					item,
					dataIndex(index),
					address,
					chainType.getContract().symbol,
					false,
					chainType.id
				)
			}.toList().let { all ->
				BTCSeriesTransactionTable.dao.insertAll(all)
				// 同样的账单插入一份燃气费的数据
				BTCSeriesTransactionTable.dao.insertAll(all.filterNot { it.isReceive }.map { it.apply { it.isFee = true } })
				callback(error)
			}
		} else if (transactions?.isEmpty() == true) {
			callback(RequestError.EmptyResut)
		} else callback(error)
	}
}

@WorkerThread
fun TokenDetailPresenter.getBTCSeriesData() {
	val btcSeriesDao = BTCSeriesTransactionTable.dao
	with(token.contract) {
		val startDataIndex =
			if (detailView.asyncData.isNullOrEmpty()) {
				btcSeriesDao.getMaxDataIndex(
					getAddress(),
					getChainType().id
				)?.dataIndex
			} else detailView.asyncData?.minBy { it.dataIndex }?.dataIndex!! - 1
		if (startDataIndex.hasValue()) {
			val transactions =
				btcSeriesDao.getDataByRange(
					getAddress(),
					getChainType().id,
					startDataIndex - DataValue.pageCount,
					startDataIndex
				)
			when {
				transactions.isNotEmpty() -> {
					if (detailView.asyncData?.isEmpty() == true) {
						transactions.map {
							TransactionListModel(it)
						}.generateBalanceList(token.contract) {
							it.updateHeaderData(false)
						}
					}
					flipPage(transactions) {
						detailView.showBottomLoading(false)
						detailView.showLoading(false)
					}
				}
				else -> loadBTCSeriesData(getChainType(), startDataIndex + 1, false)
			}
		} else {
			detailView.showLoading(false)
			detailView.showBottomLoading(false)
		}
	}
}