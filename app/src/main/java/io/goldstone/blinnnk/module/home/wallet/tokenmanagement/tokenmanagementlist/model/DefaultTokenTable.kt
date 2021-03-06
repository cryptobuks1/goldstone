package io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenmanagementlist.model

import android.arch.persistence.room.*
import android.support.annotation.WorkerThread
import com.blinnnk.extension.*
import com.blinnnk.util.TinyNumberUtils
import com.blinnnk.util.load
import com.blinnnk.util.then
import com.google.gson.annotations.SerializedName
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.thread.launchUI
import io.goldstone.blinnnk.common.value.Current
import io.goldstone.blinnnk.crypto.multichain.ChainID
import io.goldstone.blinnnk.crypto.multichain.CryptoValue
import io.goldstone.blinnnk.crypto.multichain.TokenContract
import io.goldstone.blinnnk.crypto.multichain.getCurrentChainID
import io.goldstone.blinnnk.kernel.database.GoldStoneDataBase
import io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenSearch.model.TokenSearchModel
import io.goldstone.blinnnk.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.model.ERC20TransactionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * @date 25/03/2018 5:11 PM
 * @author KaySaith
 */
@Entity(tableName = "defaultTokens", primaryKeys = ["contract", "symbol", "chainID"])
data class DefaultTokenTable(
	@SerializedName("_id")
	var serverTokenID: String,
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
	var decimals: Int,
	var totalSupply: String? = null,
	// 个人通过 `Contract` 搜索到的, 和 `Server` 与 `Local` Json 数据都不同的部分.
	var isDefault: Boolean = true,
	@SerializedName("weight")
	var weight: Int,
	var chainID: String,
	var socialMedia: List<SocialMediaModel> = listOf(),
	var description: String = "",
	var exchange: String = "",
	var whitePaper: String = "",
	var startDate: String = "",
	var website: String = "",
	var rank: String = "",
	var marketCap: String = "",
	@Ignore
	var isUsed: Boolean = false
) {

	/** 默认的 `constructor` */
	constructor() : this(
		"",
		"",
		"",
		"",
		0,
		0.0,
		"",
		0,
		"",
		true,
		0,
		""
	)

	constructor(
		data: TokenSearchModel,
		isDefault: Boolean = false
	) : this(
		"",
		data.contract,
		data.iconUrl.orEmpty(),
		data.symbol,
		0,
		data.price.toDoubleOrNull().orZero(),
		data.name,
		data.decimal,
		"",
		isDefault,
		data.weight,
		data.chainID
	)

	constructor(localData: JSONObject) : this(
		"",
		localData.safeGet("address"),
		localData.safeGet("url"),
		localData.safeGet("symbol"),
		localData.safeGet("force_show").toInt(),
		localData.safeGet("price").toDouble(),
		localData.safeGet("name"),
		localData.safeGet("decimals").toIntOrZero(),
		localData.safeGet("total_supply"),
		TinyNumberUtils.isTrue(localData.safeGet("is_default")),
		if (localData.safeGet("weight").isEmpty()) 0
		else localData.safeGet("weight").toInt(),
		localData.safeGet("chain_id"),
		SocialMediaModel.generateList(localData.safeGet("social_media")),
		localData.safeGet("description"),
		localData.safeGet("website"),
		localData.safeGet("exchange"),
		localData.safeGet("white_paper"),
		localData.safeGet("start_date")
	)

	constructor(data: CoinInfoModel) : this(
		"",
		data.contract.contract.orEmpty(),
		"",
		data.symbol,
		0,
		888.0,
		"",
		0,
		data.supply,
		false,
		0,
		data.chainID,
		SocialMediaModel.generateList(data.socialMedia),
		"${SharedWallet.getCurrentLanguageCode()}${data.description}",
		data.exchange,
		data.whitePaper,
		data.startDate,
		data.website,
		data.rank,
		data.marketCap
	)

	@Ignore
	constructor(
		contract: String,
		symbol: String,
		decimals: Int,
		chainID: ChainID,
		iconUrl: String,
		tokenName: String = "",
		isDefault: Boolean = true
	) : this(
		"",
		contract,
		iconUrl,
		symbol,
		0,
		0.0,
		tokenName,
		decimals,
		"",
		isDefault,
		0,
		chainID.id
	)

	// 服务插入 `EOS` 主网 `Token` 的构造函数
	constructor(
		contract: TokenContract,
		iconUrl: String,
		chainID: ChainID
	) : this(
		"",
		contract.contract.orEmpty(),
		iconUrl,
		contract.symbol,
		0,
		0.0,
		"",
		contract.decimal.orElse(CryptoValue.eosDecimal),
		"",
		true,
		0,
		chainID.id
	)

	// 服务插入 `EOS` 主网 `Token` 的构造函数
	constructor(
		erc20: ERC20TransactionModel,
		chainID: ChainID
	) : this(
		"",
		erc20.contract,
		"",
		erc20.tokenSymbol,
		0,
		0.0,
		erc20.tokenName,
		erc20.tokenDecimal.toIntOrZero(),
		"",
		true,
		0,
		chainID.id
	)

	infix fun insertThen(callback: () -> Unit) {
		GlobalScope.launch(Dispatchers.Default) {
			DefaultTokenTable.dao.insert(this@DefaultTokenTable)
			launchUI(callback)
		}
	}

	fun updateDefaultStatus(
		contract: TokenContract,
		isDefault: Boolean,
		name: String,
		iconUrl: String,
		callback: () -> Unit
	) {
		load {
			GoldStoneDataBase.database.defaultTokenDao().update(apply {
				this.isDefault = isDefault
				this.name = name
				this.contract = contract.contract.orEmpty()
				this.chainID = contract.getCurrentChainID().id
				this.iconUrl = iconUrl
			})
		} then { callback() }
	}

	companion object {

		@JvmField
		val dao = GoldStoneDataBase.database.defaultTokenDao()

		fun getDefaultTokens(hold: (List<DefaultTokenTable>) -> Unit) {
			load {
				GoldStoneDataBase.database.defaultTokenDao().getDefaultTokens(Current.supportChainIDs().map { it.id })
			} then (hold)
		}

		fun getToken(contract: String, symbol: String, chainID: ChainID, hold: (DefaultTokenTable?) -> Unit) {
			load {
				GoldStoneDataBase.database.defaultTokenDao().getToken(contract, symbol, chainID.id)
			} then (hold)
		}

		fun getCurrentChainToken(contract: TokenContract, hold: (DefaultTokenTable?) -> Unit) {
			load {
				GoldStoneDataBase.database.defaultTokenDao().getToken(
					contract.contract.orEmpty(),
					contract.symbol,
					contract.getCurrentChainID().id
				)
			} then (hold)
		}

		@WorkerThread
		fun updateOrInsertCoinInfo(data: CoinInfoModel, callback: () -> Unit) {
			DefaultTokenTable.dao.getToken(data.contract.contract.orEmpty(), data.symbol, data.chainID).let { targetTokens ->
				if (targetTokens.isNull()) {
					DefaultTokenTable.dao.insert(DefaultTokenTable(data))
					callback()
				} else {
					// 插入行情的 `TokenInformation` 只需要插入主链数据即可
					targetTokens.apply {
						exchange = data.exchange
						website = data.website
						marketCap = data.marketCap
						whitePaper = data.whitePaper
						socialMedia = SocialMediaModel.generateList(data.socialMedia)
						rank = data.rank
						totalSupply = data.supply
						startDate = data.startDate
						description = "${SharedWallet.getCurrentLanguageCode()}${data.description}"
					}.let {
						DefaultTokenTable.dao.update(it)
						callback()
					}
				}
			}
		}
	}
}

