package io.goldstone.blockchain.kernel.network.eos

import android.support.annotation.UiThread
import io.goldstone.blockchain.common.error.GoldStoneError
import io.goldstone.blockchain.crypto.eos.EOSCodeName
import io.goldstone.blockchain.crypto.eos.EOSTransactionMethod
import io.goldstone.blockchain.crypto.eos.EOSTransactionSerialization
import io.goldstone.blockchain.crypto.eos.account.EOSAccount
import io.goldstone.blockchain.crypto.eos.transaction.*
import io.goldstone.blockchain.crypto.multichain.CoinSymbol
import io.goldstone.blockchain.kernel.network.eos.contract.EOSTransactionInterface
import java.io.Serializable
import java.math.BigInteger


/**
 * @author KaySaith
 * @date  2018/09/14
 * @description
 *  因为 EOS 的转账之前需要查询 链上的 ChainInf 做为签名的一部分,
 *  所以这个类放到了 NetWork EOS 里面
 */
class EOSTransaction(
	/** "{\"actor\":\"fromAccountName\",\"permission\":\"active\"}" */
	private val fromAccount: EOSAuthorization,
	private val toAccountName: String,
	private val amount: BigInteger,
	private val memo: String,
	private val expirationType: ExpirationType,
	private val symbol: String = CoinSymbol.eos
) : Serializable, EOSTransactionInterface() {

	override fun serialized(
		errorCallback: (GoldStoneError) -> Unit,
		@UiThread hold: (EOSTransactionSerialization) -> Unit
	) {
		val transactionInfo = EOSTransactionInfo(
			EOSAccount(fromAccount.actor),
			EOSAccount(toAccountName),
			amount,
			memo,
			symbol
		)
		val transactionInfoCode = transactionInfo.serialize()
		EOSAPI.getTransactionHeaderFromChain(expirationType, errorCallback) { header ->
			val authorization = fromAccount
			val authorizationObjects = EOSAuthorization.createMultiAuthorizationObjects(authorization)
			// 准备 Action
			val action = EOSAction(
				EOSCodeName.EOSIOToken,
				transactionInfoCode,
				EOSTransactionMethod.Transfer,
				authorizationObjects
			)
			EOSTransactionUtils.serialize(
				EOSChain.Test,
				header,
				listOf(action),
				listOf(authorization),
				transactionInfoCode
			).let(hold)
		}
	}
}