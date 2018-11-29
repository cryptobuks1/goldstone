package io.goldstone.blockchain.module.common.tokendetail.tokenasset.view

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.clickToCopy
import com.blinnnk.util.getParentFragment
import io.goldstone.blockchain.R
import io.goldstone.blockchain.common.base.gsfragment.GSFragment
import io.goldstone.blockchain.common.base.view.GrayCardView
import io.goldstone.blockchain.common.component.ProcessType
import io.goldstone.blockchain.common.component.ProgressView
import io.goldstone.blockchain.common.component.cell.GraySquareCell
import io.goldstone.blockchain.common.component.overlay.Dashboard
import io.goldstone.blockchain.common.component.overlay.LoadingView
import io.goldstone.blockchain.common.component.title.SessionTitleView
import io.goldstone.blockchain.common.language.*
import io.goldstone.blockchain.common.sharedpreference.SharedAddress
import io.goldstone.blockchain.common.sharedpreference.SharedWallet
import io.goldstone.blockchain.common.thread.launchUI
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.utils.safeShowError
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.GrayScale
import io.goldstone.blockchain.common.value.ScreenSize
import io.goldstone.blockchain.common.value.fontSize
import io.goldstone.blockchain.crypto.eos.account.EOSAccount
import io.goldstone.blockchain.crypto.eos.base.showDialog
import io.goldstone.blockchain.crypto.multichain.CoinSymbol
import io.goldstone.blockchain.crypto.multichain.TokenContract
import io.goldstone.blockchain.crypto.multichain.getAddress
import io.goldstone.blockchain.module.common.tokendetail.eosactivation.accountselection.view.EOSAccountSelectionFragment
import io.goldstone.blockchain.module.common.tokendetail.eosresourcetrading.cputradingdetail.view.CPUTradingFragment
import io.goldstone.blockchain.module.common.tokendetail.eosresourcetrading.nettradingdetail.view.NETTradingFragment
import io.goldstone.blockchain.module.common.tokendetail.eosresourcetrading.ramtradingdetail.view.RAMTradingFragment
import io.goldstone.blockchain.module.common.tokendetail.tokenasset.contract.TokenAssetContract
import io.goldstone.blockchain.module.common.tokendetail.tokenasset.presenter.TokenAssetPresenter
import io.goldstone.blockchain.module.common.tokendetail.tokendetailcenter.view.TokenDetailCenterFragment
import io.goldstone.blockchain.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blockchain.module.common.tokendetail.tokeninfo.presenter.TokenInfoPresenter
import io.goldstone.blockchain.module.common.tokendetail.tokeninfo.view.TokenInfoView
import io.goldstone.blockchain.module.home.wallet.walletsettings.qrcodefragment.presenter.QRCodePresenter
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.UI
import java.math.BigInteger


/**
 * @author KaySaith
 * @date  2018/09/10
 */

class TokenAssetFragment : GSFragment(), TokenAssetContract.GSView {

	override val pageTitle: String = "Asset"
	private val token by lazy {
		getParentFragment<TokenDetailCenterFragment>()?.token
	}
	private lateinit var tokenInfoView: TokenInfoView
	private lateinit var balanceCell: GraySquareCell
	private lateinit var refundsCell: GraySquareCell
	private lateinit var delegateBandWidthCell: GraySquareCell
	private lateinit var transactionCountCell: GraySquareCell
	private lateinit var authorizationCell: GraySquareCell
	private lateinit var accountAddress: GraySquareCell
	private lateinit var assetCard: GrayCardView
	private lateinit var ramAssetCell: ProgressView
	private lateinit var cpuAssetCell: ProgressView
	private lateinit var netAssetCell: ProgressView
	private var loadingView: LoadingView? = null