@Dao
interface DefaultTokenDao {

	@Query("SELECT * FROM defaultTokens")
	fun getAllTokens(): List<DefaultTokenTable>

	@Query("SELECT count(*) FROM defaultTokens")
	fun rowCount(): Int

	@Query("SELECT * FROM defaultTokens WHERE forceShow == 1")
	fun getForceShow(): List<DefaultTokenTable>

	@Query("UPDATE defaultTokens SET price = :newPrice WHERE contract LIKE :contract AND symbol LIKE :symbol AND chainID LIKE :chainID")
	fun updateTokenPrice(newPrice: Double, contract: String, symbol: String, chainID: String)

	// ERC 20 Only
	@Query("UPDATE defaultTokens SET name = :name, decimals = :decimals, symbol = :symbol WHERE contract LIKE :contract AND chainID LIKE :chainID")
	fun updateTokenInfo(name: String, decimals: Int, symbol: String, contract: String, chainID: String)

	@Query("SELECT * FROM defaultTokens WHERE chainID IN (:currentChainIDs)")
	fun getCurrentChainTokens(currentChainIDs: List<String> = Current.chainIDs()): List<DefaultTokenTable>

	@Query("SELECT * FROM defaultTokens WHERE isDefault LIKE 1 AND chainID IN (:currentChainIDs)")
	fun getDefaultTokens(currentChainIDs: List<String> = Current.chainIDs()): List<DefaultTokenTable>

	@Query("SELECT * FROM defaultTokens WHERE contract LIKE :contract AND symbol LIKE :symbol  AND chainID LIKE :chainID")
	fun getToken(contract: String, symbol: String, chainID: String): DefaultTokenTable?

	@Query("SELECT price FROM defaultTokens WHERE contract LIKE :contract AND symbol LIKE :symbol  AND chainID LIKE :chainID")
	fun getTokenPrice(contract: String, symbol: String, chainID: String): Double?

	@Query("SELECT * FROM defaultTokens WHERE contract LIKE :contract AND chainID LIKE :chainID")
	fun getERC20Token(contract: String, chainID: String): DefaultTokenTable?

	@Query("SELECT * FROM defaultTokens WHERE contract LIKE :contract AND symbol LIKE :symbol")
	fun getTokenFromAllChains(contract: String, symbol: String): List<DefaultTokenTable>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(token: DefaultTokenTable)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertAll(tokens: List<DefaultTokenTable>)

	@Update
	fun update(token: DefaultTokenTable)

	@Delete
	fun delete(token: DefaultTokenTable)
}