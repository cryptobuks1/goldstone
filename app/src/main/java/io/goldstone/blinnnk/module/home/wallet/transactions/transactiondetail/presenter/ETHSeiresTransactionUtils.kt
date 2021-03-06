package io.goldstone.blinnnk.module.home.wallet.transactions.transactiondetail.presenter

import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import com.blinnnk.extension.isNotNull
import io.goldstone.blinnnk.common.error.RequestError
import io.goldstone.blinnnk.common.sharedpreference.SharedChain
import io.goldstone.blinnnk.common.thread.launchDefault
import io.goldstone.blinnnk.common.thread.launchUI
import io.goldstone.blinnnk.crypto.multichain.node.ChainURL
import io.goldstone.blinnnk.crypto.utils.CryptoUtils
import io.goldstone.blinnnk.kernel.commontable.TransactionTable
import io.goldstone.blinnnk.kernel.database.GoldStoneDataBase
import io.goldstone.blinnnk.kernel.network.ethereum.ETHJsonRPC
import io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable
import io.goldstone.blinnnk.module.home.wallet.transactions.transactiondetail.model.TransactionSealedModel
import io.goldstone.blinnnk.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.model.getMemoFromInputCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * @author KaySaith
 * @date  2018/11/08
 */
object ETHSeriesTransactionUtils {
	@WorkerThread
	fun updateERC20FeeInfo(
		data: TransactionSealedModel,
		callback: (symbol: String?, count: Double?, error: RequestError) -> Unit
	) {
		launchDefault {
			val chainURL = SharedChain.getCurrentETH()
			// 这种情况意味着没有燃气费的条目是 `ERC20` 的条目, 没有换算出 `Symbol` 和 `Decimal`
			// 从而导致的 `Count` 为 `0` 的情况, 这里需要阻碍 `UI` 显示, 更新到这个数据后再允许显示下一步
			val defaultDao =
				GoldStoneDataBase.database.defaultTokenDao()
			val transactionDao =
				GoldStoneDataBase.database.transactionDao()
			val targetToken =
				defaultDao.getERC20Token(data.contract.contract, chainURL.chainID.id)
			// 如果本地有该条燃气费的 `DefaultToken` 信息那么直接从数据库获取信息并补全
			// 否则就获取 `ContractAddress` 从链上查询对应的数据并补全本地信息
			if (targetToken.isNotNull()) {
				val count =
					CryptoUtils.toCountByDecimal(data.value, targetToken.decimals)
				transactionDao.updateFeeInfo(targetToken.symbol, count, data.hash)
				callback(targetToken.symbol, count, RequestError.None)
			} else ETHJsonRPC.getTokenInfoByContractAddress(data.contract.contract, chainURL) { symbol, name, decimal, error ->
				if (error.isNone()) {
					val count = CryptoUtils.toCountByDecimal(data.value, decimal!!)
					transactionDao.updateFeeInfo(symbol!!, count, data.hash)
					defaultDao.insert(
						DefaultTokenTable(
							data.contract.contract,
							symbol,
							decimal,
							chainURL.chainID,
							"",
							name!!,
							true
						)
					)
					callback(symbol, count, error)
				} else callback(null, null, error)
			}
		}
	}

	@WorkerThread
	fun getMemoFromChain(
		hash: String,
		isReceive: Boolean,
		isFee: Boolean,
		chainURL: ChainURL,
		hold: (memo: String?, error: RequestError) -> Unit
	) {
		ETHJsonRPC.getInputCodeByHash(hash, chainURL) { inputCode, error ->
			if (inputCode.isNotNull() && error.isNone()) {
				val memo = getMemoFromInputCode(inputCode)
				val transactionDao =
					GoldStoneDataBase.database.transactionDao()
				// 如果数据库有这条数据那么更新 `Memo` 和 `Input`
				transactionDao.updateInputCodeAndMemo(inputCode, memo, hash, isReceive, isFee)
				transactionDao.updateFeeMemo(hash, memo)
				hold(memo, error)
			} else hold(null, error)
		}
	}

	@WorkerThread
	fun getCurrentConfirmationNumber(
		blockNumber: Int,
		chainURL: ChainURL,
		@WorkerThread hold: (confirmationCount: Int?, error: RequestError) -> Unit
	) {
		ETHJsonRPC.getBlockCount(chainURL) { blockCount, error ->
			if (blockCount.isNotNull() && error.isNone()) {
				hold(blockCount - blockNumber, error)
			} else hold(null, error)
		}
	}

	fun getTransactionByHash(
		hash: String,
		isReceive: Boolean,
		chainURL: ChainURL,
		timestamp: String,
		@UiThread hold: (data: TransactionSealedModel?, error: RequestError) -> Unit
	) {
		// 先从本地找数据, 找不到就拉取链上数据并插入本地数据库
		GlobalScope.launch(Dispatchers.Default) {
			val targetData =
				TransactionTable.dao.getByTaxHashAndReceivedStatus(hash, isReceive, false)
			if (targetData.isNotNull()) launchUI {
				hold(TransactionSealedModel(targetData), RequestError.None)
			} else ETHJsonRPC.getTransactionByHash(hash, chainURL) { transaction, error ->
				if (transaction.isNotNull() && error.isNone()) {
					val formattedData =
						transaction.apply { this.timeStamp = timestamp }
					TransactionTable.dao.insert(formattedData)
					launchUI {
						hold(TransactionSealedModel(formattedData), error)
					}
				} else hold(null, error)
			}
		}
	}
}