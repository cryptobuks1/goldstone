package io.goldstone.blockchain.kernel.network

import com.blinnnk.extension.safeGet
import com.blinnnk.util.SystemUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.goldstone.blockchain.common.utils.AesCrypto
import io.goldstone.blockchain.common.utils.LogUtil
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.value.ErrorTag
import io.goldstone.blockchain.common.value.GoldStoneCrayptoKey
import io.goldstone.blockchain.common.value.currentChannel
import io.goldstone.blockchain.crypto.toJsonObject
import io.goldstone.blockchain.crypto.utils.getObjectMD5HexString
import io.goldstone.blockchain.kernel.commonmodel.AppConfigTable
import okhttp3.*
import org.jetbrains.anko.runOnUiThread
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @date 2018/6/12 4:34 PM
 * @author KaySaith
 */
object RequisitionUtil {
	
	inline fun <reified T> postRequestGetJsonObject(
		body: RequestBody,
		keyName: String,
		path: String,
		justData: Boolean = false,
		noinline errorCallback: (Exception) -> Unit,
		isEncrypt: Boolean = Config.isEncryptERCNodeRequest(),
		crossinline hold: (List<T>) -> Unit
	) {
		val client =
			OkHttpClient
				.Builder()
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build()
		
		getcryptoRequest(body, path, isEncrypt) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					GoldStoneAPI.context.runOnUiThread {
						errorCallback(error)
					}
					LogUtil.error(path, error)
				}
				
