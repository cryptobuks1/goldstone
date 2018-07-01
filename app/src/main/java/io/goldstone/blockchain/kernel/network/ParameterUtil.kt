@file:Suppress("IMPLICIT_CAST_TO_ANY")

package io.goldstone.blockchain.kernel.network

import com.blinnnk.extension.isNull
import io.goldstone.blockchain.common.utils.AesCrypto
import io.goldstone.blockchain.common.value.Config

/**
 * @date 2018/6/17 2:16 PM
 * @author KaySaith
 */
object ParameterUtil {
	
	fun <T> prepare(
		isEncrypt: Boolean = true,
		vararg parameters: Pair<String, T>
	): String {
		var content = ""
		parameters.forEach {
			val value = if (it.second is String) {
				"\"${it.second}\""
			} else it.second
			content += "\"${it.first}\":$value,"
		}
		return if (isEncrypt) {
			AesCrypto.encrypt("{${content.substringBeforeLast(",")}}").orEmpty()
		} else {
			"{${content.substringBeforeLast(",")}}"
		}
	}
	
	fun <T> prepareJsonRPC(
		isEncrypt: Boolean = Config.isEncryptERCNodeRequest(),
		method: String,
		id: Int,
		hasLatest: Boolean,
		vararg parameters: T?
	): String {
		var content = ""
		parameters.forEach {
			if (it.isNull()) return@forEach
			val value = if (it is String) "\"$it\"" else it
			content += "$value,"
		}
		val latest = if (hasLatest) ",\"latest\"" else ""
		val finalParameter =
			if (content.isEmpty()) ""
			else content.substringBeforeLast(",") + latest
		val rpcContent =
			"{\"jsonrpc\":\"2.0\", \"method\":\"$method\", \"params\":[$finalParameter], \"id\":$id}"
		return if (isEncrypt) AesCrypto.encrypt(rpcContent).orEmpty() else rpcContent
	}
	
	fun <T> preparePairJsonRPC(
		isEncrypt: Boolean = Config.isEncryptERCNodeRequest(),
		method: String,
		hasLatest: Boolean,
		vararg parameters: Pair<String, T>
	): String {
		var content = ""
		parameters.forEach {
			val value = if (it.second is String) {
				"\"${it.second}\""
			} else it.second
			content += "\"${it.first}\":$value,"
		}
		val latest = if (hasLatest) ",\"latest\"" else ""
		val rpcContent =
			"{\"jsonrpc\":\"2.0\", \"method\":\"$method\", \"params\":[{${content.substringBeforeLast(",")}}$latest],\"id\":1}"
		return if (isEncrypt) AesCrypto.encrypt(rpcContent).orEmpty() else rpcContent
	}
}