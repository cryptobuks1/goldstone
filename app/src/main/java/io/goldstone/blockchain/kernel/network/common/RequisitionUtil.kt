package io.goldstone.blockchain.kernel.network.common

import android.support.annotation.WorkerThread
import com.blinnnk.extension.isNull
import com.blinnnk.extension.orZero
import com.blinnnk.extension.safeGet
import com.blinnnk.util.SystemUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.goldstone.blockchain.common.error.RequestError
import io.goldstone.blockchain.common.sharedpreference.SharedChain
import io.goldstone.blockchain.common.sharedpreference.SharedValue
import io.goldstone.blockchain.common.sharedpreference.SharedWallet
import io.goldstone.blockchain.common.utils.AesCrypto
import io.goldstone.blockchain.common.utils.LogUtil
import io.goldstone.blockchain.common.value.ErrorTag
import io.goldstone.blockchain.common.value.GoldStoneCryptoKey
import io.goldstone.blockchain.common.value.currentChannel
import io.goldstone.blockchain.crypto.keystore.toJsonObject
import io.goldstone.blockchain.crypto.utils.getObjectMD5HexString
import io.goldstone.blockchain.kernel.commonmodel.AppConfigTable
import io.goldstone.blockchain.kernel.network.ChainURL
import io.goldstone.blockchain.kernel.network.ethereum.GoldStoneEthCall
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

	fun post(
		condition: String,
		api: String,
		errorCallback: (RequestError) -> Unit,
		isEncrypt: Boolean,
		@WorkerThread hold: (String) -> Unit
	) {
		postRequest(
			RequestBody.create(
				GoldStoneEthCall.contentType,
				condition
			),
			api,
			errorCallback,
			isEncrypt,
			hold
		)
	}

	inline fun <reified T> post(
		condition: String,
		api: String,
		keyName: String,
		noinline errorCallback: (RequestError) -> Unit,
		isEncrypt: Boolean,
		@WorkerThread noinline hold: (List<T>) -> Unit
	) {
		postRequest(
			RequestBody.create(
				GoldStoneEthCall.contentType,
				condition
			),
			keyName,
			api,
			false,
			errorCallback,
			isEncrypt,
			hold
		)
	}

	inline fun <reified T> postSingle(
		condition: String,
		api: String,
		keyName: String,
		noinline errorCallback: (RequestError) -> Unit,
		isEncrypt: Boolean,
		@WorkerThread noinline holdSingle: (T?) -> Unit
	) {
		postRequest<T>(
			RequestBody.create(
				GoldStoneEthCall.contentType,
				condition
			),
			keyName,
			api,
			false,
			errorCallback,
			isEncrypt
		) {
			holdSingle(it.firstOrNull())
		}
	}

	fun postString(
		condition: String,
		api: String,
		keyName: String,
		errorCallback: (RequestError) -> Unit,
		isEncrypt: Boolean,
		@WorkerThread holdString: (String?) -> Unit
	) {
		postRequest<String>(
			RequestBody.create(
				GoldStoneEthCall.contentType,
				condition
			),
			keyName,
			api,
			true,
			errorCallback,
			isEncrypt
		) {
			holdString(it.firstOrNull())
		}
	}

	inline fun <reified T> postRequest(
		body: RequestBody,
		keyName: String,
		path: String,
		justData: Boolean = false,
		noinline errorCallback: (RequestError) -> Unit,
		isEncrypt: Boolean = SharedValue.isEncryptERCNodeRequest(),
		crossinline hold: (List<T>) -> Unit
	) {
		val client =
			OkHttpClient
				.Builder()
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build()
		getCryptoRequest(body, path, isEncrypt) { requestBody ->
			client.newCall(requestBody).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					GoldStoneAPI.context.runOnUiThread {
						errorCallback(RequestError.PostFailed("[API: ${path.getKeyName()}]\n[ERROR: $error]"))
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
						GoldStoneCode.showErrorCodeReason(data)
						GoldStoneAPI.context.runOnUiThread {
							errorCallback(RequestError.ResolveDataError(error))
						}
					}
				}
			})
		}
	}

	fun postRequest(
		body: RequestBody,
		path: String,
		errorCallback: (RequestError) -> Unit,
		isEncrypt: Boolean = SharedValue.isEncryptERCNodeRequest(),
		hold: (String) -> Unit
	) {
		val client =
			OkHttpClient
				.Builder()
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build()
		getCryptoRequest(body, path, isEncrypt) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					LogUtil.error(path, error)
					GoldStoneAPI.context.runOnUiThread {
						errorCallback(RequestError.PostFailed("[API: ${path.substringAfter("/")}]\n[ERROR: ${error.message}]"))
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
							errorCallback(RequestError.PostFailed("[API: ${path.getKeyName()}]\n[ERROR: $error]"))
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
		crossinline errorCallback: (RequestError) -> Unit,
		targetGoldStoneID: String? = null,
		isEncrypt: Boolean,
		maxConnectTime: Long = 20,
		crossinline hold: List<T>.() -> Unit
	) {
		val client =
			OkHttpClient
				.Builder()
				.connectTimeout(maxConnectTime, TimeUnit.SECONDS)
				.readTimeout(maxConnectTime, TimeUnit.SECONDS)
				.build()

		getCryptoGetRequest(api, isEncrypt, targetGoldStoneID) {
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					GoldStoneAPI.context.runOnUiThread {
						errorCallback(RequestError.PostFailed("[API: ${api.getKeyName()}]\n[ERROR: $error]"))
					}
				}

				override fun onResponse(call: Call, response: Response) {
					val data =
						if (isEncrypt) AesCrypto.decrypt(response.body()?.string().orEmpty())
						else response.body()?.string().orEmpty()
					// 结果返回为 `Empty` 或 `Null`
					if (data.isNullOrBlank()) {
						GoldStoneAPI.context.runOnUiThread {
							errorCallback(RequestError.NullResponse(keyName))
						}
						GoldStoneCode.showErrorCodeReason(data)
					} else try {
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
							errorCallback(RequestError.ResolveDataError(error))
						}
						GoldStoneCode.showErrorCodeReason(data)
					}
				}
			})
		}
	}

	/** 请求 ehterScan, blockchain.info 的数据是明文请求不需要加密 */
	@JvmStatic
	inline fun <reified T> requestUnCryptoData(
		api: String,
		keyName: String,
		justGetData: Boolean = false,
		crossinline errorCallback: (RequestError) -> Unit,
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
				GoldStoneAPI.context.runOnUiThread {
					errorCallback(RequestError.PostFailed("[API: ${api.getKeyName()}]\n[ERROR: $error]"))
				}
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
					GoldStoneAPI.context.runOnUiThread { errorCallback(RequestError.ResolveDataError(error)) }
					LogUtil.error(keyName, error)
					GoldStoneCode.showErrorCodeReason(data)
				}
			}
		})
	}

	fun String.getKeyName(): String {
		return if (contains("/")) substringAfterLast("/")
		else this
	}

	// `GoldStone` 加密规则的 `Header Request`
	private val generateRequest: (
		path: String,
		goldStoneID: String,
		bold: RequestBody?
	) -> Request = { path, goldStoneID, body ->
		val timeStamp = System.currentTimeMillis().toString()
		val version = SystemUtils.getVersionCode(GoldStoneAPI.context).toString()
		val sign =
			(goldStoneID + "0" + GoldStoneCryptoKey.apiKey + timeStamp + version).getObjectMD5HexString()
		Request.Builder()
			.url(path)
			.apply {
				if (!body.isNull()) method("POST", body)
			}
			.header("Content-type", "application/json")
			.addHeader("device", goldStoneID)
			.addHeader("timestamp", timeStamp)
			.addHeader("os", "0")
			.addHeader("version", version)
			.addHeader("sign", sign)
			.addHeader("channel", currentChannel.value)
			.build()
	}

	/** —————————————————— header 加密请求参数准备 ——————————————————————*/
	fun getCryptoRequest(
		body: RequestBody,
		path: String,
		isEncrypt: Boolean = SharedValue.isEncryptERCNodeRequest(),
		targetGoldStoneID: String = "",
		hold: (Request) -> Unit
	) {
		when {
			isEncrypt && targetGoldStoneID.isEmpty() -> AppConfigTable.getAppConfig {
				it?.apply {
					hold(generateRequest(path, goldStoneID, body))
				}
			}
			targetGoldStoneID.isNotEmpty() ->
				hold(generateRequest(path, SharedWallet.getGoldStoneID(), body))
			else -> hold(
				Request.Builder()
					.url(path)
					.method("POST", body)
					.header("Content-type", "application/json")
					.build()
			)

		}
	}

	fun getCryptoGetRequest(
		api: String,
		isEncrypt: Boolean,
		targetGoldStoneID: String? = null,
		hold: (Request) -> Unit
	) {
		when {
			isEncrypt && targetGoldStoneID.isNullOrBlank() -> AppConfigTable.getAppConfig {
				it?.apply { hold(generateRequest(api, goldStoneID, null)) }
			}
			targetGoldStoneID?.count().orZero() > 0 -> {
				hold(generateRequest(api, SharedWallet.getGoldStoneID(), null))
			}
			else -> {
				val uncryptRequest = Request.Builder()
					.url(api)
					.header("Content-type", "application/json")
					.build()
				hold(uncryptRequest)
			}
		}
	}

	fun callChainBy(
		body: RequestBody,
		errorCallback: (RequestError) -> Unit,
		chainName: String = SharedChain.getCurrentETHName(),
		hold: (String) -> Unit
	) {
		val isEncrypt = ChainURL.unencryptedChainName.none { it.equals(chainName, true) }
		val client = OkHttpClient
			.Builder()
			.connectTimeout(40, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.build()
		val chainUrl =
			if (ChainURL.etcChainName.any { it.equals(chainName, true) })
				ChainURL.currentETCChain(chainName)
			else ChainURL.currentChain(chainName)
		getCryptoRequest(body, chainUrl, isEncrypt) { it ->
			client.newCall(it).enqueue(object : Callback {
				override fun onFailure(call: Call, error: IOException) {
					GoldStoneAPI.context.runOnUiThread {
						errorCallback(RequestError.PostFailed("[CHAIN NAME: $chainName]\n[ERROR: $error]"))
					}
				}

				override fun onResponse(call: Call, response: Response) {
					val data =
						if (isEncrypt) AesCrypto.decrypt(response.body()?.string().orEmpty())
						else response.body()?.string().orEmpty()
					checkChainErrorCode(data).let {
						if (it.isNotEmpty()) {
							GoldStoneAPI.context.runOnUiThread {
								errorCallback(RequestError.ResolveDataError(Throwable(it)))
							}
							return
						}
					}
					try {
						val dataObject = data?.toJsonObject() ?: JSONObject("")
						hold(dataObject.safeGet("result"))
					} catch (error: Exception) {
						GoldStoneAPI.context.runOnUiThread {
							errorCallback(RequestError.ResolveDataError(Throwable("$error onResponse Error in $chainName")))
						}
					}
				}
			})
		}
	}

	private fun checkChainErrorCode(data: String?): String {
		val hasError = data?.contains("error")
		val errorData = if (hasError == true) {
			try {
				JSONObject(data).safeGet("error")
			} catch (error: Exception) {
				LogUtil.error("checkChainErrorCode", error)
				""
			}
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