package io.goldstone.blockchain.kernel.network.Bitcoin

import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.value.WebUrl

/**
 * @date 2018/7/19 1:50 AM
 * @author KaySaith
 */
object BitcoinUrl {
	
	var currentUrl = if (Config.isTestEnvironment()) WebUrl.btcTest else WebUrl.btcMain
	val getBalance: (header: String, address: String) -> String = { header, address ->
		"$header/balance?active=$address"
	}
}