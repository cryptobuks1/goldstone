@file:Suppress("DEPRECATION")

package io.goldstone.blockchain.crypto.bitcoin

import io.goldstone.blockchain.crypto.bitcoincash.BCHWalletUtils
import io.goldstone.blockchain.crypto.litecoin.LitecoinNetParams
import io.goldstone.blockchain.kernel.network.bitcoin.model.UnspentModel
import org.bitcoinj.core.*
import org.bitcoinj.core.Utils.HEX
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder

/**
 * @date 2018/7/20 11:36 AM
 * @author KaySaith
 */
object BTCSeriesTransactionUtils {

	fun generateBTCSignedRawTransaction(
		sendValue: Long,
		fee: Long,
		toAddress: String,
		changeAddress: String,
		unspentModel: List<UnspentModel>,
		base58Privatekey: String,
		isTest: Boolean
	): BTCSignedModel {
		val net =
			if (isTest) TestNet3Params.get()
			else MainNetParams.get()
		return generateSignedRawTransaction(
			sendValue,
			fee,
			toAddress,
			changeAddress,
			unspentModel,
			base58Privatekey,
			net,
			false
		)
	}

	fun generateBCHSignedRawTransaction(
		sendValue: Long,
		fee: Long,
		toAddress: String,
		changeAddress: String,
		unspentModel: List<UnspentModel>,
		base58Privatekey: String,
		isTest: Boolean
	): BTCSignedModel {
		val net =
			if (isTest) TestNet3Params.get()
			else MainNetParams.get()
		return generateSignedRawTransaction(
			sendValue,
			fee,
			toAddress,
			changeAddress,
			unspentModel,
			base58Privatekey,
			net,
			true
		)
	}

	fun generateLTCSignedRawTransaction(
		sendValue: Long,
		fee: Long,
		toAddress: String,
		changeAddress: String,
		unspentModel: List<UnspentModel>,
		base58Privatekey: String,
		isTest: Boolean
	): BTCSignedModel {
		val net =
			if (isTest) TestNet3Params.get()
			else LitecoinNetParams()
		return generateSignedRawTransaction(
			sendValue,
			fee,
			toAddress,
			changeAddress,
			unspentModel,
			base58Privatekey,
			net,
			false
		)
	}

	private fun generateSignedRawTransaction(
		sendValue: Long,
		fee: Long,
		toAddress: String,
		changeAddress: String,
		unspentModel: List<UnspentModel>,
		base58Privatekey: String,
		network: NetworkParameters,
		isBCH: Boolean
	): BTCSignedModel {
		// 传入主网参数
		val transaction = Transaction(network)
		val privateKey =
			DumpedPrivateKey.fromBase58(network, base58Privatekey)
		val ecKey = privateKey.key
		var money = 0L
		val utxos = arrayListOf<UTXO>()
		unspentModel.forEach {
			// 当消费列表某几个 `item` 的值加起来大于实际转账金额+手续费,
			// 就跳出循环, 这个时候就得到了合符条件的 `utxos` 数组
			if (money >= (sendValue + fee)) {
				return@forEach
			}
			// 遍历 `unspents`, 组装合适的 `item`
			val utxo = UTXO(
				Sha256Hash.wrap(it.txid),
				it.outputNumber,
				Coin.valueOf(it.value),
				it.confirmations,
				false,
				Script(HEX.decode(it.scriptPubKey))
			)
			utxos.add(utxo)
			// 把消费列表的值加起来
			money += it.value
		}
		// 输出-转给别人
		transaction.addOutput(
			Coin.valueOf(sendValue),
			Address.fromBase58(network, BCHWalletUtils.formattedToLegacy(toAddress, network))
		)
		// 消费列表总金额 - 已经转账的金额 - 手续费 就等于需要返回给自己的金额了
		val leave = money - sendValue - fee
		if (leave > 0) {
			// 输出-转给自己
			transaction.addOutput(
				Coin.valueOf(leave),
				Address.fromBase58(network, BCHWalletUtils.formattedToLegacy(changeAddress, network))
			)
		}
		// 输入未消费列表项
		utxos.forEach { utxo ->
			val outPoint = TransactionOutPoint(network, utxo.index, utxo.hash)
			if (isBCH) {
				// `BCH` 的签名需要 `ForkID` 控件里的方法有 `BUG` 这里重新自定义了有方法
				transaction.addBCHSignedInput(
					outPoint,
					utxo.script,
					ecKey,
					utxo.value,
					Transaction.SigHash.ALL,
					true,
					true
				)
			} else {
				transaction.addSignedInput(
					outPoint,
					utxo.script,
					ecKey,
					Transaction.SigHash.ALL,
					true
				)
			}
		}
		return BTCSignedModel(HEX.encode(transaction.bitcoinSerialize()), transaction.messageSize)
	}
}

@Throws(ScriptException::class)
fun Transaction.addBCHSignedInput(
	prevOut: TransactionOutPoint,
	scriptPubKey: Script,
	sigKey: ECKey,
	value: Coin,
	sigHash: Transaction.SigHash,
	anyoneCanPay: Boolean,
	forkId: Boolean
): TransactionInput {

	val input = TransactionInput(params, this, byteArrayOf(), prevOut)
	addInput(input)
	val hash = if (forkId)
		hashForSignatureWitness(inputs.size - 1, scriptPubKey, value, sigHash, anyoneCanPay)
	else
		hashForSignature(inputs.size - 1, scriptPubKey, sigHash, anyoneCanPay)

	val ecSig = sigKey.sign(hash)
	val txSig = TransactionSignature(ecSig, sigHash, anyoneCanPay, forkId)
	when {
		scriptPubKey.isSentToRawPubKey -> input.scriptSig = ScriptBuilder.createInputScript(txSig)
		scriptPubKey.isSentToAddress -> input.scriptSig = ScriptBuilder.createInputScript(txSig, sigKey)
		else -> throw ScriptException("Don't know how to sign for this kind of scriptPubKey: $scriptPubKey")
	}
	return input
}