				override fun onResponse(call: Call, response: Response) {
					val data =
						if (isEncrypt) AesCrypto.decrypt(response.body()?.string().orEmpty())
						else response.body()?.string().orEmpty()
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
						GoldStoneCode.showErrorCodeReason(data) {
							GoldStoneAPI.context.runOnUiThread {
								errorCallback(error)
							}
						}
					}
				}
			})
		}
	}
	
	fun postRequest(
		body: RequestBody,
		path: String,
		netWorkError: (Exception) -> Unit,
		isEncrypt: Boolean = Config.isEncryptERCNodeRequest(),
		hold: (String) -> Unit
	) {
		val client =
			OkHttpClient
				.Builder()
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build()
		
		getcryptoRequest(body, path, isEncrypt) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					LogUtil.error(path, error)
					GoldStoneAPI.context.runOnUiThread {
						netWorkError(error)
					}
				}
				
				override fun onResponse(call: Call, response: Response) {
					val data =
						if (isEncrypt) AesCrypto.decrypt(response.body()?.string().orEmpty())
						else response.body()?.string().orEmpty()
					try {
						hold(data.orEmpty())
					} catch (error: Exception) {
						GoldStoneAPI.context.runOnUiThread {
							netWorkError(error)
						}
						LogUtil.error(path, error)
					}
				}
			})
		}
	}
	
	@JvmStatic
	inline fun <reified T> requestData(
		api: String,
		keyName: String,
		justGetData: Boolean = false,
		crossinline errorCallback: (Exception) -> Unit,
		isEncrypt: Boolean = Config.isEncryptERCNodeRequest(),
		maxConnectTime: Long = 20,
		crossinline hold: List<T>.() -> Unit
	) {
		val client =
			OkHttpClient
				.Builder()
				.connectTimeout(maxConnectTime, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build()
		
		getcryptGetRequest(api, isEncrypt) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					GoldStoneAPI.context.runOnUiThread {
						errorCallback(error)
					}
					LogUtil.error(keyName + "requestData", error)
				}
				
				override fun onResponse(call: Call, response: Response) {
					val data =
						if (isEncrypt) AesCrypto.decrypt(response.body()?.string().orEmpty())
						else response.body()?.string().orEmpty()
					// 结果返回为 `Empty` 或 `Null`
					if (data.isNullOrBlank()) {
						LogUtil.error("$keyName data.isNullOrBlank")
						errorCallback(Exception())
						GoldStoneCode.showErrorCodeReason(data)
						return
					}
					
					try {
						val dataObject = data?.toJsonObject() ?: JSONObject("")
						val jsonData = if (keyName.isEmpty()) data else dataObject[keyName].toString()
						if (justGetData) {
							hold(listOf(jsonData as T))
						} else {
							val gson = Gson()
							val collectionType = object : TypeToken<Collection<T>>() {}.type
							hold(gson.fromJson(jsonData, collectionType))
						}
					} catch (error: Exception) {
						GoldStoneAPI.context.runOnUiThread {
							LogUtil.error("Error in onResponse Try Catch")
							errorCallback(error)
						}
						GoldStoneCode.showErrorCodeReason(data)
						LogUtil.error("$keyName requestData", error)
					}
				}
			})
		}
	}
	
	/** 请求 ehterScan, blockchain.info 的数据是明文请求不需要加密 */
	@JvmStatic
	inline fun <reified T> requestUncryptoData(
		api: String,
		keyName: String,
		justGetData: Boolean = false,
		crossinline errorCallback: (Throwable) -> Unit,
		crossinline hold: List<T>.() -> Unit
	) {
		val client =
			OkHttpClient
				.Builder()
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build()
		val request =
			Request.Builder().url(api).build()
		client.newCall(request).enqueue(object : Callback {
			override fun onFailure(call: Call, error: IOException) {
				GoldStoneAPI.context.runOnUiThread { errorCallback(error) }
				LogUtil.error("$api $keyName", error)
			}
			
			override fun onResponse(call: Call, response: Response) {
				val data = response.body()?.string()
				try {
					val dataObject = data?.toJsonObject() ?: JSONObject("")
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
		isEncrypt: Boolean = Config.isEncryptERCNodeRequest(),
		callback: (Request) -> Unit
	) {
		if (isEncrypt) {
			AppConfigTable.getAppConfig {
				it?.apply {
					val timeStamp = System.currentTimeMillis().toString()
					val version = SystemUtils.getVersionCode(GoldStoneAPI.context).toString()
					val sign =
						(goldStoneID + "0" + GoldStoneCrayptoKey.apiKey + timeStamp + version)
							.getObjectMD5HexString()
					val request =
						Request.Builder()
							.url(path)
							.method("POST", body)
							.header("Content-type", "application/json")
							.addHeader("device", goldStoneID)
							.addHeader("timestamp", timeStamp)
							.addHeader("os", "0")
							.addHeader("version", version)
							.addHeader("sign", sign)
							.build()
					callback(request)
				}
			}
		} else {
			val request =
				Request.Builder()
					.url(path)
					.method("POST", body)
					.header("Content-type", "application/json")
					.build()
			callback(request)
		}
	}
	
	fun getcryptGetRequest(
		api: String,
		isEncrypt: Boolean = Config.isEncryptERCNodeRequest(),
		callback: (Request) -> Unit
	) {
		val timeStamp = System.currentTimeMillis().toString()
		val version = SystemUtils.getVersionCode(GoldStoneAPI.context).toString()
		AppConfigTable.getAppConfig {
			it?.apply {
				if (isEncrypt) {
					val sign =
						(goldStoneID + "0" + GoldStoneCrayptoKey.apiKey + timeStamp + version)
							.getObjectMD5HexString()
					val request =
						Request.Builder()
							.url(api)
							.header("Content-type", "application/json")
							.addHeader("device", goldStoneID)
							.addHeader("timestamp", timeStamp)
							.addHeader("os", "0")
							.addHeader("version", version)
							.addHeader("sign", sign)
							.addHeader("channel", currentChannel.value)
							.build()
					callback(request)
				} else {
					val uncryptRequest = Request.Builder()
						.url(api)
						.header("Content-type", "application/json")
						.build()
					callback(uncryptRequest)
				}
			}
		}
	}
	
	fun callChainBy(
		body: RequestBody,
		errorCallback: (error: Throwable?, reason: String?) -> Unit,
		chainName: String = Config.getCurrentChainName(),
		hold: (String) -> Unit
	) {
		val isEncrypt = ChainURL.uncryptChainName.none { it.equals(chainName, true) }
		val client = OkHttpClient
			.Builder()
			.connectTimeout(40, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.build()
		val chainUrl =
			if (ChainURL.etcChainName.any { it.equals(chainName, true) })
				ChainURL.currentETCChain(chainName)
			else ChainURL.currentChain(chainName)
		getcryptoRequest(body, chainUrl, isEncrypt) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					GoldStoneAPI.context.runOnUiThread {
						errorCallback(error, "Call Ethereum Failured")
					}
				}
				
				override fun onResponse(call: Call, response: Response) {
					val data =
						if (isEncrypt) AesCrypto.decrypt(response.body()?.string().orEmpty())
						else response.body()?.string().orEmpty()
					checkChainErrorCode(data).let {
						if (it.isNotEmpty()) {
							GoldStoneAPI.context.runOnUiThread {
								errorCallback(null, it)
							}
							return
						}
					}
					try {
						val dataObject = data?.toJsonObject() ?: JSONObject("")
						hold(dataObject.safeGet("result"))
					} catch (error: Exception) {
						GoldStoneAPI.context.runOnUiThread {
							errorCallback(error, "onResponse Error in $chainName")
						}
					}
				}
			})
		}
	}
	
	private fun checkChainErrorCode(data: String?): String {
		val hasError = data?.contains("error")
		val errorData: String
		if (hasError == true) {
			errorData = JSONObject(data).safeGet("error")
		} else {
			val code =
				if (data?.contains("code") == true)
					JSONObject(data).get("code")?.toString()?.toIntOrNull()
				else null
			return if (code == -10) ErrorTag.chain
			else ""
		}
		return when {
			data.isNullOrBlank() -> return ""
			
			errorData.isNotEmpty() -> {
				try {
					if (errorData.equals("null", true)) ""
					else JSONObject(errorData).safeGet("message")
				} catch (error: Exception) {
					"$error"
				}
			}
			
			else -> ""
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
			val code = try {
				JSONObject(this).safeGet("code")
			} catch (error: Exception) {
				"100"
			}
			if (code.isNotEmpty()) {
				when (code.toInt()) {
					-1 -> {
						GoldStoneAPI.context.runOnUiThread {
							errorCallback()
						}
						LogUtil.error("Server Error GoldStone")
					}
					
					-4 -> {
						GoldStoneAPI.context.runOnUiThread {
							errorCallback()
						}
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