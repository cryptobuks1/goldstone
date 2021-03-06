package io.goldstone.blinnnk.kernel.network.btcseries.insight

import android.support.annotation.WorkerThread
import com.blinnnk.extension.isNotNull
import com.blinnnk.extension.safeGet
import com.blinnnk.extension.toJSONObjectList
import io.goldstone.blinnnk.common.error.RequestError
import io.goldstone.blinnnk.crypto.multichain.Amount
import io.goldstone.blinnnk.crypto.multichain.ChainID
import io.goldstone.blinnnk.crypto.multichain.ChainType
import io.goldstone.blinnnk.crypto.multichain.isBCH
import io.goldstone.blinnnk.crypto.utils.toSatoshi
import io.goldstone.blinnnk.kernel.commontable.BTCSeriesTransactionTable
import io.goldstone.blinnnk.kernel.network.ParameterUtil
import io.goldstone.blinnnk.kernel.network.bitcoin.model.UnspentModel
import io.goldstone.blinnnk.kernel.network.common.RequisitionUtil
import org.json.JSONArray
import org.json.JSONObject

/**
 * @date 2018/8/13 12:07 PM
 * @author KaySaith
 */

object InsightApi {
	fun sendRAWTransaction(
		chainType: ChainType,
		singedMessage: String,
		hold: (hash: String?, error: RequestError) -> Unit
	) {
		RequisitionUtil.post(
			ParameterUtil.prepare(false, Pair("rawtx", singedMessage)),
			InsightUrl.sendRAWTransaction(chainType),
			false,
			hold
		)
	}

	fun getBlockCount(
		chainType: ChainType,
		@WorkerThread hold: (count: Int?, error: RequestError) -> Unit
	) {
		RequisitionUtil.requestData<String>(
			InsightUrl.getBlockCount(chainType),
			"",
			true,
			null,
			false
		) { result, error ->
			if (result.isNotNull() && error.isNone()) {
				try {
					hold(JSONObject(result.firstOrNull()).safeGet("blockChainHeight").toIntOrNull(), error)
				} catch (error: Exception) {
					hold(null, RequestError(error.message.orEmpty()))
				}
			} else {
				hold(null, error)
			}
		}
	}

	fun getEstimateFee(
		chainType: ChainType,
		blockCount: Int,
		@WorkerThread hold: (fee: Double?, error: RequestError) -> Unit
	) {
		RequisitionUtil.requestData<String>(
			InsightUrl.estimateFee(chainType, blockCount),
			"",
			true,
			null,
			false
		) { result, error ->
			if (result.isNotNull() && error.isNone()) {
				try {
					hold(JSONObject(result.firstOrNull()).safeGet("$blockCount").toDoubleOrNull(), error)
				} catch (error: Exception) {
					hold(null, RequestError(error.message.orEmpty()))
				}
			} else {
				hold(null, error)
			}
		}
	}

	fun getBalance(
		chainType: ChainType,
		address: String,
		@WorkerThread hold: (balance: Amount<Long>?, error: RequestError) -> Unit
	) {
		RequisitionUtil.requestData<String>(
			InsightUrl.getBalance(chainType, address),
			"",
			true,
			null,
			false
		) { result, error ->
			// Insight BCH 的 getBalance 接口返回的是 Double 信息, BTC
			// 和 LTC 返回的是 Satoshi
			if (result.isNotNull() && error.isNone()) {
				val data =
					if (chainType.isBCH()) result.firstOrNull()?.toDoubleOrNull()?.toSatoshi() ?: 0
					else result.firstOrNull()?.toLongOrNull() ?: 0
				hold(Amount(data), RequestError.None)
			} else hold(null, error)
		}
	}

	fun getUnspent(
		chainType: ChainType,
		address: String,
		@WorkerThread hold: (unspentList: List<UnspentModel>?, error: RequestError) -> Unit
	) {
		RequisitionUtil.requestData(
			InsightUrl.getUnspentInfo(chainType, address),
			"",
			false,
			null,
			false,
			hold = hold
		)
	}

	fun getTransactions(
		chainType: ChainType,
		address: String,
		from: Int,
		to: Int,
		@WorkerThread hold: (transactions: List<JSONObject>?, error: RequestError) -> Unit
	) {
		RequisitionUtil.requestData<String>(
			InsightUrl.getTransactions(chainType, address, from, to),
			"items",
			true,
			null,
			false
		) { result, error ->
			if (result.isNotNull() && error.isNone()) {
				val jsonArray = JSONArray(result.firstOrNull())
				hold(jsonArray.toJSONObjectList(), error)
			} else {
				hold(null, error)
			}
		}
	}

	fun getTransactionCount(
		chainType: ChainType,
		address: String,
		hold: (count: Int?, error: RequestError) -> Unit
	) {
		RequisitionUtil.requestData<String>(
			InsightUrl.getTransactions(chainType, address, 999999999, 0),
			"totalItems",
			true,
			null,
			false
		) { result, error ->
			try {
				hold(result?.firstOrNull()?.toIntOrNull(), error)
			} catch (error: Exception) {
				hold(null, RequestError.None)
			}
		}
	}

	fun getTransactionJSONByHash(
		chainID: ChainID,
		hash: String,
		@WorkerThread hold: (json: String?, error: RequestError) -> Unit
	) {
		RequisitionUtil.requestData<String>(
			InsightUrl.getTransactionByHash(chainID, hash),
			"",
			true,
			null,
			false
		) { result, error ->
			hold(result?.firstOrNull(), error)
		}
	}

	// 因为通知中心是混合主网测试网的查账所以, 相关接口设计为需要传入网络头的参数头
	fun getTransactionByHash(
		chainID: ChainID,
		hash: String,
		address: String,
		@WorkerThread hold: (transaction: BTCSeriesTransactionTable?, error: RequestError) -> Unit
	) {
		getTransactionJSONByHash(chainID, hash) { json, error ->
			if (json.isNotNull() && error.isNone()) {
				val data = JSONObject(json)
				hold(
					BTCSeriesTransactionTable(
						data,
						// 这里拉取的数据只在通知中心展示并未插入数据库 , 所以 DataIndex 随便设置即可
						0,
						address,
						chainID.getContract().symbol,
						false,
						chainID.getChainType().id
					),
					error
				)
			} else hold(null, error)
		}
	}
}