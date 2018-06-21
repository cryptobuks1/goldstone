package io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.model

import android.arch.persistence.room.*
import com.blinnnk.extension.orZero
import com.blinnnk.extension.safeGet
import com.blinnnk.extension.toArrayList
import com.blinnnk.util.coroutinesTask
import com.google.gson.annotations.SerializedName
import io.goldstone.blockchain.common.utils.TinyNumberUtils
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import io.goldstone.blockchain.kernel.network.GoldStoneAPI
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenSearch.model.TokenSearchModel
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.json.JSONObject

/**
 * @date 25/03/2018 5:11 PM
 * @author KaySaith
 */
@Entity(tableName = "defaultTokens")
data class DefaultTokenTable(
	@PrimaryKey(autoGenerate = true)
	var id: Int,
	@SerializedName("address")
	var contract: String,
	@SerializedName("url")
	var iconUrl: String,
	@SerializedName("symbol")
	var symbol: String,
	@SerializedName("force_show")
	var forceShow: Int,
	@SerializedName("price")
	var price: Double,
	@SerializedName("name")
	var name: String,
	@SerializedName("decimals")
	var decimals: Double,
	var totalSupply: String? = null,
	// 个人通过 `Contract` 搜索到的, 和 `Server` 与 `Local` Json 数据都不同的部分.
	var isDefault: Boolean = true,
	@Ignore
	var isUsed: Boolean = false,
	@SerializedName("weight")
	var weight: Int,
	@SerializedName("chain_id")
	var chain_id: String
) {
	
	/** 默认的 `constructor` */
	@Ignore
	constructor() : this(
		0,
		"",
		"",
		"",
		0,
		0.0,
		"",
		0.0,
		"",
		true,
		false,
		0,
		Config.getCurrentChain()
	)
	
	constructor(
		data: TokenSearchModel,
		isUsed: Boolean = false
	) : this(
		0,
		data.contract,
		data.iconUrl,
		data.symbol,
		0,
		data.price.toDoubleOrNull().orZero(),
		data.name,
		data.decimal.toDouble(),
		"",
		isUsed,
		isUsed,
		data.weight,
		Config.getCurrentChain()
	)
	
	constructor(
		localData: JSONObject,
		isUsed: Boolean = false
	) : this(
		0,
		localData.safeGet("address"),
		localData.safeGet("url"),
		localData.safeGet("symbol"),
		localData.safeGet("force_show").toInt(),
		localData.safeGet("price").toDouble(),
		localData.safeGet("name"),
		localData.safeGet("decimals").toDouble(),
		localData.safeGet("total_supply"),
		TinyNumberUtils.isTrue(localData.safeGet("is_default")),
		isUsed,
		if (localData.safeGet("weight").isEmpty()) 0
		else localData.safeGet("weight").toInt(),
		localData.safeGet("chain_id")
	)
	
	constructor(
		contract: String,
		symbol: String,
		decimals: Double
	) : this(
		0,
		contract,
		"",
		symbol,
		0,
		0.0,
		"",
		decimals,
		"",
		false,
		false,
		0,
		Config.getCurrentChain()
	)
	
	companion object {
		
		fun getAllTokens(hold: (ArrayList<DefaultTokenTable>) -> Unit) {
			coroutinesTask(
				{
					GoldStoneDataBase.database.defaultTokenDao().getAllTokens()
				}) {
				hold(it.toArrayList())
			}
		}
		
		fun getCurrentChainTokens(hold: (ArrayList<DefaultTokenTable>) -> Unit) {
			coroutinesTask(
				{
					GoldStoneDataBase.database.defaultTokenDao().getCurrentChainTokens()
				}) {
				hold(it.toArrayList())
			}
		}
		
		fun getDefaultTokens(hold: (ArrayList<DefaultTokenTable>) -> Unit) {
			coroutinesTask(
				{
					GoldStoneDataBase.database.defaultTokenDao().getDefaultTokens()
				}) {
				hold(it.toArrayList())
			}
		}
		
		fun getCurrentChainTokenByContract(
			contract: String,
			ercChain: String = Config.getCurrentChain(),
			etcChain: String = Config.getETCCurrentChain(),
			hold: (DefaultTokenTable?) -> Unit
		) {
			coroutinesTask(
				{
					GoldStoneDataBase
						.database
						.defaultTokenDao()
						.getCurrentChainTokenByContract(contract, ercChain, etcChain)
				}) {
				hold(it)
			}
		}
		
		fun updateTokenPrice(
			contract: String,
			newPrice: Double,
			callback: () -> Unit = {}
		) {
			doAsync {
				GoldStoneDataBase.database.defaultTokenDao()
					.apply {
						getCurrentChainTokenByContract(contract)?.let {
							update(it.apply { price = newPrice })
							GoldStoneAPI.context.runOnUiThread { callback() }
						}
					}
			}
		}
		
		fun updateTokenName(
			contract: String,
			name: String
		) {
			doAsync {
				GoldStoneDataBase.database.defaultTokenDao()
					.apply {
						getCurrentChainTokenByContract(contract)?.let {
							update(it.apply { this.name = name })
						}
					}
			}
		}
		
		fun updateTokenDefaultStatus(
			contract: String,
			isDefault: Boolean,
			name: String,
			callback: () -> Unit
		) {
			doAsync {
				GoldStoneDataBase.database.defaultTokenDao()
					.apply {
						getCurrentChainTokenByContract(contract)?.let {
							update(it.apply {
								this.isDefault = isDefault
								this.name = name
							})
							GoldStoneAPI.context.runOnUiThread { callback() }
						}
					}
			}
		}
		
		fun insertToken(
			token: DefaultTokenTable,
			callback: () -> Unit
		) {
			doAsync {
				GoldStoneDataBase.database.defaultTokenDao().insert(token)
				GoldStoneAPI.context.runOnUiThread {
					callback()
				}
			}
		}
	}
}

@Dao
interface DefaultTokenDao {
	
	@Query("SELECT * FROM defaultTokens")
	fun getAllTokens(): List<DefaultTokenTable>
	
	@Query("SELECT * FROM defaultTokens WHERE chain_id LIKE :ercChain OR chain_id LIKE :etcChain")
	fun getCurrentChainTokens(
		ercChain: String = Config.getCurrentChain(),
		etcChain: String = Config.getETCCurrentChain()
	): List<DefaultTokenTable>
	
	@Query("SELECT * FROM defaultTokens WHERE isDefault LIKE :isDefault AND (chain_id LIKE :ercChain OR chain_id LIKE :etcChain)")
	fun getDefaultTokens(
		isDefault: Boolean = true,
		ercChain: String = Config.getCurrentChain(),
		etcChain: String = Config.getETCCurrentChain()
	): List<DefaultTokenTable>
	
	@Query("SELECT * FROM defaultTokens WHERE contract LIKE :contract  AND (chain_id LIKE :ercChain OR chain_id LIKE :etcChain)")
	fun getCurrentChainTokenByContract(
		contract: String,
		ercChain: String = Config.getCurrentChain(),
		etcChain: String = Config.getETCCurrentChain()
	): DefaultTokenTable?
	
	@Query("SELECT * FROM defaultTokens WHERE contract LIKE :contract")
	fun getTokenByContractFromAllChains(contract: String): DefaultTokenTable?
	
	@Insert
	fun insert(token: DefaultTokenTable)
	
	@Update
	fun update(token: DefaultTokenTable)
	
	@Delete
	fun delete(token: DefaultTokenTable)
}