package io.goldstone.blockchain.module.common.tokendetail.tokendetail.presenter

import android.os.Bundle
import com.blinnnk.extension.*
import com.blinnnk.uikit.AnimationDuration
import com.blinnnk.util.getParentFragment
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.language.LoadingText
import io.goldstone.blockchain.common.language.TokenDetailText
import io.goldstone.blockchain.common.language.TransactionText
import io.goldstone.blockchain.common.utils.*
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.ChainID
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.value.WalletType
import io.goldstone.blockchain.crypto.multichain.ChainType
import io.goldstone.blockchain.crypto.multichain.CryptoSymbol
import io.goldstone.blockchain.crypto.utils.CryptoUtils
import io.goldstone.blockchain.crypto.utils.daysAgoInMills
import io.goldstone.blockchain.kernel.commonmodel.BTCSeriesTransactionTable
import io.goldstone.blockchain.kernel.commonmodel.MyTokenTable
import io.goldstone.blockchain.kernel.commonmodel.TransactionTable
import io.goldstone.blockchain.kernel.commonmodel.eos.EOSTransactionTable
import io.goldstone.blockchain.module.common.tokendetail.tokendetail.model.TokenBalanceTable
import io.goldstone.blockchain.module.common.tokendetail.tokendetail.view.TokenDetailAdapter
import io.goldstone.blockchain.module.common.tokendetail.tokendetail.view.TokenDetailFragment
import io.goldstone.blockchain.module.common.tokendetail.tokendetail.view.TokenDetailHeaderView
import io.goldstone.blockchain.module.common.tokendetail.tokendetailcenter.view.TokenDetailCenterFragment
import io.goldstone.blockchain.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blockchain.module.home.quotation.quotation.model.ChartPoint
import io.goldstone.blockchain.module.home.wallet.transactions.transactiondetail.view.TransactionDetailFragment
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.model.TransactionListModel

/**
 * @date 27/03/2018 3:21 PM
 * @author KaySaith
 */
