package io.goldstone.blockchain.module.common.walletgeneration.createwallet.model

/**
 * @date 2018/7/30 5:52 PM
 * @author KaySaith
 */
data class AddressCommitionModel(
	val address: String,
	val chainType: Int,
	val option: Int
)