package io.goldstone.blockchain.module.common.tokenpayment.addressselection.presenter

import com.blinnnk.extension.*
import com.blinnnk.util.addFragmentAndSetArgument
import com.blinnnk.util.getParentFragment
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.language.ImportWalletText
import io.goldstone.blockchain.common.language.QRText
import io.goldstone.blockchain.common.language.TokenDetailText
import io.goldstone.blockchain.common.utils.alert
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.value.ContainerID
import io.goldstone.blockchain.crypto.bitcoin.AddressType
import io.goldstone.blockchain.crypto.multichain.*
import io.goldstone.blockchain.crypto.utils.MultiChainUtils
import io.goldstone.blockchain.kernel.commonmodel.QRCodeModel
import io.goldstone.blockchain.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blockchain.module.common.tokenpayment.addressselection.view.AddressSelectionFragment
import io.goldstone.blockchain.module.common.tokenpayment.deposit.presenter.DepositPresenter
import io.goldstone.blockchain.module.common.tokenpayment.paymentprepare.view.PaymentPrepareFragment
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blockchain.module.home.profile.contacts.contracts.model.ContactTable
import io.goldstone.blockchain.module.home.profile.contacts.contracts.view.ContactsAdapter
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.yesButton

/**
 * @date 28/03/2018 9:24 AM
 * @author KaySaith
 */