class TokenDetailPresenter(
	override val fragment: TokenDetailFragment
) : BaseRecyclerPresenter<TokenDetailFragment, TransactionListModel>() {

	private var allData: List<TransactionListModel>? = null
	val token by lazy {
		fragment.getParentFragment<TokenDetailCenterFragment>()?.token
	}

	override fun updateData() {
		fragment.asyncData = arrayListOf()
		updateEmptyCharData(fragment.token?.symbol.orEmpty())
		// 错开动画和数据读取的时间, 避免 `UI` 可能的卡顿
		AnimationDuration.Default timeUpThen {
			prepareTokenDetailData()
		}
	}

	fun showOnlyReceiveData() {
		allData?.filter {
			it.isReceived
		}?.let {
			diffAndUpdateAdapterData<TokenDetailAdapter>(it.toArrayList())
		}
	}

	fun showOnlyFailedData() {
		allData?.filter {
			it.isFailed || it.hasError
		}?.let {
			diffAndUpdateAdapterData<TokenDetailAdapter>(it.toArrayList())
		}
	}

	fun showOnlySendData() {
		allData?.filter {
			!it.isReceived && !it.isFee
		}?.let {
			diffAndUpdateAdapterData<TokenDetailAdapter>(it.toArrayList())
		}
	}

	fun showAllData() {
		allData?.let {
			diffAndUpdateAdapterData<TokenDetailAdapter>(it.toArrayList())
		}
	}

	fun showAddressSelectionFragment() {
		fragment.getGrandFather<TokenDetailOverlayFragment>()
			?.presenter?.showAddressSelectionFragment()
	}

	fun showDepositFragment() {
		fragment.getGrandFather<TokenDetailOverlayFragment>()?.presenter?.showDepositFragment()
	}

	fun showTransactionDetailFragment(model: TransactionListModel) {
		val argument = Bundle().apply {
			putSerializable(ArgumentKey.transactionFromList, model)
		}
		fragment.getGrandFather<TokenDetailOverlayFragment>()?.apply {
			presenter.showTargetFragment<TransactionDetailFragment>(
				TransactionText.detail,
				TokenDetailText.tokenDetail, argument
			)
		}
	}

	private fun prepareTokenDetailData() {
		fragment.showLoadingView(LoadingText.tokenData)
		loadDataFromDatabaseOrElse { ethERC20OrETCLocalData, localBTCSeriesData, localEOSSeriesData ->
			NetworkUtil.hasNetworkWithAlert(fragment.context) isTrue {
				// `BTCSeries` 的拉取账单及更新账单需要使用 `localDataMaxIndex`
				// `ETHERC20OrETC` 需要使用到 `localData`
				when {
					CryptoSymbol.isBTCSeriesSymbol(token?.symbol) -> {
						// This localDataMaxIndex is BTCSeries Transactions Only
						val localDataMaxIndex = localBTCSeriesData?.maxBy { it.dataIndex }?.dataIndex ?: 0
						fragment.loadDataFromChain(listOf(), localDataMaxIndex)
					}
					token?.symbol.equals(CryptoSymbol.eos, true) -> {
						// This localDataMaxIndex is EOSSeries Transactions Only
						val localDataMaxIndex = localEOSSeriesData?.maxBy { it.dataIndex }?.dataIndex ?: 0
						fragment.loadDataFromChain(listOf(), localDataMaxIndex)
					}
					!ethERC20OrETCLocalData.isNull() || !ethERC20OrETCLocalData?.isEmpty().orFalse() -> {
						fragment.loadDataFromChain(ethERC20OrETCLocalData!!, 0)
					}
				}
			}
		}
	}

	private var hasUpdateData = false

	private fun TokenDetailFragment.loadDataFromChain(
		localETHERC20OrETCData: List<TransactionListModel>,
		localDataMaxIndex: Int
	) {
		when {
			token?.symbol.equals(CryptoSymbol.etc, true) -> {
				if (!hasUpdateData) loadETCChainData(localETHERC20OrETCData)
				hasUpdateData = true
			}

			token?.symbol.equals(CryptoSymbol.pureBTCSymbol, true) -> {
				if (!hasUpdateData) loadBTCChainData(localDataMaxIndex)
				hasUpdateData = true
			}

			token?.symbol.equals(CryptoSymbol.bch, true) -> {
				if (!hasUpdateData) loadBCHChainData(localDataMaxIndex)
				hasUpdateData = true
			}

			token?.symbol.equals(CryptoSymbol.ltc, true) -> {
				if (!hasUpdateData) loadLTCChainData(localDataMaxIndex)
				hasUpdateData = true
			}

			token?.symbol.equals(CryptoSymbol.eth, true) -> {
				if (!hasUpdateData) loadETHChainData(localETHERC20OrETCData)
				hasUpdateData = true
			}

			token?.symbol.equals(CryptoSymbol.eos, true) -> {
				if (!hasUpdateData) loadEOSDataFromChain(localDataMaxIndex)
				hasUpdateData = true
			}

			else -> {
				if (!hasUpdateData) loadERCChainData(localETHERC20OrETCData)
				hasUpdateData = true
			}
		}
	}

	fun loadDataFromDatabaseOrElse(
		callback: (
			localETHERC20OrETCData: List<TransactionListModel>?,
			localBTCSeriesData: List<BTCSeriesTransactionTable>?,
			localEOSSeriesData: List<EOSTransactionTable>?
		) -> Unit
	) {
		when (Config.getCurrentWalletType()) {
			WalletType.Bip44MultiChain.content, WalletType.MultiChain.content -> {
				when {
					token?.symbol.equals(CryptoSymbol.etc, true) ->
						getETHERC20OrETCData(Config.getCurrentETCAddress()) {
							callback(it, null, null)
						}

					token?.symbol.equals(CryptoSymbol.pureBTCSymbol, true) -> {
						getBTCSeriesData(
							AddressUtils.getCurrentBTCAddress(),
							ChainType.BTC.id
						) {
							callback(null, it, null)
						}
					}

					token?.symbol.equals(CryptoSymbol.ltc, true) -> {
						getBTCSeriesData(
							AddressUtils.getCurrentLTCAddress(),
							ChainType.LTC.id
						) {
							callback(null, it, null)
						}
					}

					token?.symbol.equals(CryptoSymbol.bch, true) -> {
						getBTCSeriesData(
							AddressUtils.getCurrentBCHAddress(),
							ChainType.BCH.id
						) {
							callback(null, it, null)
						}
					}

					token?.symbol.equals(CryptoSymbol.eos, true) -> getEOSSeriesData {
						callback(null, null, it)
					}

					else -> getETHERC20OrETCData(Config.getCurrentEthereumAddress()) {
						callback(it, null, null)
					}
				}
			}

			WalletType.BCHOnly.content ->
				getBTCSeriesData(
					Config.getCurrentBCHAddress(),
					ChainType.BCH.id
				) {
					callback(null, it, null)
				}

			WalletType.ETHERCAndETCOnly.content ->
				getETHERC20OrETCData(Config.getCurrentEthereumAddress()) {
					callback(it, null, null)
				}

			WalletType.BTCTestOnly.content -> {
				getBTCSeriesData(
					Config.getCurrentBTCSeriesTestAddress(),
					ChainType.BTC.id
				) {
					callback(null, it, null)
				}
			}

			WalletType.BTCOnly.content -> {
				getBTCSeriesData(
					Config.getCurrentBTCAddress(),
					ChainType.BTC.id
				) {
					callback(null, it, null)
				}
			}

			WalletType.LTCOnly.content -> {
				getBTCSeriesData(
					Config.getCurrentLTCAddress(),
					ChainType.LTC.id
				) {
					callback(null, it, null)
				}
			}
		}
	}

	private fun getETHERC20OrETCData(
		address: String,
		callback: (List<TransactionListModel>) -> Unit
	) {
		TransactionTable.getCurrentChainByAddressAndContract(
			address,
			fragment.token?.contract.orEmpty(),
			ChainID.getChainIDBySymbol(fragment.token?.symbol.orEmpty())
		) { transactions ->
			transactions.isNotEmpty() isTrue {
				fragment.updatePageBy(transactions, address)
				fragment.removeLoadingView()
			}
			callback(transactions)
		}
	}

	private fun getEOSSeriesData(callback: (List<EOSTransactionTable>) -> Unit) {
		val accountName = Config.getCurrentEOSName()
		EOSTransactionTable.getTransactionByAccountName(
			accountName,
			Config.getEOSCurrentChain()
		) { transactions ->
			transactions.isNotEmpty() isTrue {
				fragment.updatePageBy(
					transactions.filter {
						// EOS 会存在一些 `FromName` 或 `ToName` 都为空的坏账这里过滤一下
						it.transactionData.fromName.isNotEmpty()
					}.map {
						TransactionListModel(it)
					}.sortedByDescending {
						it.timeStamp
					},
					accountName
				)
				fragment.removeLoadingView()
			}
			callback(transactions)
		}
	}

	private fun getBTCSeriesData(
		address: String,
		chainType: Int,
		callback: (List<BTCSeriesTransactionTable>) -> Unit
	) {
		BTCSeriesTransactionTable
			.getTransactionsByAddressAndChainType(address, chainType) { transactions ->
				transactions.isNotEmpty() isTrue {
					fragment.updatePageBy(
						transactions.map {
							TransactionListModel(it)
						}.sortedByDescending {
							it.timeStamp
						},
						address
					)
					fragment.removeLoadingView()
				}
				callback(transactions)
			}
	}

	fun TokenDetailFragment.updatePageBy(
		data: List<TransactionListModel>,
		ownerName: String
	) {
		allData = data
		checkAddressNameInContacts(data) {
			diffAndUpdateAdapterData<TokenDetailAdapter>(data.toArrayList())
			// 显示内存的数据后异步更新数据
			NetworkUtil.hasNetworkWithAlert(context) isTrue {
				data.prepareTokenHistoryBalance(token?.contract.orEmpty(), ownerName) {
					it.updateChartAndHeaderData()
				}
			} otherwise {
				updateEmptyCharData(token?.symbol.orEmpty())
			}
		}
	}

	private fun updateEmptyCharData(symbol: String) {
		// 没网的时候返回空数据
		val now = System.currentTimeMillis()
		var emptyData = listOf<TokenBalanceTable>()
		(0 until 7).forEach {
			emptyData += TokenBalanceTable(symbol, now)
		}
		emptyData.updateChartAndHeaderData()
	}

	private fun List<TokenBalanceTable>.updateChartAndHeaderData() {
		fragment.recyclerView.getItemAtAdapterPosition<TokenDetailHeaderView>(0) { header ->
			val maxChartCount = 7
			val chartArray = arrayListOf<ChartPoint>()
			val charCount = if (size > maxChartCount) maxChartCount else size
			forEach {
				chartArray.add(
					ChartPoint(
						CryptoUtils.dateInDay(it.date),
						it.balance.toBigDecimal().toFloat()
					)
				)
				if (chartArray.size == charCount) {
					header.setCharData(chartArray.reversed().toArrayList())
				}
			}
		}
	}

	private fun List<TransactionListModel>.prepareTokenHistoryBalance(
		contract: String,
		ownerName: String,
		callback: (List<TokenBalanceTable>) -> Unit
	) {
		// 首先更新此刻最新的余额数据到今天的数据
		MyTokenTable.getTokenBalance(contract, ownerName) { todayBalance ->
			if (todayBalance.isNull()) return@getTokenBalance
			// 计算过去7天的所有余额
			generateHistoryBalance(todayBalance.orZero()) { history ->
				load {
					history.forEach { data ->
						TokenBalanceTable.insertOrUpdate(
							contract,
							ownerName,
							data.date,
							data.balance
						)
					}
				} then { _ ->
					// 更新数据完毕后在主线程从新从数据库获取数据
					TokenBalanceTable.getBalanceByContract(contract, ownerName) {
						callback(it)
					}
				}
			}
		}
	}

	data class DateBalance(val date: Long, val balance: Double)

	private fun List<TransactionListModel>.generateHistoryBalance(
		todayBalance: Double,
		callback: (List<DateBalance>) -> Unit
	) {
		val maxCount = 6
		var balances = listOf<DateBalance>()
		var balance = todayBalance
		object : ConcurrentAsyncCombine() {
			override var asyncCount: Int = maxCount
			override fun concurrentJobs() {
				(0 until maxCount).forEach { index ->
					val currentMills =
						if (index == 0) System.currentTimeMillis() else (index - 1).daysAgoInMills()
					(balance - filter {
						it.timeStamp.toMillisecond() in index.daysAgoInMills() .. currentMills
					}.sumByDouble {
						if (it.isFee) {
							it.minerFee.substringBefore(" ").toDouble() * -1
						} else {
							it.value.toDouble() * modulusByReceiveStatus(it.isReceived)
						}
					}).let {
						balance = it.toBigDecimal().toDouble()
						balances += DateBalance((index + 1).daysAgoInMills(), balance)
						completeMark()
					}
				}
			}

			override fun mergeCallBack() {
				balances += DateBalance(0.daysAgoInMills(), todayBalance)
				callback(balances)
			}
		}.start()
	}

	private fun modulusByReceiveStatus(isReceived: Boolean) = if (isReceived) 1 else -1
}