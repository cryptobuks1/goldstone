package io.goldstone.blockchain.crypto.eos.eosram

import io.goldstone.blockchain.crypto.eos.EOSCodeName
import io.goldstone.blockchain.crypto.eos.EOSUtils
import io.goldstone.blockchain.crypto.eos.base.EOSModel
import io.goldstone.blockchain.crypto.eos.transaction.EOSAuthorization
import io.goldstone.blockchain.crypto.eos.transaction.EOSTransactionInfo
import io.goldstone.blockchain.crypto.multichain.CoinSymbol
import io.goldstone.blockchain.module.common.tokendetail.eosresourcetrading.common.basetradingfragment.view.StakeType
import java.io.Serializable
import java.math.BigInteger

/**
 * @author KaySaith
 * @date 2018/09/05
 */


data class EOSRamModel(
	val authorizations: List<EOSAuthorization>,
	val payerName: String,
	val receiverName: String,
	val eosAmount: BigInteger,
	val isBuying: Boolean
) : Serializable, EOSModel {
	@Throws
	override fun createObject(): String {
		var authorizationObjects = ""
		authorizations.forEach {
			authorizationObjects += it.createObject() + ","
		}
		authorizationObjects = authorizationObjects.substringBeforeLast(",")
		val eosCount = EOSUtils.convertAmountToValidFormat(eosAmount)
		val method =
			if (isBuying) StakeType.BuyRam else StakeType.SellRam
		return "{\"account\":\"eosio\",\"name\":\"${method.value}\",\"authorization\":[$authorizationObjects],\"data\":{\"payer\":\"$payerName\",\"receiver\":\"$receiverName\",\"quant\":\"$eosCount ${CoinSymbol.eos}\"},\"hex_data\":\"\"}"
	}

	override fun serialize(): String {
		val method =
			if (isBuying) StakeType.BuyRam else StakeType.SellRam
		val serializedAccount = EOSUtils.getLittleEndianCode(EOSCodeName.EOSIO.value)
		val serializedMethodName = EOSUtils.getLittleEndianCode(method.value)
		val serializedAuthorizationSize = EOSUtils.getVariableUInt(authorizations.size)
		var serializedAuthorizations = ""
		authorizations.forEach {
			serializedAuthorizations += it.serialize()
		}
		val serializedBuyInfo = EOSTransactionInfo(
			payerName,
			receiverName,
			eosAmount
		).serialize()
		val hexDataLength = EOSUtils.getHexDataByteLengthCode(serializedBuyInfo)
		return serializedAccount + serializedMethodName + serializedAuthorizationSize +
			serializedAuthorizations + hexDataLength + serializedBuyInfo
	}
}