package io.goldstone.blockchain.kernel.network

import android.annotation.SuppressLint
import android.content.Context
import com.blinnnk.extension.isNotNull
import com.blinnnk.extension.orZero
import com.blinnnk.extension.safeGet
import com.blinnnk.extension.toArrayList
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import io.goldstone.blockchain.common.utils.AesCrypto
import io.goldstone.blockchain.common.utils.ConcurrentAsyncCombine
import io.goldstone.blockchain.common.utils.LogUtil
import io.goldstone.blockchain.common.utils.SystemUtils
import io.goldstone.blockchain.common.value.ChainID
import io.goldstone.blockchain.common.value.GoldStoneCrayptoKey
import io.goldstone.blockchain.crypto.getObjectMD5HexString
import io.goldstone.blockchain.crypto.toJsonObject
import io.goldstone.blockchain.kernel.commonmodel.AppConfigTable
import io.goldstone.blockchain.kernel.commonmodel.TransactionTable
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blockchain.module.home.profile.profile.model.VersionModel
import io.goldstone.blockchain.module.home.quotation.markettokendetail.model.ChartModel
import io.goldstone.blockchain.module.home.quotation.quotationsearch.model.QuotationSelectionLineChartModel
import io.goldstone.blockchain.module.home.quotation.quotationsearch.model.QuotationSelectionTable
import io.goldstone.blockchain.module.home.wallet.notifications.notificationlist.model.NotificationTable
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenSearch.model.TokenSearchModel
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.model.TinyNumber
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.model.ERC20TransactionModel
import io.goldstone.blockchain.module.home.wallet.walletdetail.model.TokenPriceModel
import okhttp3.*
import org.jetbrains.anko.runOnUiThread
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
/**
 * @date 31/03/2018 8:08 PM
 * @author KaySaith
 */
object GoldStoneAPI {
	
	/** 网络请求很多是全台异步所以使用 `Application` 的 `Context` */
	lateinit var context: Context
	private val requestContentType = MediaType.parse("application/json; charset=utf-8")
	
	/**
	 * 从服务器获取产品指定的默认的 `DefaultTokenList`
	 */
	@JvmStatic
	fun getDefaultTokens(
		errorCallback: () -> Unit = {},
		hold: (ArrayList<DefaultTokenTable>) -> Unit
	) {
		requestData<String>(APIPath.defaultTokenList, "data", true, errorCallback) {
			val gson = Gson()
			val collectionType = object : TypeToken<Collection<DefaultTokenTable>>() {}.type
			val allDefaultTokens = arrayListOf<DefaultTokenTable>()
			object : ConcurrentAsyncCombine() {
				override var asyncCount = ChainID.getAllChainID().size
				override fun concurrentJobs() {
					ChainID.getAllChainID().forEach { chainID ->
						allDefaultTokens +=
							gson.fromJson<List<DefaultTokenTable>>(
								JSONObject(this@requestData[0]).safeGet(chainID),
								collectionType
							).map {
								it.apply {
									it.chain_id = chainID
									it.isDefault = true
								}
							}.apply {
								completeMark()
							}
					}
				}
				
				override fun mergeCallBack() {
					hold(allDefaultTokens)
				}
			}.start()
		}
	}
	
	@JvmStatic
	fun getCoinInfoBySymbolFromGoldStone(
		symbols: String,
		hold: (ArrayList<TokenSearchModel>) -> Unit
	) {
		requestData<TokenSearchModel>(APIPath.getCoinInfo + symbols, "list") {
			hold(toArrayList())
		}
	}
	
	@JvmStatic
	fun getNewVersionOrElse(
		hold: (VersionModel?) -> Unit
	) {
		requestData<String>(APIPath.getNewVersion, "", true) {
			val data = JSONObject(this[0])
			val hasNewVersion =
				data.safeGet("has_new_version").toIntOrNull().orZero() == TinyNumber.True.value
			GoldStoneAPI.context.runOnUiThread {
				if (hasNewVersion) {
					hold(VersionModel(JSONObject(data.safeGet("data"))))
				} else {
					hold(null)
				}
			}
		}
	}
	
	@JvmStatic
	fun getCurrencyRate(
		symbols: String,
		errorCallback: () -> Unit,
		hold: (Double) -> Unit
	) {
		requestData<String>(APIPath.getCurrencyRate + symbols, "rate", true, errorCallback) {
			this[0].isNotNull { hold(this[0].toDouble()) }
		}
	}
	
