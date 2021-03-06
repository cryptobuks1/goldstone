package io.goldstone.blinnnk.crypto.eos.transaction

import com.blinnnk.extension.safeGet
import io.goldstone.blinnnk.crypto.eos.EOSUtils
import io.goldstone.blinnnk.crypto.eos.accountregister.EOSActor
import io.goldstone.blinnnk.crypto.eos.base.EOSModel
import org.json.JSONObject
import java.io.Serializable

/**
 * @author KaySaith
 * @date 2018/09/03
 */

data class EOSAuthorization(
	val actor: String,
	val permission: EOSActor
) : Serializable, EOSModel {

	constructor(data: JSONObject) : this(
		data.safeGet("actor"),
		EOSActor.getActorByValue(data.safeGet("permission"))
	)

	override fun createObject(): String {
		return "{\"actor\":\"$actor\",\"permission\":\"${permission.value}\"}"
	}

	override fun serialize(): String {
		return "${EOSUtils.getLittleEndianCode(actor)}${EOSUtils.getLittleEndianCode(permission.value)}"
	}

	companion object {
		fun createMultiAuthorizationObjects(vararg authorizations: EOSAuthorization): String {
			var objects = ""
			authorizations.forEach {
				objects += it.createObject() + ","
			}
			return "[${objects.substringBeforeLast(",")}]"
		}
	}
}