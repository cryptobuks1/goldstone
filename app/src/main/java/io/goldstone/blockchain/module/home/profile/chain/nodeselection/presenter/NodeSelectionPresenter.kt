package io.goldstone.blockchain.module.home.profile.chain.nodeselection.presenter

import com.blinnnk.util.TinyNumberUtils
import io.goldstone.blockchain.common.base.basefragment.BasePresenter
import io.goldstone.blockchain.common.language.ChainText
import io.goldstone.blockchain.common.value.ChainID
import io.goldstone.blockchain.common.value.ChainNameID
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.crypto.ChainType
import io.goldstone.blockchain.kernel.commonmodel.AppConfigTable
import io.goldstone.blockchain.module.home.profile.chain.nodeselection.view.NodeSelectionFragment

/**
 * @date 2018/6/20 8:59 PM
 * @author KaySaith
 */
class NodeSelectionPresenter(
	override val fragment: NodeSelectionFragment
) : BasePresenter<NodeSelectionFragment>() {

	/**
	 * `ChainID` 会重复使用导致获取 `Chain` 并不能准确, 所以切换 `Chain` 的时候存储
	 */
	fun updateERC20ChainID(nodeName: String) {
		Config.updateCurrentChainName(nodeName)
		Config.updateCurrentChain(ChainID.getChainIDByName(nodeName))
		// 根据节点属性判断是否需要对 `JSON RPC` 加密或解密, `GoldStone`的节点请求全部加密了.
		Config.updateEncryptERCNodeRequest(checkIsEncryptERCNode(nodeName))
	}

	fun updateETCChainID(nodeName: String) {
		Config.updateETCCurrentChainName(nodeName)
		Config.updateETCCurrentChain(ChainID.getChainIDByName(nodeName))
		// 根据节点属性判断是否需要对 `JSON RPC` 加密或解密, `GoldStone`的节点请求全部加密了.
		Config.updateEncryptETCNodeRequest(checkIsEncryptETCNode(nodeName))
	}

	fun updateBTCChainID(nodeName: String) {
		Config.updateBTCCurrentChainName(nodeName)
		Config.updateBTCCurrentChain(ChainID.getChainIDByName(nodeName))
	}

	fun updateBCHChainID(nodeName: String) {
		Config.updateBCHCurrentChainName(nodeName)
		Config.updateBCHCurrentChain(ChainID.getChainIDByName(nodeName))
	}

	fun updateLTCChainID(nodeName: String) {
		Config.updateLTCCurrentChainName(nodeName)
		Config.updateLTCCurrentChain(ChainID.getChainIDByName(nodeName))
	}

	fun updateEOSChainID(nodeName: String) {
		Config.updateEOSCurrentChainName(nodeName)
		Config.updateEOSCurrentChain(ChainID.getChainIDByName(nodeName))
	}

	fun getCurrentChainName(isMainnet: Boolean, type: ChainType): String {
		return if (isMainnet) {
			when (type) {
				ChainType.ETH -> {
					if (Config.getCurrentChain() != ChainID.Main.id) ChainText.infuraMain
					else Config.getCurrentChainName()
				}

				ChainType.BTC -> ChainText.btcMain
				ChainType.LTC -> ChainText.ltcMain
				ChainType.BCH -> ChainText.bchMain

				else -> {
					if (Config.getETCCurrentChain() != ChainID.ETCMain.id) ChainText.etcMainGasTracker
					else Config.getETCCurrentChainName()
				}
			}
		} else {
			when (type) {
				ChainType.ETH -> {
					if (Config.getCurrentChain() == ChainID.Main.id) ChainText.infuraRopsten
					else Config.getCurrentChainName()
				}
				ChainType.BTC -> ChainText.btcTest
				ChainType.LTC -> ChainText.ltcTest
				ChainType.BCH -> ChainText.bchTest
				else -> {
					if (Config.getETCCurrentChain() == ChainID.ETCMain.id) ChainText.etcMorden
					else Config.getETCCurrentChainName()
				}
			}
		}
	}

	private fun checkIsEncryptERCNode(nodeName: String): Boolean {
		return TinyNumberUtils.allFalse(
			nodeName.contains("infura", true)
		)
	}

	private fun checkIsEncryptETCNode(nodeName: String): Boolean {
		return TinyNumberUtils.allFalse(
			nodeName.contains("gasTracker", true)
		)
	}

	companion object {
		fun setAllTestnet(callback: () -> Unit) {
			AppConfigTable.getAppConfig {
				it?.apply {
					AppConfigTable.updateChainStatus(false) {
						Config.updateIsTestEnvironment(true)
						Config.updateBTCCurrentChain(ChainID.BTCTest.id)
						Config.updateLTCCurrentChain(ChainID.LTCTest.id)
						Config.updateBCHCurrentChain(ChainID.BCHTest.id)
						Config.updateETCCurrentChain(ChainID.ETCTest.id)
						Config.updateEOSCurrentChain(ChainID.EOSTest.id)
						Config.updateCurrentChain(
							ChainID.getChainIDByName(
								ChainNameID.getChainNameByID(currentETHERC20AndETCTestChainNameID)
							)
						)
						Config.updateETCCurrentChainName(
							ChainNameID.getChainNameByID(currentETCTestChainNameID)
						)
						Config.updateEOSCurrentChainName(
							ChainNameID.getChainNameByID(currentEOSChainNameID)
						)
						Config.updateCurrentChainName(
							ChainNameID.getChainNameByID(currentETHERC20AndETCTestChainNameID)
						)
						Config.updateBTCCurrentChainName(
							ChainNameID.getChainNameByID(currentBTCTestChainNameID)
						)
						Config.updateBCHCurrentChainName(
							ChainNameID.getChainNameByID(currentBCHTestChainNameID)
						)
						Config.updateLTCCurrentChainName(
							ChainNameID.getChainNameByID(currentLTCTestChainNameID)
						)
						callback()
					}
				}
			}

		}

		fun setAllMainnet(callback: () -> Unit) {
			AppConfigTable.getAppConfig {
				it?.apply {
					AppConfigTable.updateChainStatus(true) {
						Config.updateIsTestEnvironment(false)
						Config.updateBTCCurrentChain(ChainID.BTCMain.id)
						Config.updateLTCCurrentChain(ChainID.LTCMain.id)
						Config.updateBCHCurrentChain(ChainID.BCHMain.id)
						Config.updateETCCurrentChain(ChainID.ETCMain.id)
						Config.updateEOSCurrentChain(ChainID.EOSMain.id)
						Config.updateCurrentChain(
							ChainID.getChainIDByName(
								ChainNameID.getChainNameByID(currentETHERC20AndETCChainNameID)
							)
						)
						Config.updateETCCurrentChainName(
							ChainNameID.getChainNameByID(currentETCChainNameID)
						)
						Config.updateEOSCurrentChainName(
							ChainNameID.getChainNameByID(currentEOSChainNameID)
						)
						Config.updateCurrentChainName(ChainNameID.getChainNameByID(
							currentETHERC20AndETCChainNameID)
						)
						Config.updateBTCCurrentChainName(
							ChainNameID.getChainNameByID(currentBTCChainNameID)
						)
						Config.updateBCHCurrentChainName(
							ChainNameID.getChainNameByID(currentBCHChainNameID)
						)
						Config.updateLTCCurrentChainName(
							ChainNameID.getChainNameByID(currentLTCChainNameID)
						)
						callback()
					}
				}
			}
		}
	}
}