	@JvmStatic
	fun getMarketSearchList(
		pair: String,
		hold: (ArrayList<QuotationSelectionTable>) -> Unit
	) {
		requestData<QuotationSelectionTable>(APIPath.marketSearch + pair, "pair_list") {
			hold(toArrayList())
		}
	}
	
	fun getERC20TokenIncomingTransaction(
		startBlock: String = "0",
		address: String = WalletTable.current.address,
		errorCallback: (Exception) -> Unit,
		hold: (ArrayList<ERC20TransactionModel>) -> Unit
	) {
		requestUncryptoData<ERC20TransactionModel>(
			EtherScanApi.getTokenIncomingTransaction(address, startBlock),
			"result",
			false,
			errorCallback
		) {
			hold(toArrayList())
		}
	}
	
	/**
	 * 从 `EtherScan` 获取指定钱包地址的 `TransactionList`
	 */
	@JvmStatic
	fun getTransactionListByAddress(
		startBlock: String = "0",
		address: String = WalletTable.current.address,
		errorCallback: (Exception) -> Unit,
		hold: ArrayList<TransactionTable>.() -> Unit
	) {
		requestUncryptoData<TransactionTable>(
			EtherScanApi.transactions(address, startBlock),
			"result",
			false,
			errorCallback
		) {
			hold(map { TransactionTable(it) }.toArrayList())
		}
	}
	
	fun registerDevice(
		language: String,
		pushToken: String,
		deviceID: String,
		isChina: Int,
		isAndroid: Int,
		chainID: Int,
		errorCallback: () -> Unit,
		hold: (String) -> Unit
	) {
		RequestBody.create(
			requestContentType, AesCrypto.encrypt(
			"{\"language\":\"$language\", \"cid\":\"$pushToken\", \"device\":\"$deviceID\",\"push_type\":$isChina, \"os\":$isAndroid, \"chainid\":$chainID}"
		).orEmpty()
		).let {
			postRequest(it, APIPath.registerDevice, errorCallback) {
				hold(it)
			}
		}
	}
	
	fun getCurrencyLineChartData(
		pairList: JsonArray,
		hold: (ArrayList<QuotationSelectionLineChartModel>) -> Unit
	) {
		RequestBody.create(
			requestContentType,
			AesCrypto.encrypt("{\"pair_list\":$pairList}").orEmpty()
		).let {
			postRequestGetJsonObject<QuotationSelectionLineChartModel>(
				it,
				"data_list",
				APIPath.getCurrencyLineChartData
			) {
				hold(it.toArrayList())
			}
		}
	}
	
	fun registerWalletAddress(
		addressList: JsonArray,
		deviceID: String,
		netWorkError: () -> Unit = {},
		hold: (String) -> Unit
	) {
		RequestBody.create(
			requestContentType,
			AesCrypto.encrypt("{\"address_list\":$addressList,\"device\":\"$deviceID\"}").orEmpty()
		).let {
			postRequest(it, APIPath.updateAddress, netWorkError) {
				hold(it)
			}
		}
	}
	
	fun getUnreadCount(
		deviceID: String,
		time: Long,
		netWorkError: () -> Unit = {},
		hold: (String) -> Unit
	) {
		RequestBody.create(
			requestContentType, AesCrypto.encrypt("{\"device\":\"$deviceID\",\"time\":$time}").orEmpty()
		).let {
			postRequest(it, APIPath.getUnreadCount, netWorkError) {
				hold(JSONObject(it).safeGet("count"))
			}
		}
	}
	
	fun getNotificationList(
		goldSonteID: String,
		time: Long,
		errorCallback: () -> Unit,
		hold: (ArrayList<NotificationTable>) -> Unit
	) {
		// 加密 `Post` 请求
		val content = AesCrypto.encrypt("{\"device\":\"$goldSonteID\",\"time\":$time}").orEmpty()
		RequestBody.create(requestContentType, content).let {
			postRequestGetJsonObject<String>(
				it,
				"message_list",
				APIPath.getNotification,
				true,
				errorCallback
			) {
				// 因为返回的数据格式复杂这里采用自己处理数据的方式, 不实用 `Gson`
				val notificationData = arrayListOf<NotificationTable>()
				val jsonarray = JSONArray(it[0])
				GoldStoneAPI.context.runOnUiThread {
					if (jsonarray.length() == 0) {
						hold(arrayListOf())
					} else {
						(0 until jsonarray.length()).forEach {
							notificationData.add(NotificationTable(JSONObject(jsonarray[it].toString())))
							if (it == jsonarray.length() - 1) {
								hold(notificationData)
							}
						}
					}
				}
			}
		}
	}
	
