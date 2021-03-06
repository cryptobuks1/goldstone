package io.goldstone.blinnnk.kernel.commontable

import android.arch.persistence.room.*
import android.support.annotation.WorkerThread
import com.blinnnk.extension.orZero
import com.blinnnk.extension.safeGet
import com.blinnnk.extension.toIntOrZero
import com.blinnnk.extension.toJSONObjectList
import io.goldstone.blinnnk.crypto.bitcoin.BTCUtils
import io.goldstone.blinnnk.crypto.bitcoincash.BCHUtil
import io.goldstone.blinnnk.crypto.bitcoincash.BCHWalletUtils
import io.goldstone.blinnnk.crypto.multichain.ChainType
import io.goldstone.blinnnk.crypto.multichain.isBCH
import io.goldstone.blinnnk.kernel.database.GoldStoneDataBase
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.json.JSONArray
import org.json.JSONObject

/**
 * @date 2018/7/26 11:20 PM
 * @author KaySaith
 */
@Entity(tableName = "bitcoinTransactionList", primaryKeys = ["hash", "recordAddress", "isFee"])
data class BTCSeriesTransactionTable(
	val dataIndex: Int, // 复杂的翻页机制需要和服务器映射的抽象角标
	var symbol: String,
	var blockNumber: Int,
	var transactionIndex: Int,
	var timeStamp: String,
	val hash: String,
	val fromAddress: String,
	val to: String,
	var recordAddress: String,
	var isReceive: Boolean,
	val value: String, // Double 0.5
	val fee: String,
	var size: String,
	var confirmations: Int,
	var isFee: Boolean,
	var isPending: Boolean,
	var chainType: Int
) {

	constructor(
		data: JSONObject,
		dataIndex: Int,
		myAddress: String,
		symbol: String,
		isFee: Boolean,
		chainType: Int
	) : this(
		dataIndex,
		symbol,
		data.safeGet("blockheight").toIntOrNull() ?: -1,
		0,
		data.safeGet("time"),
		data.safeGet("txid"),
		getFromAddress(data),
		getToAddresses(data).joinToString(",") { it },
		myAddress,
		isReceive(getFromAddress(data), myAddress),
		getTransactionValue(data, myAddress),
		data.safeGet("fees"),
		data.safeGet("size"),
		data.safeGet("confirmations").toIntOrZero(),
		isFee,
		// 因为 `BTC Series` 的账单会收录但是又不一定成功. 与 `以太坊` 收录即有状态不同
		// 所以 `Pending` 状态还是会根据是否拥有 `BlockHeight` 来做判断
		data.safeGet("confirmations").toIntOrZero() <= 6,
		chainType
	)

	companion object {
		@JvmField
		val dao = GoldStoneDataBase.database.btcSeriesTransactionDao()

		private fun isReceive(fromAddress: String, toAddress: String): Boolean {
			val formatToAddress =
				if (BCHWalletUtils.isNewCashAddress(fromAddress)) {
					if (!BCHWalletUtils.isNewCashAddress(toAddress))
						BCHUtil.instance
							.encodeCashAddressByLegacy(toAddress)
							.substringAfter(":")
					else {
						if (toAddress.contains(":")) toAddress.substringAfter(":")
						else toAddress
					}
				} else {
					if (BTCUtils.isValidTestnetAddress(fromAddress))
						BCHWalletUtils.formattedToLegacy(toAddress, TestNet3Params.get())
					else BCHWalletUtils.formattedToLegacy(toAddress, MainNetParams.get())
				}
			return !fromAddress.equals(formatToAddress, true)
		}

		private fun convertToBCHOrDefaultAddress(myAddress: String, targetAddress: String): String {
			return if (BCHWalletUtils.isNewCashAddress(targetAddress)) {
				val myAddressIsLegacy = !BCHWalletUtils.isNewCashAddress(myAddress)
				if (myAddressIsLegacy)
					BCHUtil.instance
						.encodeCashAddressByLegacy(myAddress)
						.substringAfter(":")
				else {
					if (myAddress.contains(":")) myAddress.substringAfter(":")
					else myAddress
				}
			} else {
				if (BTCUtils.isValidTestnetAddress(targetAddress))
					BCHWalletUtils.formattedToLegacy(myAddress, TestNet3Params.get())
				else BCHWalletUtils.formattedToLegacy(myAddress, MainNetParams.get())
			}
		}

		private fun getFromAddress(data: JSONObject): String {
			val inputs = JSONArray(data.safeGet("vin")).toJSONObjectList()
			return inputs.first().safeGet("addr")
		}

		private fun getToAddresses(
			data: JSONObject
		): List<String> {
			val out = JSONArray(data.safeGet("vout")).toJSONObjectList()
			var toAddresses = listOf<String>()
			/**
			 * 个别的账单会出现 `BCH` 的 `VOut ScriptPubKey` 出现没有 `Addresses` 的 `Key` 值, 这里额外做处理判断一下
			 *  E.G. TXID: 961a13441cd57c89b9d04bb259cf72b74c5e8c8163322d06c064cd7290b8cd6b
			 * */
			(0 until out.size).forEach {
				val scriptPubKeyObject = out[it].safeGet("scriptPubKey")
				if (scriptPubKeyObject.contains("addresses")) {
					toAddresses += JSONArray(JSONObject(scriptPubKeyObject).safeGet("addresses"))[0].toString()
				}
			}
			// 如果发起地址里面有我的地址, 那么接收地址就是 `Out` 里面不等于我的及找零地址的地址.
			val finalToAddress = toAddresses.filterNot {
				it.equals(getFromAddress(data), true)
			}
			return if (finalToAddress.isEmpty()) toAddresses.subList(0, 1) else finalToAddress
		}

		private fun getTransactionValue(data: JSONObject, myAddress: String): String {
			val totalWithoutFee = data.safeGet("valueOut").toDoubleOrNull().orZero()
			return (totalWithoutFee - getChangeValue(myAddress, data).toDoubleOrNull().orZero()).toString()
		}

		/**
		 * 理论上, 比特币的转账地址都可以定义为找零地址, 而若当用户更改不为人所知的自己可以控制的 `ChangeAddress`
		 * 我们是无从得知的。这里我们假定输出地址就是发起转账的地址为找零地址。并把对应的 `Value` 定义为 `ChangeValue`、
		 * 或发起地址不为我自己, `Out` 地址中去除我的地址的部分为 `ChangeValue`
		 */
		private fun getChangeValue(
			toAddress: String,
			data: JSONObject
		): String {
			val out = JSONArray(data.safeGet("vout")).toJSONObjectList()
			var changeValue = 0.0
			val formatToAddress = convertToBCHOrDefaultAddress(toAddress, getFromAddress(data))
			val mineIsTo = getFromAddress(data).equals(formatToAddress, true)
			var countForCalculateTransferToMySelf = out.size
			(0 until out.size).forEach {
				val child = JSONObject(out[it].toString())
				/**
				 * 个别的账单会出现 `BCH` 的 `VOut ScriptPubKey` 出现没有 `Addresses` 的 `Key` 值, 这里额外做处理判断一下
				 *  E.G. TXID: 961a13441cd57c89b9d04bb259cf72b74c5e8c8163322d06c064cd7290b8cd6b
				 * */
				val scriptPubKeyObject = child.safeGet("scriptPubKey")
				if (scriptPubKeyObject.contains("addresses")) {
					val childAddress = JSONArray(JSONObject(child.safeGet("scriptPubKey")).safeGet("addresses"))[0].toString()
					changeValue +=
						if (childAddress.equals(formatToAddress, true) == mineIsTo) {
							countForCalculateTransferToMySelf -= 1
							// 如果 `countForCalculateTransferToMySelf` 的值是 `0` 那么意味着是自己转给自己
							//  这种情况留一个值作为 `TransferValue`
							if (countForCalculateTransferToMySelf != 0) {
								child.safeGet("value").toDoubleOrNull().orZero()
							} else 0.0
						} else {
							0.0
						}
				}
			}
			return changeValue.toString()
		}

		@WorkerThread
		fun deleteByAddress(address: String, chainType: ChainType) {
			// `BCH` 的 `insight` 账单是新地址格式, 本地的测试网是公用的 `BTCTest Legacy` 格式,
			// 删除多链钱包的时候需要额外处理一下这种情况的地址比对
			val formattedAddress =
				if (chainType.isBCH() && !BCHWalletUtils.isNewCashAddress(address))
					BCHWalletUtils.formattedToLegacy(address, TestNet3Params.get())
				else address
			dao.deleteDataByAddressAndChainType(formattedAddress, chainType.id)
		}
	}
}

