package io.goldstone.blockchain.module.home.profile.profile.model

import com.blinnnk.extension.orZero
import com.blinnnk.extension.safeGet
import org.json.JSONObject

/**
 * @date 2018/6/4 6:04 PM
 * @author KaySaith
 */
data class VersionModel(
	val versionCode: Int,
	val versionName: String,
	val description: String,
	val url: String
) {
	
	constructor(data: JSONObject) : this(
		data.safeGet("version").toIntOrNull().orZero(),
		data.safeGet("version_name"),
		data.safeGet("description"),
		data.safeGet("download_url")
	)
}