	fun getPriceByContractAddress(
		addressList: JsonArray,
		errorCallback: () -> Unit,
		hold: (ArrayList<TokenPriceModel>) -> Unit
	) {
		// 加密 `Post` 请求
		val content = AesCrypto.encrypt("{\"address_list\":$addressList}").orEmpty()
		RequestBody.create(requestContentType, content).let {
			postRequestGetJsonObject<TokenPriceModel>(
				it,
				"price_list",
				APIPath.getPriceByAddress,
				errorCallback = { errorCallback() }) {
				GoldStoneAPI.context.runOnUiThread {
					hold(it.toArrayList())
				}
			}
		}
	}
	
	fun getQuotationCurrencyChart(
		pair: String,
		period: String,
		size: Int,
		hold: (ArrayList<ChartModel>) -> Unit
	) {
		requestData<ChartModel>(
			APIPath.getQuotationCurrencyChart(pair, period, size),
			"point_list"
		) {
			hold(this.toArrayList())
		}
	}
	
	fun getQuotationCurrencyInfo(
		pair: String,
		hold: (JSONObject) -> Unit
	) {
		requestData<String>(APIPath.getQuotationCurrencyInfo(pair), "", true) {
			hold(JSONObject(this[0]))
		}
	}
	
	fun getQuotationCurrencyDescription(
		symbol: String,
		hold: (String) -> Unit
	) {
		requestData<String>(
			APIPath.getTokenDescription + symbol, "", true
		) {
			this[0].let {
				hold(JSONObject(it).safeGet("description"))
			}
		}
	}
	
	/**————————————————————— public network request method ———————————————————————*/
	private val client =
		OkHttpClient
			.Builder()
			.connectTimeout(30, TimeUnit.SECONDS)
			.readTimeout(40, TimeUnit.SECONDS)
			.build()
	
	private inline fun <reified T> postRequestGetJsonObject(
		body: RequestBody,
		keyName: String,
		path: String,
		justData: Boolean = false,
		noinline errorCallback: () -> Unit = {},
		crossinline hold: (List<T>) -> Unit
	) {
		getcryptoRequest(body, path) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					errorCallback()
					LogUtil.error(path, error)
				}
				
