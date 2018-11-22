package io.goldstone.blockchain.module.common.walletimport.watchonly.presenter

import android.support.annotation.WorkerThread
import android.widget.EditText
import com.blinnnk.extension.getParentFragment
import com.blinnnk.extension.isNotNull
import io.goldstone.blockchain.common.base.basefragment.BasePresenter
import io.goldstone.blockchain.common.error.AccountError
import io.goldstone.blockchain.common.error.GoldStoneError
import io.goldstone.blockchain.common.sharedpreference.SharedAddress
import io.goldstone.blockchain.common.sharedpreference.SharedChain
import io.goldstone.blockchain.common.sharedpreference.SharedValue
import io.goldstone.blockchain.common.utils.isEmptyThen
import io.goldstone.blockchain.crypto.bitcoin.BTCUtils
import io.goldstone.blockchain.crypto.bitcoincash.BCHWalletUtils
import io.goldstone.blockchain.crypto.eos.EOSWalletUtils
import io.goldstone.blockchain.crypto.eos.account.EOSAccount
import io.goldstone.blockchain.crypto.ethereum.Address
import io.goldstone.blockchain.crypto.ethereum.isValid
import io.goldstone.blockchain.crypto.litecoin.LTCWalletUtils
import io.goldstone.blockchain.crypto.multichain.AddressType
import io.goldstone.blockchain.crypto.multichain.ChainAddresses
import io.goldstone.blockchain.crypto.multichain.ChainID
import io.goldstone.blockchain.crypto.multichain.ChainType
import io.goldstone.blockchain.crypto.multichain.node.ChainNodeTable
import io.goldstone.blockchain.crypto.multichain.node.ChainURL
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import io.goldstone.blockchain.kernel.network.eos.EOSAPI
import io.goldstone.blockchain.kernel.receiver.XinGePushReceiver
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.*
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.presenter.CreateWalletPresenter
import io.goldstone.blockchain.module.common.walletimport.watchonly.view.WatchOnlyImportFragment
import io.goldstone.blockchain.module.home.profile.profileoverlay.view.ProfileOverlayFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @date 23/03/2018 2:16 AM
 * @author KaySaith
 */
