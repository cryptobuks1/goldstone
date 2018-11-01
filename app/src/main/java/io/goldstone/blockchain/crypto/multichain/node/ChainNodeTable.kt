package io.goldstone.blockchain.crypto.multichain.node

import android.arch.persistence.room.*
import com.blinnnk.extension.safeGet
import com.blinnnk.extension.toList
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable


/**
 * @author KaySaith
 * @date  2018/10/28
 */
@Entity(tableName = "chainNode")
class ChainNodeTable(
	@SerializedName("node_name")
	val name: String,
	@SerializedName("url")
	@PrimaryKey
	val url: String,
	@SerializedName("encrypt_status")
	val isEncrypt: Int,
	@SerializedName("chain_type")
	val chainType: Int,
	@SerializedName("chain_id")
	val chainID: String,
	@SerializedName("weight")
	val weight: Int,
	@SerializedName("net_type")
	val netType: Int, // 0 Mainnet 1 Testnet
	@SerializedName("key_list")
	val keyList: List<String>,
	@SerializedName("is_default")
	var isUsed: Int
) : Serializable {
	constructor(data: JSONObject) : this(
		data.safeGet("node_name"),
		data.safeGet("url"),
		data.safeGet("encrypt_status").toInt(),
		data.safeGet("chain_type").toInt(),
		data.safeGet("chain_id"),
		data.safeGet("weight").toInt(),
		data.safeGet("net_type").toInt(),
		JSONArray(data.safeGet("key_list")).toList(),
		data.safeGet("is_default").toInt()
	)
}

@Dao
interface ChainNodeDao {

	@Query("SELECT * FROM chainNode")
	fun getAll(): List<ChainNodeTable>

	@Query("SELECT * FROM chainNode WHERE netType LIKE 0 ORDER BY chainType")
	fun getMainnet(): List<ChainNodeTable>

	@Query("SELECT * FROM chainNode WHERE netType LIKE 1 ORDER BY chainType")
	fun getTestnet(): List<ChainNodeTable>

	@Query("SELECT * FROM chainNode WHERE chainType LIKE 194")
	fun getEOSNodes(): List<ChainNodeTable>

	@Query("SELECT * FROM chainNode WHERE chainType LIKE 194 AND isUsed LIKE :isUsed")
	fun getCurrentEOSNode(isUsed: Boolean = true): ChainNodeTable

	@Query("SELECT * FROM chainNode WHERE chainType LIKE 61")
	fun getETCNodes(): List<ChainNodeTable>

	@Query("SELECT * FROM chainNode WHERE chainType LIKE 60")
	fun getETHNodes(): List<ChainNodeTable>

	@Query("SELECT * FROM chainNode WHERE netType LIKE 0 AND isUsed Like :isUsed ORDER BY chainType")
	fun getUsedMainnet(isUsed: Boolean = true): List<ChainNodeTable>

	@Query("SELECT * FROM chainNode WHERE netType LIKE 1 AND isUsed Like :isUsed ORDER BY chainType")
	fun getUsedTestnet(isUsed: Boolean = true): List<ChainNodeTable>

	@Query("UPDATE chainNode SET isUsed = :isUsed WHERE url = :url")
	fun updateIsUsedByURL(url: String, isUsed: Boolean)

	@Query("UPDATE chainNode SET isUsed = :isUsed WHERE netType = :netType")
	fun clearIsUsedStatus(netType: Int, isUsed: Boolean = false)

	@Insert
	fun insert(chainTable: ChainNodeTable)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertAll(chainNodes: List<ChainNodeTable>)

	@Update
	fun update(chainTable: ChainNodeTable)

	@Delete
	fun delete(chainTable: ChainNodeTable)
}