				override fun onResponse(
					call: Call,
					response: Response
				) {
					val data = AesCrypto.decrypt(response.body()?.string().orEmpty())
					try {
						val dataObject = data?.toJsonObject() ?: JSONObject("")
						val jsonData = dataObject[keyName].toString()
						if (justData) {
							hold(listOf(jsonData as T))
							return
						}
						val gson = Gson()
						val collectionType = object : TypeToken<Collection<T>>() {}.type
						hold(gson.fromJson(jsonData, collectionType))
					} catch (error: Exception) {
						LogUtil.error(keyName, error)
						GoldStoneCode.showErrorCodeReason(data, errorCallback)
					}
				}
			})
		}
	}
	
	private fun postRequest(
		body: RequestBody,
		path: String,
		netWorkError: () -> Unit = {},
		hold: (String) -> Unit
	) {
		getcryptoRequest(body, path) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(
					call: Call,
					error: IOException
				) {
					LogUtil.error(path, error)
					netWorkError()
				}
				
				override fun onResponse(
					call: Call,
					response: Response
				) {
					val data = AesCrypto.decrypt(response.body()?.string().orEmpty())
					try {
						hold(data.orEmpty())
					} catch (error: Exception) {
						LogUtil.error(path, error)
					}
				}
			})
		}
	}
	
	@JvmStatic
	private inline fun <reified T> requestData(
		api: String,
		keyName: String,
		justGetData: Boolean = false,
		crossinline netWorkError: () -> Unit = {},
		crossinline hold: List<T>.() -> Unit
	) {
		getcryptGetRequest(api) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					netWorkError()
					LogUtil.error(keyName + "requestData", error)
				}
				
				override fun onResponse(call: Call, response: Response) {
					val data = AesCrypto.decrypt(response.body()?.string().orEmpty())
					try {
						val dataObject =
							data?.toJsonObject()
							?: JSONObject("")
						val jsonData = if (keyName.isEmpty()) data else dataObject[keyName].toString()
						if (justGetData) {
							hold(listOf(jsonData as T))
						} else {
							val gson = Gson()
							val collectionType = object : TypeToken<Collection<T>>() {}.type
							hold(gson.fromJson(jsonData, collectionType))
						}
					} catch (error: Exception) {
						netWorkError()
						GoldStoneCode.showErrorCodeReason(data)
						LogUtil.error("$keyName requestData", error)
					}
				}
			})
		}
	}
	
	/** 请求 ehterScan 的数据是明文请求不需要加密 */
	@JvmStatic
	private inline fun <reified T> requestUncryptoData(
		api: String,
		keyName: String,
		justGetData: Boolean = false,
		crossinline errorCallback: (Exception) -> Unit = {},
		crossinline hold: List<T>.() -> Unit
	) {
		val client =
			OkHttpClient.Builder().build()
		val request = Request.Builder().url(api).build()
		client.newCall(request).enqueue(object : Callback {
			override fun onFailure(
				call: Call,
				error: IOException
			) {
				GoldStoneAPI.context.runOnUiThread { errorCallback(error) }
				LogUtil.error(keyName, error)
			}
			
			override fun onResponse(
				call: Call,
				response: Response
			) {
				val data = response.body()?.string()
				try {
					val dataObject =
						data?.toJsonObject()
						?: JSONObject("")
					val jsonData = if (keyName.isEmpty()) data else dataObject[keyName].toString()
					if (justGetData) {
						hold(listOf(jsonData as T))
					} else {
						val gson = Gson()
						val collectionType = object : TypeToken<Collection<T>>() {}.type
						hold(gson.fromJson(jsonData, collectionType))
					}
				} catch (error: Exception) {
					GoldStoneAPI.context.runOnUiThread { errorCallback(error) }
					LogUtil.error(keyName, error)
					GoldStoneCode.showErrorCodeReason(data)
				}
			}
		})
	}
	
	/** —————————————————— header 加密请求参数准备 ——————————————————————*/
	fun getcryptoRequest(
		body: RequestBody,
		path: String,
		callback: (Request) -> Unit
	) {
		val timeStamp = System.currentTimeMillis().toString()
		val version = SystemUtils.getVersionCode(GoldStoneAPI.context).toString()
		AppConfigTable.getAppConfig {
			it?.apply {
				val sign =
					(goldStoneID + "0" + GoldStoneCrayptoKey.apiKey + timeStamp + version)
						.getObjectMD5HexString()
						.removePrefix("0x")
				val request =
					Request.Builder().url(path).method("POST", body)
						.header("Content-type", "application/json")
						.addHeader("device", goldStoneID)
						.addHeader("timestamp", timeStamp)
						.addHeader("os", "0")
						.addHeader("version", version)
						.addHeader("sign", sign)
						.addHeader("chainid", chainID)
						.build()
				callback(request)
			}
		}
	}
	
	fun getcryptGetRequest(
		api: String,
		callback: (Request) -> Unit
	) {
		val timeStamp = System.currentTimeMillis().toString()
		val version = SystemUtils.getVersionCode(GoldStoneAPI.context).toString()
		AppConfigTable.getAppConfig {
			it?.apply {
				val sign =
					(goldStoneID + "0" + GoldStoneCrayptoKey.apiKey + timeStamp + version)
						.getObjectMD5HexString()
						.removePrefix("0x")
				val request =
					Request.Builder()
						.url(api)
						.header("Content-type", "application/json")
						.addHeader("device", goldStoneID)
						.addHeader("timestamp", timeStamp)
						.addHeader("os", "0")
						.addHeader("version", version)
						.addHeader("sign", sign)
						.addHeader("chainid", chainID)
						.build()
				callback(request)
			}
		}
	}
}

object GoldStoneCode {
	fun isSuccess(
		code: Any,
		callback: (isSuccessful: Boolean) -> Unit
	) {
		if (code == 0) callback(true)
		else {
			callback(false)
			LogUtil.error("function: GoldStoneCode, wrongCode: $code")
		}
	}
	
	fun showErrorCodeReason(data: String?, errorCallback: () -> Unit = {}) {
		data?.apply {
			val code = JSONObject(this).safeGet("code")
			if (code.isNotEmpty()) {
				when (code.toInt()) {
					-1 -> {
						errorCallback()
						LogUtil.error("Server Error GoldStone")
					}
					
					-4 -> {
						errorCallback()
						LogUtil.error("Url Error")
						/**
						 *  `Device` 错误, `APi URL` 是否正确, `API` 文档是否有错误
						 */
					}
				}
			}
		}
	}
}





