package io.goldstone.blinnnk.kernel.network.common

import android.net.Uri
import io.goldstone.blinnnk.common.value.WebUrl

/**
 * @date 31/03/2018 8:09 PM
 * @author KaySaith
 */
object APIPath {

	/** GoldStone Basic Api Address */
	var currentUrl = WebUrl.normalServer

	fun updateServerUrl(newUrl: String) {
		currentUrl = newUrl
	}

	const val serverStatus = "https://gs.blinnnk.com/index/serverStatus"
	val getCurrencyRate: (header: String) -> String = { "$it/index/exchangeRate?currency=" }
	val registerDevice: (header: String) -> String = { "$it/account/registerDevice" }
	val updateAddresses: (header: String) -> String = { "$it/account/commitAddress" }
	val getNotification: (header: String) -> String = { "$it/account/unreadMessageList" }
	val terms: (header: String) -> String = { "$it/index/agreement?md5=" }
	val marketSearch: (header: String, pair: String, marketIds: String) -> String = { header, pair, marketIds ->
		"$header/account/searchPair?pair=$pair" +
			if (marketIds.isEmpty()) "" else "&market_ids=$marketIds"
	}
	val searchPairByExactKey: (header: String) -> String = {
		"$it/account/searchPairByExactKey"
	}
	val marketList: (header: String) -> String = { header ->
		"$header/index/marketList?md5="
	}
	val getConfigList: (header: String) -> String = { "$it/index/getConfigList" }
	val getCurrencyLineChartData: (header: String) -> String = { "$it/account/lineDataByDay" }
	val getPriceByAddress: (header: String) -> String = { "$it/index/priceByAddress" }
	val getCoinInfo: (header: String, symbol: String, contract: String) -> String = { header, symbol, contract ->
		"$header/market/coinInfo?symbol=$symbol&contract=$contract"
	}
	val getUnreadCount: (header: String) -> String = { "$it/account/checkUnreadMessage" }
	val getNewVersion: (header: String) -> String = { "$it/index/getNewVersion" }
	val getShareContent: (header: String) -> String = { "$it/index/getShareContent" }
	val unregisterDevice: (header: String) -> String = { "$it/account/unregisterDevice" }
	val getIconURL: (header: String) -> String = { "$it/index/getTokenBySymbolAndAddress" }
	val getChainNodes: (header: String) -> String = { "$it/market/getChainNodes" }
	val getMD5Info: (header: String, coinRankSize: Int) -> String = {
			header, coinRankSize ->"$header/index/md5Info?coin_rank_size=$coinRankSize" }
	val getEOSTokenList: (header: String, chainID: String, account: String) -> String = { header, chainID, account ->
		"$header/eos/tokenHistory?chainid=$chainID&account=$account"
	}
	val getEOSTokenCountInfo: (
		header: String,
		chainID: String,
		account: String,
		code: String,
		symbol: String
	) -> String = { header, chainID, account, codeName, symbol ->
		"$header/eos/transferStatInfo?chainid=$chainID&account=$account&code=$codeName&symbol=$symbol"
	}
	val getEOSTransactions: (
		header: String,
		chainID: String,
		account: String,
		pageSize: Int,
		startID: Long,
		endID: Long,
		codeName: String,
		symbol: String
	) -> String = { header, chainID, account, pageSize, startID, endID, codeName, symbol ->
		"$header/eos/actionHistory?chainid=$chainID&account=$account&size=$pageSize&start=$startID&end=$endID&code=$codeName&symbol=$symbol"
	}
	val defaultTokenList: (
		header: String
	) -> String = { header ->
		"$header/index/defaultCoinList?md5="
	}
	val getTokenInfo: (
		header: String,
		condition: String,
		chainIDs: String
	) -> String = { header, condition, chainIDs ->
		"$header/index/searchToken?symbolOrContract=$condition&chainids=$chainIDs"
	}
	val getTestNetETCTransactions: (
		header: String,
		chainID: String,
		address: String,
		startBlock: Int
	) -> String = { header, chainID, address, startBlock ->
		"$header/tx/pageList?chainid=$chainID&address=$address&start_block=$startBlock"
	}
	
	val getETCTransactions: (
		page: Int,
		offset: Int,
		address: String
	) -> String = { page, offset, address ->
		"https://blockscout.com/etc/mainnet/api?module=account&action=txlist&address=$address&page=$page&offset=$offset&sort=desc"
	}

	val getQuotationCurrencyCandleChart: (
		header: String,
		pair: String,
		period: String,
		size: Int
	) -> String = { header, pair, period, size ->
		"$header/chart/lineData?pair=$pair&period=$period&size=$size"
	}
	val getQuotationCurrencyInfo: (header: String, pair: String) -> String = { header, pair ->
		"$header/market/coinDetail?pair=$pair"
	}

	val getRecommendDAPPs: (header: String, page: Int, pageSize: Int) -> String = { header, pageIndex, pageSize ->
		"$header/dapp/getRecommendDapp?page=$pageIndex&size=$pageSize"
	}

	val getNewDAPPs: (header: String, page: Int, pageSize: Int) -> String = { header, pageIndex, pageSize ->
		"$header/dapp/getDapps?page=$pageIndex&size=$pageSize"
	}

	// 从服务器动态更新注入 `Scatter` 的 `JS Code`
	val getDAPPJSCode: (header: String) -> String = { header ->
		"$header/index/getJSCode"
	}

	val searchDAPP: (header: String, condition: String) -> String = { header, condition ->
		"$header/dapp/searchDapp?dapp=${Uri.encode(condition)}"
	}
	
	val coinGlobalData: (header: String) -> String = {
		"$it/market/globalData"
	}
	
	val coinRank:(header: String, rank: Int, size: Int) -> String = { header, rank, size ->
		"$header/market/coinRank?rank=$rank&size=$size"
	}
}
