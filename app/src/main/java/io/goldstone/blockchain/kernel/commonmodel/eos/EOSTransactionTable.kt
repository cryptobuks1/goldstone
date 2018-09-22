package io.goldstone.blockchain.kernel.commonmodel.eos

import android.arch.persistence.room.*
import android.support.annotation.UiThread
import com.blinnnk.extension.*
import io.goldstone.blockchain.common.utils.load
import io.goldstone.blockchain.common.utils.then
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.crypto.eos.EOSUtils
import io.goldstone.blockchain.crypto.eos.base.EOSResponse
import io.goldstone.blockchain.crypto.eos.transaction.EOSTransactionInfo
import io.goldstone.blockchain.crypto.multichain.ChainID
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.io.Serializable
import java.math.BigInteger

@Entity(tableName = "eosTransactions")
data class EOSTransactionTable(
	@PrimaryKey(autoGenerate = true)
	var id: Int,
	var dataIndex: Int,
	var txID: String,
	var cupUsage: BigInteger,
	var netUsage: BigInteger,
	var transactionData: EOSTransactionData,
	var blockNumber: Int,
	var time: Long,
	var status: Boolean,
	var recordAccountName: String,
	var recordPublicKey: String, // 删除钱包的时候用这个标记来删除, 一个公钥下的全部
	var chainID: String,
	var isPending: Boolean
) : Serializable {
	constructor(
		info: EOSTransactionInfo,
		response: EOSResponse,
		dataIndex: Int
	) : this(
		0,
		dataIndex,
		response.transactionID,
		response.cupUsageByte,
		response.netUsageByte,
		EOSTransactionData(info),
		0, // 需要在数块的时候获取
		System.currentTimeMillis(),
		response.executedStatus,
		// 这个构造方法用于插入 `Pending Data` 是本地发起才用到, 所以 `RecordAccount` 就是 `FromAccount `
		info.fromAccount.accountName,
		Config.getCurrentEOSAddress(),
		Config.getEOSCurrentChain().id,
		true
	)

	constructor(data: JSONObject, recordAccountName: String) : this(
		0,
		dataIndex = data.safeGet("account_action_seq").toIntOrZero(),
		txID = data.getTargetChild("action_trace", "trx_id"),
		cupUsage = BigInteger.ZERO,
		netUsage = BigInteger.ZERO,
		transactionData = EOSTransactionData(data.getTargetObject("action_trace", "act", "data")),
		blockNumber = data.safeGet("block_num").toIntOrZero(),
		time = EOSUtils.getUTCTimeStamp(data.safeGet("block_time")),
		status = true,
		recordAccountName = recordAccountName,
		recordPublicKey = Config.getCurrentEOSAddress(),
		chainID = Config.getEOSCurrentChain().id,
		isPending = false
	)

	companion object {

		fun updateBandWidthAndStatusBy(
			txID: String,
			cpuUsage: BigInteger,
			netUsage: BigInteger,
			status: String
		) {
			doAsync {
				GoldStoneDataBase.database.eosTransactionDao()
					.updateBandWidthAndStatusByTxID(txID, cpuUsage, netUsage, status)
			}
		}

		fun preventDuplicateInsert(name: String, table: EOSTransactionTable) {
			doAsync {
				GoldStoneDataBase.database.eosTransactionDao().apply {
					if (getDataByTxIDAndRecordName(table.txID, name).isNull()) insert(table)
				}
			}
		}

		fun getTransactionByAccountName(
			name: String,
			chainID: ChainID,
			@UiThread hold: (List<EOSTransactionTable>) -> Unit
		) {
			load {
				GoldStoneDataBase.database.eosTransactionDao().getDataByRecordAccount(name, chainID.id)
			} then (hold)
		}

		fun deleteByAddress(address: String) {
			doAsync {
				GoldStoneDataBase.database.eosTransactionDao().apply {
					deleteAll(getDataByRecordAddress(address))
				}
			}
		}
	}
}

@Dao
interface EOSTransactionDao {

	@Query("UPDATE eosTransactions SET cupUsage = :cpuUsage, netUsage = :netUsage, status = :status WHERE txID LIKE :txID")
	fun updateBandWidthAndStatusByTxID(txID: String, cpuUsage: BigInteger, netUsage: BigInteger, status: String)

	@Query("UPDATE eosTransactions SET blockNumber = :blockNumber WHERE txID LIKE :txID")
	fun updateBlockNumberByTxID(txID: String, blockNumber: Int)

	@Query("UPDATE eosTransactions SET isPending = :pendingStatus WHERE txID LIKE :txID")
	fun updatePendingStatusByTxID(txID: String, pendingStatus: Boolean = false)

	@Query("SELECT * FROM eosTransactions WHERE recordAccountName LIKE :recordAccountName")
	fun getDataByRecordAccount(recordAccountName: String): List<EOSTransactionTable>

	@Query("SELECT * FROM eosTransactions WHERE recordAccountName LIKE :recordAddress")
	fun getDataByRecordAddress(recordAddress: String): List<EOSTransactionTable>

	@Query("SELECT * FROM eosTransactions WHERE recordAccountName LIKE :recordAccountName AND chainID LIKE :chainID")
	fun getDataByRecordAccount(recordAccountName: String, chainID: String): List<EOSTransactionTable>

	@Query("SELECT * FROM eosTransactions WHERE recordAccountName LIKE :recordAccountName AND txID LIKE :txID")
	fun getDataByTxIDAndRecordName(txID: String, recordAccountName: String): EOSTransactionTable?

	@Insert
	fun insert(transaction: EOSTransactionTable)

	@Insert
	fun insertAll(transactions: List<EOSTransactionTable>)

	@Update
	fun update(transaction: EOSTransactionTable)

	@Delete
	fun delete(transaction: EOSTransactionTable)

	@Delete
	fun deleteAll(transactions: List<EOSTransactionTable>)
}