package io.goldstone.blockchain.common.sharedpreference

import com.blinnnk.extension.toJSONObjectList
import com.blinnnk.util.getStringFromSharedPreferences
import com.blinnnk.util.saveDataToSharedPreferences
import io.goldstone.blockchain.GoldStoneApp
import io.goldstone.blockchain.common.value.SharesPreference
import io.goldstone.blockchain.crypto.multichain.node.ChainNodeTable
import io.goldstone.blockchain.crypto.multichain.node.ChainURL
import org.json.JSONArray
import org.json.JSONObject


/**
 * @author KaySaith
 * @date  2018/09/27
 */
object SharedChain {
	/** Chain Config */
	fun getCurrentETH(): ChainURL {
		val chainObject =
			JSONObject(GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.ethCurrentChain))
		return ChainURL(chainObject)
	}

	fun updateCurrentETH(chainInfo: ChainURL) =
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.ethCurrentChain, chainInfo.generateObject())

	/** LTC ChainID And Chain Name in Shared Preference*/
	fun getLTCCurrent(): ChainURL {
		val chainObject =
			JSONObject(GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.ltcCurrentChain))
		return ChainURL(chainObject)
	}

	fun updateLTCCurrent(chainInfo: ChainURL) =
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.ltcCurrentChain, chainInfo.generateObject())

	/** BCH ChainID And Chain Name in Shared Preference */
	fun getBCHCurrent(): ChainURL {
		val chainObject =
			JSONObject(GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.bchCurrentChain))
		return ChainURL(chainObject)
	}

	fun updateBCHCurrent(chainInfo: ChainURL) =
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.bchCurrentChain, chainInfo.generateObject())

	/** EOS ChainID And ChainName In Shared Preference*/
	fun getEOSCurrent(): ChainURL {
		val chainObject =
			JSONObject(GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.eosCurrentChain))
		return ChainURL(chainObject)
	}

	fun updateEOSCurrent(chainInfo: ChainURL) =
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.eosCurrentChain, chainInfo.generateObject())

	fun getEOSMainnet(): ChainURL {
		val chainObject =
			JSONObject(GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.eosMainnet))
		return ChainURL(chainObject)
	}

	fun updateEOSMainnet(chainInfo: ChainURL) =
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.eosMainnet, chainInfo.generateObject())

	fun getEOSTestnet(): ChainURL {
		val chainObject =
			JSONObject(GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.eosTestnet))
		return ChainURL(chainObject)
	}

	fun updateEOSTestnet(chainInfo: ChainURL) =
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.eosTestnet, chainInfo.generateObject())


	/** ETC ChainID And Chain Name in Shared Preference*/
	fun getETCCurrent(): ChainURL {
		val chainObject =
			JSONObject(GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.etcCurrentChain))
		return ChainURL(chainObject)
	}

	fun updateETCCurrent(chainInfo: ChainURL) =
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.etcCurrentChain, chainInfo.generateObject())

	fun getBTCCurrent(): ChainURL {
		val chainObject =
			JSONObject(GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.btcCurrentChain))
		return ChainURL(chainObject)
	}

	fun updateBTCCurrent(chainInfo: ChainURL) =
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.btcCurrentChain, chainInfo.generateObject())

	fun getAllUsedTestnetChains(): List<ChainURL> {
		val allTestnetChains =
			GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.allTestnetChains)
		val jsonArray = JSONArray(allTestnetChains).toJSONObjectList()
		return jsonArray.map { ChainURL(it) }
	}

	fun updateAllUsedTestnetChains(chainInfo: List<ChainNodeTable>) {
		val jsonArray = JSONArray()
		chainInfo.forEach {
			jsonArray.put(ChainURL(it).generateObject())
		}
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.allTestnetChains, jsonArray.toString())
	}

	fun getAllUsedMainnetChains(): List<ChainURL> {
		val allTestnetChains =
			GoldStoneApp.appContext.getStringFromSharedPreferences(SharesPreference.allMainnetChains)
		val jsonArray = JSONArray(allTestnetChains).toJSONObjectList()
		return jsonArray.map { ChainURL(it) }
	}

	fun updateAllUsedMainnetChains(chainInfo: List<ChainNodeTable>) {
		val jsonArray = JSONArray()
		chainInfo.forEach {
			jsonArray.put(ChainURL(it).generateObject())
		}
		GoldStoneApp.appContext.saveDataToSharedPreferences(SharesPreference.allMainnetChains, jsonArray.toString())
	}

}