@Dao
interface BTCSeriesTransactionDao {

	@Query("SELECT * FROM bitcoinTransactionList")
	fun getAll(): List<BTCSeriesTransactionTable>

	@Query("UPDATE bitcoinTransactionList SET blockNumber = :blockNumber, isPending = :isPending WHERE hash LIKE :hash AND recordAddress LIKE :recordAddress")
	fun updateBlockNumber(blockNumber: Int, hash: String, recordAddress: String, isPending: Boolean)

	@Query("SELECT * FROM bitcoinTransactionList WHERE recordAddress LIKE :address AND chainType LIKE :chainType ORDER BY timeStamp DESC")
	fun getTransactions(address: String, chainType: Int): List<BTCSeriesTransactionTable>

	@Query("DELETE FROM bitcoinTransactionList WHERE recordAddress LIKE :address AND chainType LIKE :chainType")
	fun deleteDataByAddressAndChainType(address: String, chainType: Int)

	@Query("SELECT * FROM bitcoinTransactionList WHERE hash = :hash AND isReceive = :isReceive AND isFee = :isFee")
	fun getDataByHash(hash: String, isReceive: Boolean, isFee: Boolean): BTCSeriesTransactionTable?

	@Query("SELECT * FROM bitcoinTransactionList WHERE dataIndex = (SELECT MAX(dataIndex) FROM bitcoinTransactionList WHERE recordAddress LIKE :address AND chainType = :chainType)")
	fun getMaxDataIndex(address: String, chainType: Int): BTCSeriesTransactionTable?

	@Query("SELECT * FROM bitcoinTransactionList WHERE recordAddress LIKE :address AND chainType LIKE :chainType  AND dataIndex BETWEEN :start AND :end ORDER BY timeStamp DESC")
	fun getDataByRange(address: String, chainType: Int, start: Int, end: Int): List<BTCSeriesTransactionTable>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(table: BTCSeriesTransactionTable)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertAll(tables: List<BTCSeriesTransactionTable>)

	@Update
	fun update(table: BTCSeriesTransactionTable)

	@Delete
	fun delete(table: BTCSeriesTransactionTable)

	@Delete
	fun deleteAll(table: List<BTCSeriesTransactionTable>)
}