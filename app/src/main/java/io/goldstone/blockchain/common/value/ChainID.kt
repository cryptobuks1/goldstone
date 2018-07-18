package io.goldstone.blockchain.common.value

import io.goldstone.blockchain.crypto.CryptoSymbol

/**
 * @date 2018/5/25 8:14 PM
 * @author KaySaith
 */
enum class ChainID(val id: String) {
	
	Main("1"),
	Ropsten("3"),
	Rinkeby("4"),
	Kovan("42"),
	ETCMain("61"),
	ETCTest("62"),
	BTCMain("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"),
	BTCTest("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943"),
	LTCMain("12a765e31ffd4059bada1e25190f6e98c99d9714d334efa41a195a7e7e04bfe2");
	
	companion object {
		
		fun getTestChains(): ArrayList<String> {
			return arrayListOf(
				ChainID.ETCTest.id,
				ChainID.BTCTest.id,
				ChainID.BTCTest.id,
				ChainID.Ropsten.id,
				ChainID.Kovan.id,
				ChainID.Rinkeby.id
			)
		}
		
		fun getAllChainID(): ArrayList<String> {
			return arrayListOf(
				ChainID.Main.id,
				ChainID.Ropsten.id,
				ChainID.Kovan.id,
				ChainID.Rinkeby.id,
				ChainID.ETCTest.id,
				ChainID.ETCMain.id
			)
		}
		
		fun getChainNameByID(chainID: String): String {
			return when (chainID) {
			// Third Pardy Nodes
				Kovan.id -> ChainText.infuraKovan
				Ropsten.id -> ChainText.infuraRopsten
				Main.id -> ChainText.infuraMain
				Rinkeby.id -> ChainText.infuraRinkeby
				ETCTest.id -> ChainText.etcMorden
				ETCMain.id -> ChainText.etcMainGasTracker
			// GoldStone Nodes
				Main.id -> ChainText.goldStoneMain
				Ropsten.id -> ChainText.ropsten
				Kovan.id -> ChainText.kovan
				Rinkeby.id -> ChainText.rinkeby
				ETCMain.id -> ChainText.goldStoneEtcMain
				ETCTest.id -> ChainText.goldStoneEtcMorderTest
				else -> ChainText.goldStoneMain
			}
		}
		
		fun getChainIDByName(name: String): String {
			return when (name) {
			// GoldStone ERC Node
				ChainText.goldStoneMain -> Main.id
				ChainText.ropsten -> Ropsten.id
				ChainText.kovan -> Kovan.id
				ChainText.rinkeby -> Rinkeby.id
			// Infura ERC Node
				ChainText.infuraMain -> Main.id
				ChainText.infuraRopsten -> Ropsten.id
				ChainText.infuraKovan -> Kovan.id
				ChainText.infuraRinkeby -> Rinkeby.id
			// ETC Node
				ChainText.etcMorden -> ETCTest.id
				ChainText.goldStoneEtcMain -> ETCMain.id
				ChainText.goldStoneEtcMorderTest -> ETCTest.id
				ChainText.etcMainGasTracker -> ETCMain.id
			// BTC Node
				ChainText.btcMain -> BTCMain.id
				ChainText.btcTest -> BTCTest.id
				else -> Main.id
			}
		}
		
		fun getChainIDBySymbol(symbol: String?): String {
			return when (symbol) {
				CryptoSymbol.etc -> Config.getETCCurrentChain()
				CryptoSymbol.btc -> Config.getBTCCurrentChain()
				else -> Config.getCurrentChain()
			}
		}
	}
}