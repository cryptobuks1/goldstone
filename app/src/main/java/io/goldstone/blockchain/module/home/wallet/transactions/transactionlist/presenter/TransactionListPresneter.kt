package io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.presenter

import android.os.Bundle
import com.blinnnk.extension.*
import com.blinnnk.util.getParentFragment
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.utils.getMainActivity
import io.goldstone.blockchain.common.utils.toArrayList
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.crypto.CryptoUtils
import io.goldstone.blockchain.crypto.GoldStoneEthCall
import io.goldstone.blockchain.kernel.commonmodel.TransactionTable
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import io.goldstone.blockchain.kernel.network.GoldStoneAPI
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blockchain.module.home.home.view.MainActivity
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable
import io.goldstone.blockchain.module.home.wallet.transactions.transaction.view.TransactionFragment
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.model.TransactionListModel
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.view.TransactionListFragment
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread

/**
 * @date 24/03/2018 2:12 PM
 * @author KaySaith
 */

class TransactionListPresenter(
  override val fragment: TransactionListFragment
) : BaseRecyclerPresenter<TransactionListFragment, TransactionListModel>() {

  override fun updateData(asyncData: ArrayList<TransactionListModel>?) {
    TransactionTable.getAllTransactionsByAddress(WalletTable.current.address) {
      it.isEmpty().isTrue {
        getTransactionDataFromEtherScan(fragment.getMainActivity()!!) {
          fragment.asyncData = it
        }
      } otherwise {
        fragment.asyncData = it.map { TransactionListModel(it) }.toArrayList()
        System.out.println("There are local data about transaction")
      }
    }
  }

  fun showTransactionDetail(model: TransactionListModel?) {
    fragment.getParentFragment<TransactionFragment>()?.apply {
      val bundle = Bundle().apply {
        putSerializable(ArgumentKey.transactionDetail, model)
      }
      presenter.showTargetFragment(true, bundle)
    }
  }

  companion object {

    private fun completeTransactionInfo(data: ArrayList<TransactionTable>, hold: ArrayList<TransactionTable>.() -> Unit) {
      data.apply {
        forEachIndexed { index, it ->
          CryptoUtils.isERC20Transfer(it) {
            // 解析 `input code` 获取 `ERC20` 接受 `address`, 及接受 `count`
            val transactionInfo = CryptoUtils.loadTransferInfoFromInputData(it.input)
            // 判断是否是接受交易
            val receiveStatus = WalletTable.current.address == transactionInfo?.address
            // 首先从本地数据库检索 `contract` 对应的 `symbol`
            DefaultTokenTable.getTokenByContractAddress(it.to) { tokenInfo ->
              val count = CryptoUtils.toCountByDecimal(
                transactionInfo?.count.orElse(0.0), tokenInfo?.decimals.orElse(0.0)
              )
              tokenInfo.isNull().isTrue {
                // 如果本地没有检索到 `contract` 对应的 `symbol` 则从链上查询
                GoldStoneEthCall.getTokenSymbol(it.to) { tokenSymbol ->
                  it.apply {
                    isReceive = receiveStatus
                    isERC20 = true
                    symbol = tokenSymbol
                    value = count.toString()
                    tokenReceiveAddress = transactionInfo?.address
                    recordOwnerAddress = WalletTable.current.address
                  }

                  if (index == lastIndex) {
                    hold(this)
                  }

                }
              } otherwise {
                it.apply {
                  isReceive = receiveStatus
                  isERC20 = true
                  symbol = tokenInfo!!.symbol
                  value = count.toString()
                  tokenReceiveAddress = transactionInfo?.address
                  recordOwnerAddress = WalletTable.current.address
                }

                if (index == lastIndex) {
                  hold(this)
                }
              }
            }
          }.isFalse {
            it.apply {
              isReceive = WalletTable.current.address == it.to
              symbol = "ETH"
              value = CryptoUtils.toCountByDecimal(it.value.toDouble(), 18.0).toString()
              recordOwnerAddress = WalletTable.current.address
            }
            if (index == lastIndex) {
              hold(this)
            }
          }
        }
      }
    }

    fun getTransactionDataFromEtherScan(activity: MainActivity, hold: (ArrayList<TransactionListModel>) -> Unit) {
      // Show loading view
      activity.showLoadingView()
      // Get transaction data from `etherScan`
      GoldStoneAPI.getTransactionListByAddress(WalletTable.current.address) {
        if (isEmpty()) {
          GoldStoneAPI.context.runOnUiThread {
            // There isn't data in blockchain
            getMainActivity()?.removeLoadingView()
            // Show empty view in recycler view
          }
          return@getTransactionListByAddress
        }
        val data = this
        doAsync {
          completeTransactionInfo(data) {
            forEachIndexed { index, it ->
              it.to.isNotEmpty().isTrue { GoldStoneDataBase.database.transactionDao().insert(it) }
              if (index == lastIndex) {
                val transactions = filter { it.to.isNotEmpty() }.toArrayList()
                GoldStoneAPI.context.runOnUiThread {
                  activity.removeLoadingView()
                  hold(transactions.map { TransactionListModel(it) }.toArrayList())
                }
              }
            }
          }
        }
      }
    }
  }

}