class AddressSelectionPresenter(
	override val fragment: AddressSelectionFragment
) : BaseRecyclerPresenter<AddressSelectionFragment, ContactTable>() {

	val token by lazy {
		fragment.getParentFragment<TokenDetailOverlayFragment>()?.token
	}

	override fun updateData() {
		updateAddressList {
			fragment.updateHeaderViewStatus()
		}
	}

	fun showPaymentPrepareFragmentByQRCode(result: String) {
		if (isValidQRCodeContent(result)) {
			if (result.contains("transfer")) {
				DepositPresenter.convertERC20QRCode(result).let {
					isCorrectCoinOrChainID(it) {
						showPaymentPrepareFragment(it.walletAddress, it.amount)
					}
				}
			} else {
				when {
					token?.contract.isBTCSeries() -> {
						val qrModel = DepositPresenter.convertBitcoinQRCode(result)
						if (qrModel.isNull()) fragment.context.alert(QRText.invalidContract)
						else isCorrectCoinOrChainID(qrModel!!) {
							showPaymentPrepareFragment(qrModel.walletAddress, qrModel.amount)
						}
					}

					token?.contract.isETC() || token?.contract.isETH() -> {
						DepositPresenter.convertETHOrETCQRCOde(result).let {
							isCorrectCoinOrChainID(it) {
								showPaymentPrepareFragment(it.walletAddress, it.amount)
							}
						}
					}
				}
			}
		} else {
			// 如果不是 `681` 格式的 `QRCode` 那么当作纯地址进行检测
			val addressType = MultiChainUtils.isValidMultiChainAddress(result, token?.symbol.orEmpty())
			if (
				addressType.isNull() || !addressType?.symbol.equals(token?.symbol, true)
			) fragment.context.alert(QRText.invalidQRCodeAlert)
			else showPaymentPrepareFragment(result, 0.0)
		}
	}

	fun showPaymentPrepareFragment(toAddress: String, count: Double = 0.0) {
		// 检查当前转账地址是否为本地任何一个钱包的正在使用的默认地址, 并提示告知用户.
		fun showAlertIfLocalExistThisAddress(localAddresses: List<String>) {
			localAddresses.any { it.equals(toAddress, true) } isTrue {
				alert(
					TokenDetailText.transferToLocalWalletAlertDescription,
					TokenDetailText.transferToLocalWalletAlertTitle
				) {
					goToPaymentPrepareFragment(toAddress, count)
				}
			} otherwise {
				goToPaymentPrepareFragment(toAddress, count)
			}
		}
		// 检查地址是否合规
		when (MultiChainUtils.isValidMultiChainAddress(toAddress, token?.symbol.orEmpty())) {
			null -> {
				fragment.context?.alert(ImportWalletText.addressFormatAlert)
				return
			}

			AddressType.ETHERCOrETC -> {
				if (token?.contract.isBTCSeries() || token?.contract.isEOS()) {
					fragment.context.alert("this is not a valid bitcoin address")
					return
				}

				WalletTable.getAllETHAndERCAddresses {
					showAlertIfLocalExistThisAddress(this)
				}
			}

			AddressType.EOS, AddressType.EOSAccountName -> {
				if (!token?.contract.isEOS()) {
					fragment.context.alert("this is not a valid eos account name")
					return
				}
				// 查询数据库对应的当前链下的全部 `EOS Account Name` 用来提示比对
				WalletTable.getAllEOSAccountNames {
					showAlertIfLocalExistThisAddress(this)
				}
			}

			AddressType.LTC -> {
				if (!token?.contract.isLTC()) {
					fragment.context.alert(
						"This is a invalid address type for ${CoinSymbol.ltc}, Please check it agin"
					)
					return
				} else {
					WalletTable.getAllLTCAddresses {
						showAlertIfLocalExistThisAddress(this)
					}
				}
			}

			AddressType.BCH -> {
				if (!token?.contract.isBCH()) {
					fragment.context.alert(
						"This is a invalid address type for ${CoinSymbol.bch}, Please check it agin"
					)
					return
				} else {
					WalletTable.getAllBCHAddresses {
						showAlertIfLocalExistThisAddress(this)
					}
				}
			}

			AddressType.BTC -> {
				if (Config.isTestEnvironment()) {
					fragment.context.alert(
						"this is a mainnet address, please switch your local net " +
							"setting in settings first"
					)
					return
				} else if (!token?.contract.isBTC()) {
					fragment.context.alert(
						"This is a invalid address type for ${CoinSymbol.btc()}, Please check it agin"
					)
					return
				} else {
					WalletTable.getAllBTCMainnetAddresses {
						showAlertIfLocalExistThisAddress(this)
					}
				}
			}

			AddressType.BTCSeriesTest -> {
				if (!Config.isTestEnvironment()) {
					fragment.context.alert(
						"this is a testnet address, please switch your local net " +
							"setting in settings first"
					)
					return
				} else if (!token?.contract.isBTCSeries()) {
					fragment.context.alert(
						"This is a invalid address type for Testnet, Please check it again"
					)
					return
				} else {
					WalletTable.getAllBTCSeriesTestnetAddresses {
						showAlertIfLocalExistThisAddress(this)
					}
				}
			}
		}
	}

	private fun isValidQRCodeContent(content: String): Boolean {
		return when {
			content.isEmpty() -> false
			!content.contains(":") -> false
			CryptoName.allChainName.none {
				content.contains(it.toLowerCase())
			} -> false
			content.length < CryptoValue.bitcoinAddressClassicLength -> false
			else -> true
		}
	}

	private fun isCorrectCoinOrChainID(qrModel: QRCodeModel, callback: () -> Unit) {
		when {
			token?.contract.isETC() -> {
				when {
					!TokenContract(qrModel.contractAddress).isETC() -> {
						fragment.context.alert(QRText.invalidContract)
						return
					}

					!qrModel.chainID.equals(Config.getETCCurrentChain().id, true) -> {
						fragment.context.alert(CommonText.wrongChainID)
						return
					}

					else -> callback()
				}
			}

			token?.contract.isEOS() -> {
				// TODO
			}

			token?.contract.isBTC() -> {
				when {
					!TokenContract(qrModel.contractAddress).isBTC() -> {
						fragment.context.alert(QRText.invalidContract)
						return
					}

					!qrModel.chainID.equals(Config.getBTCCurrentChain().id, true) -> {
						fragment.context.alert(CommonText.wrongChainID)
						return
					}

					else -> callback()
				}
			}

			token?.contract.isLTC() -> {
				when {
					!TokenContract(qrModel.contractAddress).isLTC() -> {
						fragment.context.alert(QRText.invalidContract)
						return
					}

					!qrModel.chainID.equals(Config.getLTCCurrentChain().id, true) -> {
						fragment.context.alert(CommonText.wrongChainID)
						return
					}

					else -> callback()
				}
			}

			token?.contract.isBCH() -> {
				when {
					!TokenContract(qrModel.contractAddress).isBCH() -> {
						fragment.context.alert(QRText.invalidContract)
						return
					}

					!qrModel.chainID.equals(Config.getBCHCurrentChain().id, true) -> {
						fragment.context.alert(CommonText.wrongChainID)
						return
					}

					else -> callback()
				}
			}

			else -> {
				when {
					!qrModel.contractAddress.equals(token?.contract?.contract, true) -> {
						fragment.context.alert(QRText.invalidContract)
						return
					}

					!qrModel.chainID.equals(Config.getCurrentChain().id, true) -> {
						fragment.context.alert(CommonText.wrongChainID)
						return
					}

					else -> callback()
				}
			}
		}
	}

	private fun alert(title: String, subtitle: String, callback: () -> Unit) {
		fragment.alert(title, subtitle) {
			yesButton { callback() }
			noButton { }
		}.show()
	}

	private fun goToPaymentPrepareFragment(address: String, count: Double = 0.0) {
		fragment.getParentFragment<TokenDetailOverlayFragment>()?.apply {
			hideChildFragment(fragment)
			addFragmentAndSetArgument<PaymentPrepareFragment>(ContainerID.content) {
				putString(ArgumentKey.paymentAddress, address)
				putDouble(ArgumentKey.paymentCount, count)
				putSerializable(ArgumentKey.tokenModel, token)
			}
			overlayView.header.apply {
				backButton.onClick {
					headerTitle = TokenDetailText.address
					presenter.popFragmentFrom<PaymentPrepareFragment>()
					showCloseButton(false)
				}
			}
			headerTitle = TokenDetailText.transferDetail
		}
	}

	override fun onFragmentShowFromHidden() {
		super.onFragmentShowFromHidden()
		/** 从下一个页面返回后通过显示隐藏监听重设回退按钮的事件 */
		fragment.getParentFragment<TokenDetailOverlayFragment>()?.apply {
			if (!isFromQuickTransfer) {
				overlayView.header.showBackButton(true) {
					presenter.popFragmentFrom<AddressSelectionFragment>()
				}
			}
		}
	}

	private fun updateAddressList(callback: () -> Unit) {
		ContactTable.getAllContacts { contacts ->
			contacts.isEmpty() isTrue {
				fragment.asyncData = arrayListOf()
			} otherwise {
				if (fragment.asyncData.isNullOrEmpty()) {
					// 根据当前的 `Coin Type` 来选择展示地址的哪一项
					fragment.asyncData = when {
						token?.contract.isBTC() -> contacts.map {
							it.apply {
								defaultAddress =
									if (Config.isTestEnvironment()) it.btcSeriesTestnetAddress
									else it.btcMainnetAddress
							}
						}.toArrayList()
						token?.contract.isLTC() -> contacts.map {
							it.apply {
								defaultAddress =
									if (Config.isTestEnvironment()) it.btcSeriesTestnetAddress
									else it.ltcAddress
							}
						}.toArrayList()
						token?.contract.isBCH() -> contacts.map {
							it.apply {
								defaultAddress =
									if (Config.isTestEnvironment()) it.btcSeriesTestnetAddress
									else it.bchAddress
							}
						}.toArrayList()
						else -> contacts.map {
							it.apply {
								defaultAddress = ethERCAndETCAddress
							}
						}.toArrayList()
					}
				} else {
					diffAndUpdateSingleCellAdapterData<ContactsAdapter>(contacts)
				}
			}
			callback()
		}
	}
}