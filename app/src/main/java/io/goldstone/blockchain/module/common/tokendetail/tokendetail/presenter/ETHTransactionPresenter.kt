package io.goldstone.blockchain.module.common.tokendetail.tokendetail.presenter

import android.support.annotation.WorkerThread
import io.goldstone.blockchain.common.error.RequestError
import io.goldstone.blockchain.common.sharedpreference.SharedAddress
import io.goldstone.blockchain.crypto.utils.CryptoUtils
import io.goldstone.blockchain.kernel.commonmodel.TransactionTable
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import io.goldstone.blockchain.kernel.network.common.RequisitionUtil
import io.goldstone.blockchain.kernel.network.ethereum.EtherScanApi
import io.goldstone.blockchain.module.home.profile.contacts.contracts.model.ContactTable
import io.goldstone.blockchain.module.home.profile.contacts.contracts.model.getContactName
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.model.ETHTransactionModel
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.model.TransactionListModel

/**
 * @date 2018/8/20 2:51 PM
 * @author KaySaith
 */

@WorkerThread
fun TokenDetailPresenter.loadETHChainData(blockNumber: Int) {
	updateLocalETHTransactions(blockNumber) {
		if (it.isNone()) loadLocalData()
	}
}

@WorkerThread
fun updateLocalETHTransactions(startBlock: Int, callback: (RequestError) -> Unit) {
	RequisitionUtil.requestUnCryptoData<ETHTransactionModel>(
		EtherScanApi.transactions(SharedAddress.getCurrentEthereum(), "$startBlock"),
		"result"
	) { transactions, error ->
		if (transactions != null && error.isNone()) {
			val transactionDao =
				GoldStoneDataBase.database.transactionDao()
			// onConflict InsertAll 利用 RoomDatabase 进行双主键做重复判断 
			// 覆盖或新增到本地 TransactionTable 数据库里
			transactionDao.insertAll(
				transactions.asSequence().map {
					TransactionTable(it)
				}.filterNot {
					it.fromAddress.isEmpty() || it.to.isEmpty()
				}.toList()
			)
			// `Copy` 账单中的 `ETH` 转账账单并标记为 `Fee`
			transactions.asSequence().filter {
				!CryptoUtils.isERC20Transfer(it.input)
			}.map {
				TransactionTable(it).apply { isFee = true }
			}.filterNot {
				it.isReceive
			}.toList().let {
				transactionDao.insertAll(it)
			}
			callback(error)
		} else callback(error)
	}
}

@WorkerThread
fun checkAddressNameInContacts(
	transactions: List<TransactionListModel>,
	callback: () -> Unit
) {
	val contacts =
		ContactTable.dao.getAllContacts()
	transactions.forEach { transaction ->
		transaction.addressName = contacts.getContactName(transaction.addressName)
	}
	callback()
}

