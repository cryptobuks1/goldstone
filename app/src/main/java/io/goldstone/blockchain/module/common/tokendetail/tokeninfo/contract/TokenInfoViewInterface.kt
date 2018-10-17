package io.goldstone.blockchain.module.common.tokendetail.tokeninfo.contract

import android.graphics.Bitmap
import java.io.Serializable


/**
 * @author KaySaith
 * @date  2018/09/13
 */
// 实现头部的 `TokenInfo` 必须要实现的两个接口
interface TokenInfoViewInterface {
	fun setTokenInfo(
		qrCode: Bitmap?,
		title: String,
		subtitle: String,
		icon: Int,
		action: () -> Unit
	)

	fun updateLatestActivationDate(date: String)
}

data class EOSTokenCountInfo(
	val totalSent: Int,
	val totalReceived: Int,
	val totalCount: Int
): Serializable