	override lateinit var presenter: TokenAssetContract.GSPresenter
	override fun onResume() {
		super.onResume()
		presenter = TokenAssetPresenter(this)
		presenter.start()
		token?.let { setAccountInfo(it.contract) }
	}

	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		if (!hidden) presenter.start()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return UI {
			scrollView {
				lparams(matchParent, matchParent)
				verticalLayout {
					lparams(matchParent, wrapContent)
					bottomPadding = 20.uiPX()
					gravity = Gravity.CENTER_HORIZONTAL
					tokenInfoView = TokenInfoView(context)
					tokenInfoView.into(this)
					showAccountManagementCells()
					showTransactionCells()
					showAssetDashboard()
					SessionTitleView(context).setTitle(TokenDetailText.assetTools).into(this)
					linearLayout {
						lparams {
							width = ScreenSize.card
							height = wrapContent
						}
						listOf(
							Pair(R.drawable.cpu_icon, TokenDetailText.delegateCPU),
							Pair(R.drawable.net_icon, TokenDetailText.delegateNET),
							Pair(R.drawable.ram_icon, TokenDetailText.buySellRAM)
						).forEach { pair ->
							generateCardView(pair)
						}
					}
				}
			}
		}.view
	}

	override fun setTransactionCount(count: Int) {
		transactionCountCell.setSubtitle(count.toString())
	}

	override fun showError(error: Throwable) {
		safeShowError(error)
	}

	private fun setAccountInfo(contract: TokenContract) {
		val info = TokenInfoPresenter.getDetailButtonInfo(contract)
		val code = QRCodePresenter.generateQRCode(contract.getAddress())
		val chainName = CoinSymbol.eos suffix TokenDetailText.chainType
		tokenInfoView.setData(code, chainName, CommonText.calculating, info.first) {
			TokenInfoPresenter.showThirdPartyAddressDetail(
				getGrandFather<TokenDetailOverlayFragment>(),
				info.second
			)
		}
	}

	private fun showPublicKeyAccountNames() {
		getGrandFather<TokenDetailOverlayFragment>()
			?.presenter?.showTargetFragment<EOSAccountSelectionFragment>(
			Bundle().apply {
				putString(
					ArgumentKey.defaultEOSAccountName,
					SharedAddress.getCurrentEOSAccount().name
				)
			},
			2
		)
	}

	private fun showTradingFragment(title: String) {
		val tokenDetailOverlayPresenter =
			getGrandFather<TokenDetailOverlayFragment>()?.presenter
		when (title) {
			TokenDetailText.delegateCPU -> tokenDetailOverlayPresenter
				?.showTargetFragment<CPUTradingFragment>(Bundle(), 2)
			TokenDetailText.delegateNET -> tokenDetailOverlayPresenter
				?.showTargetFragment<NETTradingFragment>(Bundle(), 2)
			TokenDetailText.buySellRAM -> tokenDetailOverlayPresenter
				?.showTargetFragment<RAMTradingFragment>(Bundle(), 2)
		}
	}

	override fun setEOSBalance(balance: String) {
		balanceCell.setSubtitle(balance)
	}

	override fun setEOSRefunds(description: String) {
		refundsCell.setSubtitle(description)
	}

	override fun setEOSDelegateBandWidth(value: String) {
		delegateBandWidthCell.setSubtitle(value)
	}

	override fun setResourcesValue(
		ramAvailable: BigInteger,
		ramTotal: BigInteger,
		ramEOSCount: String,
		cpuAvailable: BigInteger,
		cpuTotal: BigInteger,
		cpuWeight: String,
		netAvailable: BigInteger,
		netTotal: BigInteger,
		netWeight: String
	) {
		ramAssetCell.setLeftValue(ramAvailable, TokenDetailText.available, ProcessType.Disk)
		ramAssetCell.setRightValue(ramTotal, TokenDetailText.total, ProcessType.Disk)
		ramAssetCell.setSubtitle(ramEOSCount)
		cpuAssetCell.setSubtitle(cpuWeight)
		cpuAssetCell.setLeftValue(
			cpuAvailable,
			TokenDetailText.available,
			ProcessType.Time
		)
		cpuAssetCell.setRightValue(
			cpuTotal,
			TokenDetailText.total,
			ProcessType.Time
		)
		netAssetCell.setSubtitle(netWeight)
		netAssetCell.setLeftValue(
			netAvailable,
			TokenDetailText.available,
			ProcessType.Disk
		)
		netAssetCell.setRightValue(
			netTotal,
			TokenDetailText.total,
			ProcessType.Disk
		)
	}

	private fun ViewGroup.showAccountManagementCells() {
		SessionTitleView(context).setTitle(TokenDetailText.accountManagement).into(this)
		authorizationCell = GraySquareCell(context).apply {
			showArrow()
			setTitle(EOSAccountText.authority)
			setSubtitle(SharedAddress.getCurrentEOSAccount().name)
			click {
				val type = SharedWallet.getCurrentWalletType()
				when {
					type.isEOSMainnet() || type.isEOSJungle() ->
						safeShowError(Throwable(WalletText.watchOnly))
					else -> showPublicKeyAccountNames()
				}
			}
		}
		authorizationCell.into(this)

		accountAddress = GraySquareCell(context).apply {
			setTitle(EOSAccountText.publicKey)
			val address =
				if (SharedAddress.getCurrentEOS().isEmpty()) "Account Name Only"
				else SharedAddress.getCurrentEOS()
			setSubtitle(address, true)
			click {
				context?.clickToCopy(SharedAddress.getCurrentEOS())
			}
		}
		accountAddress.into(this)
	}

	private fun ViewGroup.showTransactionCells() {
		SessionTitleView(context).setTitle(TokenDetailText.balance).into(this)
		balanceCell = GraySquareCell(context).apply {
			setTitle(TokenDetailText.balance)
			setSubtitle(CommonText.calculating)
		}
		balanceCell.into(this)
		delegateBandWidthCell = GraySquareCell(context).apply {
			showArrow()
			setTitle(TokenDetailText.delband)
			setSubtitle(CommonText.calculating)
		}
		delegateBandWidthCell.click {
			showDelegateBandWidthDashboard()
		}.into(this)
		refundsCell = GraySquareCell(context).apply {
			setTitle(TokenDetailText.refunds)
			setSubtitle(CommonText.calculating)
		}
		refundsCell.into(this)

		transactionCountCell = GraySquareCell(context).apply {
			setTitle(TokenDetailText.transactionCount)
			setSubtitle(CommonText.calculating)
		}
		transactionCountCell.into(this)
	}

	private fun ViewGroup.showAssetDashboard() {
		SessionTitleView(context).setTitle(TokenDetailText.resources).into(this)
		assetCard = GrayCardView(context).apply {
			layoutParams = RelativeLayout.LayoutParams(ScreenSize.card, 255.uiPX())
		}
		assetCard.into(this)

		ramAssetCell = ProgressView(context).apply {
			setTitle(TokenDetailText.ram)
			setSubtitle(CommonText.calculating)
		}

		cpuAssetCell = ProgressView(context).apply {
			setTitle(TokenDetailText.cpu)
			setSubtitle(CommonText.calculating)
		}

		netAssetCell = ProgressView(context).apply {
			setTitle(TokenDetailText.net)
			setSubtitle(CommonText.calculating)
		}

		assetCard.addContent {
			addView(ramAssetCell)
			addView(cpuAssetCell)
			addView(netAssetCell)
		}
	}

	override fun showCenterLoading(status: Boolean) = launchUI {
		if (loadingView.isNull()) loadingView = LoadingView(context!!)
		if (status) loadingView?.show() else loadingView?.remove()
	}

	private fun showDelegateBandWidthDashboard() {
		presenter.getDelegateBandWidthData {
			launchUI {
				Dashboard(context!!) {
					showList(
						TokenDetailText.delegateBandWidth,
						DelegateBandwidthAdapter(it) {
							if (SharedWallet.isWatchOnlyWallet()) {
								showError(Throwable(AlertText.watchOnly))
							} else showRefundBandwidthEditorDashboard(EOSAccount(toName))
						}
					)
				}
			}
		}
	}

	private fun Dashboard.showRefundBandwidthEditorDashboard(receiver: EOSAccount) {
		with(dialog) {
			cancelOnTouchOutside(false)
			setContentView(
				DelegateEditorView(context).apply {
					setTitle(TokenDetailText.delegateDetailTitle)
					closeEvent = Runnable { dismiss() }
					confirmEvent = Runnable {
						showLoading(true)
						presenter.redemptionBandwidth(
							getPassword(),
							receiver,
							getCPUAMount(),
							getNetAmount()
						) { response, error ->
							launchUI {
								if (response.isNotNull() && error.isNone()) launchUI {
									dialog.dismiss()
									showLoading(false)
									presenter.updateRefundInfo()
									response.showDialog(context)
								} else {
									showLoading(false)
									showError(error)
								}
							}
						}
					}
				},
				LinearLayout.LayoutParams(matchParent, wrapContent)
			)
		}
	}

	private fun ViewGroup.generateCardView(info: Pair<Int, String>) {
		val cardWidth = ScreenSize.card / 3 - 4.uiPX()
		val cardView = GrayCardView(context).apply {
			layoutParams = RelativeLayout.LayoutParams(cardWidth, 135.uiPX())
			container.apply {
				onClick {
					if (SharedWallet.isWatchOnlyWallet())
						safeShowError(Throwable(AlertText.watchOnly))
					else showTradingFragment(info.second)
					preventDuplicateClicks()
				}
				imageView {
					setColorFilter(GrayScale.gray)
					scaleType = ImageView.ScaleType.CENTER_INSIDE
					imageResource = info.first
					layoutParams = RelativeLayout.LayoutParams(cardWidth, 75.uiPX())
				}
				textView(info.second) {
					textSize = fontSize(11)
					textColor = GrayScale.midGray
					typeface = GoldStoneFont.black(context)
					layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
					gravity = Gravity.CENTER_HORIZONTAL
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
						lineHeight = 13.uiPX()
					}
				}
			}
		}
		cardView.into(this)
		cardView.setMargins<LinearLayout.LayoutParams> {
			leftMargin = 2.uiPX()
			rightMargin = 2.uiPX()
		}
	}
}