class WatchOnlyImportPresenter(
	override val fragment: WatchOnlyImportFragment
) : BasePresenter<WatchOnlyImportFragment>() {

	private var currentETHSeriesAddress = ""
	private var currentBTCAddress = ""
	private var currentBTCTestAddress = ""
	private var currentETCAddress = ""
	private var currentLTCAddress = ""
	private var currentBCHAddress = ""
	// EOS 可能导入的三种情况
	private var eosMainnetAccountName = ""
	private var eosTestnetAccountName = ""
	private var currentEOSAddress = ""

	@WorkerThread
	fun importWatchOnlyWallet(
		addressType: String,
		addressInput: EditText,
		nameInput: EditText,
		callback: (GoldStoneError) -> Unit
	) = GlobalScope.launch(Dispatchers.Default) {
		// 默认去除所有的空格
		val address = addressInput.text.toString().replace(" ", "")
		var chainID: ChainID? = null
		when (addressType) {
			AddressType.ETHSeries.value -> {
				chainID = SharedChain.getETCCurrent().chainID
				if (!Address(address).isValid()) {
					callback(AccountError.InvalidAddress)
					return@launch
				}
			}
			AddressType.BTC.value -> {
				chainID = SharedChain.getBTCCurrent().chainID
				if (!BTCUtils.isValidMainnetAddress(address)) {
					callback(AccountError.InvalidAddress)
					return@launch
				}
			}
			AddressType.LTC.value -> {
				chainID = SharedChain.getLTCCurrent().chainID
				if (!LTCWalletUtils.isValidAddress(address)) {
					callback(AccountError.InvalidAddress)
					return@launch
				}
			}
			AddressType.EOS.value -> {
				chainID = ChainID.EOS
				if (!EOSWalletUtils.isValidAddress(address) && !EOSAccount(address).isValid(false)) {
					callback(AccountError.InvalidAddress)
					return@launch
				}
			}
			AddressType.EOSJungle.value -> {
				chainID = ChainID.EOSTest
				if (!EOSWalletUtils.isValidAddress(address) && !EOSAccount(address).isValid(false)) {
					callback(AccountError.InvalidAddress)
					return@launch
				}
			}

			AddressType.BCH.value -> {
				chainID = SharedChain.getBCHCurrent().chainID
				if (!BCHWalletUtils.isValidAddress(address)) {
					callback(AccountError.InvalidAddress)
					return@launch
				}
			}
			else -> if (!BTCUtils.isValidTestnetAddress(address)) {
				callback(AccountError.InvalidAddress)
				return@launch
			}
		}
		val name =
			if (nameInput.text.toString().isEmpty()) nameInput.hint.toString()
			else nameInput.text.toString()
		// 通过用导入的地址查找钱包的行为判断是否已经存在此钱包地址
		WalletTable.existAddressOrAccountName(address, chainID) { existed ->
			// 准备对应的地址
			if (!existed) setAddressByChainType(address, addressType) {
				if (it.isNone()) WalletTable(
					name,
					currentETHSeriesAddress,
					currentBTCTestAddress,
					currentBTCAddress,
					currentETCAddress,
					currentLTCAddress,
					currentBCHAddress,
					// 如果用户只导入 `AccountName` 那么会把名字存成 `Address` 以对接切换钱包的逻辑
					currentEOSAddress isEmptyThen eosMainnetAccountName isEmptyThen eosTestnetAccountName,
					EOSDefaultAllChainName(eosMainnetAccountName, eosTestnetAccountName),
					if (eosMainnetAccountName.isEmpty() && eosTestnetAccountName.isEmpty()) listOf()
					else listOf(
						EOSAccountInfo(eosMainnetAccountName, ChainID.EOS.id),
						EOSAccountInfo(eosTestnetAccountName, ChainID.EOSTest.id)
					)
				) insert { thisWallet ->
					CreateWalletPresenter.insertNewAccount(
						ChainAddresses(
							Bip44Address(currentETHSeriesAddress, ChainType.ETH.id),
							Bip44Address(currentETCAddress, ChainType.ETC.id),
							Bip44Address(currentBTCAddress, ChainType.BTC.id),
							Bip44Address(currentBTCTestAddress, ChainType.AllTest.id),
							Bip44Address(currentLTCAddress, ChainType.LTC.id),
							Bip44Address(currentBCHAddress, ChainType.BCH.id),
							Bip44Address(currentEOSAddress isEmptyThen eosMainnetAccountName isEmptyThen eosTestnetAccountName, ChainType.EOS.id)
						)
					) {
						thisWallet.registerPushByAddress()
						callback(GoldStoneError.None)
					}
				} else callback(it)
			} else callback(AccountError.ExistAddress)
		}
	}

	private fun WalletTable.registerPushByAddress() {
		listOf(
			Pair(currentBTCAddress, ChainType.BTC),
			Pair(currentLTCAddress, ChainType.LTC),
			Pair(currentBCHAddress, ChainType.BCH),
			Pair(currentBTCTestAddress, ChainType.AllTest),
			Pair(currentETCAddress, ChainType.ETC),
			Pair(currentETHSeriesAddress, ChainType.ETH),
			Pair(currentEOSAddress isEmptyThen eosMainnetAccountName isEmptyThen eosTestnetAccountName, ChainType.EOS)
		).first {
			it.first.isNotEmpty()
		}.apply {
			XinGePushReceiver.registerSingleAddress(AddressCommissionModel(first, second.id, 1, id))
		}
	}

	@WorkerThread
	private fun setAddressByChainType(
		address: String,
		addressType: String,
		callback: (GoldStoneError) -> Unit
	) {
		when (addressType) {
			AddressType.ETHSeries.value -> {
				currentETHSeriesAddress = address
				currentETCAddress = address
				callback(GoldStoneError.None)
			}
			AddressType.BTC.value -> {
				currentBTCAddress = address
				SharedValue.updateIsTestEnvironment(false)
				callback(GoldStoneError.None)
			}
			AddressType.LTC.value -> {
				currentLTCAddress = address
				SharedValue.updateIsTestEnvironment(false)
				callback(GoldStoneError.None)
			}
			AddressType.BCH.value -> {
				currentBCHAddress = address
				SharedValue.updateIsTestEnvironment(false)
				callback(GoldStoneError.None)
			}
			AddressType.EOS.value -> when {
				EOSWalletUtils.isValidAddress(address) -> {
					currentEOSAddress = address
					SharedValue.updateIsTestEnvironment(false)
					callback(GoldStoneError.None)
				}
				EOSAccount(address).isValid(false) -> {
					EOSAPI.getAccountInfo(EOSAccount(address), SharedChain.getEOSMainnet().getURL()) { info, error ->
						if (info.isNotNull() && error.isNone()) {
							eosMainnetAccountName = address
							SharedAddress.updateCurrentEOSName(address)
							SharedChain.updateEOSCurrent(
								ChainURL(ChainNodeTable.dao.getMainnetEOSNode())
							)
							SharedValue.updateIsTestEnvironment(false)
							callback(error)
						} else callback(AccountError.InactivatedAccountName)
					}
				}
				else -> callback(AccountError.InactivatedAccountName)
			}

			AddressType.EOSJungle.value -> {
				if (EOSWalletUtils.isValidAddress(address)) {
					currentEOSAddress = address
					SharedValue.updateIsTestEnvironment(true)
					callback(GoldStoneError.None)
				} else if (EOSAccount(address).isValid(false)) {
					EOSAPI.getAccountInfo(EOSAccount(address), SharedChain.getEOSTestnet().getURL()) { info, error ->
						if (info.isNotNull() || error.isNone()) {
							eosTestnetAccountName = address
							SharedAddress.updateCurrentEOSName(address)
							SharedChain.updateEOSCurrent(
								ChainURL(GoldStoneDataBase.database.chainNodeDao().getTestnetEOSNode())
							)
							SharedValue.updateIsTestEnvironment(true)
							callback(error)
						} else callback(AccountError.InvalidAccountName)
					}
				}
			}
			else -> {
				SharedValue.updateIsTestEnvironment(true)
				currentBTCTestAddress = address
				callback(GoldStoneError.None)
			}
		}
	}

	override fun onFragmentShowFromHidden() {
		super.onFragmentShowFromHidden()
		setRootChildFragmentBackEvent<ProfileOverlayFragment>(fragment)
		// 深度回退站恢复
		fragment.getParentFragment<ProfileOverlayFragment> {
			showBackButton(true) {
				presenter.popFragmentFrom<WatchOnlyImportFragment>()
			}
		}
	}
}