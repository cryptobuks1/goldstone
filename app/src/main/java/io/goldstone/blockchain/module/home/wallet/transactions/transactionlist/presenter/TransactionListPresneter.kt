package io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.presenter

import android.os.Bundle
import android.util.Log
import com.blinnnk.extension.*
import com.blinnnk.util.getParentFragment
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.utils.getMainActivity
import io.goldstone.blockchain.common.utils.toArrayList
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.TransactionText
import io.goldstone.blockchain.crypto.CryptoSymbol
import io.goldstone.blockchain.crypto.CryptoUtils
import io.goldstone.blockchain.crypto.GoldStoneEthCall
import io.goldstone.blockchain.crypto.toEthCount
import io.goldstone.blockchain.kernel.commonmodel.TransactionTable
import io.goldstone.blockchain.kernel.commonmodel.TransactionTable.Companion.getTransactionListModelsByAddress
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import io.goldstone.blockchain.kernel.network.GoldStoneAPI
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blockchain.module.home.home.view.MainActivity
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable
import io.goldstone.blockchain.module.home.wallet.transactions.transaction.view.TransactionFragment
import io.goldstone.blockchain.module.home.wallet.transactions.transactiondetail.view.TransactionDetailFragment
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.model.TransactionListModel
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.view.TransactionListFragment
import org.jetbrains.anko.doAsync

/**
 * @date 24/03/2018 2:12 PM
 * @author KaySaith
 */

// 从数据库去除交易记录存放到内存里, 提升用户体验.
var localTransactions: ArrayList<TransactionListModel>? = null

class TransactionListPresenter(
  override val fragment: TransactionListFragment
) : BaseRecyclerPresenter<TransactionListFragment, TransactionListModel>() {

  override fun updateData() {
    fragment.apply {
      localTransactions.isNotNull {
        asyncData = localTransactions
        // 更新显示数据后, 异步继续更新新的数据.并动态刷新到界面
        getMainActivity()?.updateTransactionInAsync(localTransactions!!)
      } otherwise {
        getTransactionListModelsByAddress(WalletTable.current.address) { localData ->
          localData.isNotEmpty().isTrue {
            asyncData = localData
            localTransactions = localData
          } otherwise {
            getMainActivity()?.getTransactionDataFromEtherScan {
              asyncData = it
              localTransactions = it
            }
          }
        }
      }
    }
  }

  fun showTransactionDetail(model: TransactionListModel?) {
    fragment.getParentFragment<TransactionFragment>()?.apply {
      Bundle().apply {
        putSerializable(ArgumentKey.transactionFromList, model)
        presenter.showTargetFragment<TransactionDetailFragment>(TransactionText.detail, TransactionText.transaction, this)
      }
    }
  }

  private fun MainActivity.updateTransactionInAsync(localData: ArrayList<TransactionListModel>) {
    // 本地可能存在 `pending` 状态的账目, 所以获取最近的 `blockNumber` 先剥离掉 `pending` 的类型
    val lastBlockNumber = localData.first { it.blockNumber.isNotEmpty() }.blockNumber + 1
    // 本地若有数据获取本地最近一条数据的 `BlockNumber` 作为 StartBlock 尝试拉取最新的数据
    getMainActivity()?.getTransactionDataFromEtherScan(lastBlockNumber) { newData ->
      // 如果梅拉去到直接更新本地数据
      doAsync {
        // 拉取到新数据后检查是否包含本地已有的部分, 这种该情况会出现在, 本地转账后插入临时数据的条目。
        newData.forEachOrEnd { item, isEnd ->
          localData.find {
            it.transactionHash == item.transactionHash
          }?.let {
            localData.remove(it)
            TransactionTable.deleteByTaxHash(it.transactionHash)
          }
          if (isEnd) {
            // 数据清理干净后在主线程更新 `UI`
            runOnUiThread {
              // 拉取到后, 把最新获取的数据合并本地数据更新到界面
              localData.addAll(0, newData)
              // 把数据存到内存里面, 下次打开直接使用内存, 不用再度数据库，提升用户体验.
              localTransactions = localData
              fragment.asyncData?.addAll(0, localData)
              fragment.recyclerView.adapter.notifyItemRangeInserted(0, newData.lastIndex)
            }
          }
        }
      }
      Log.d("DEBUG", "updated new transaction data")
    }
  }

  companion object {

    private fun completeTransactionInfo(
      data: ArrayList<TransactionTable>, hold: ArrayList<TransactionTable>.() -> Unit
    ) {
      data.forEachOrEnd { transaction, isEnd ->
        CryptoUtils.isERC20Transfer(transaction) {
          // 解析 `input code` 获取 `ERC20` 接受 `address`, 及接受 `count`
          val transactionInfo = CryptoUtils.loadTransferInfoFromInputData(transaction.input)
          // 首先从本地数据库检索 `contract` 对应的 `symbol`
          DefaultTokenTable.getTokenByContractAddress(transaction.to) { tokenInfo ->
            val count = CryptoUtils.toCountByDecimal(
              transactionInfo?.count.orElse(0.0), tokenInfo?.decimals.orElse(0.0)
            )
            tokenInfo.isNull().isTrue {
              // 如果本地没有检索到 `contract` 对应的 `symbol` 则从链上查询
              GoldStoneEthCall.getTokenSymbol(transaction.to) { tokenSymbol ->
                TransactionTable.updateModelInfoFromChain(
                  transaction,
                  true,
                  tokenSymbol,
                  count.toString(),
                  transactionInfo?.address
                )
                if (isEnd) hold(data)
              }
            } otherwise {
              TransactionTable.updateModelInfoFromChain(
                transaction,
                true,
                tokenInfo!!.symbol,
                count.toString(),
                transactionInfo?.address
              )
              if (isEnd) hold(data)
            }
          }
        }.isFalse {
          TransactionTable.updateModelInfoFromChain(
            transaction,
            false,
            CryptoSymbol.eth,
            transaction.value.toDouble().toEthCount().toString(),
            transaction.to
          )
          if (isEnd) hold(data)
        }
      }
    }

    // 默认拉取全部的 `EtherScan` 的交易数据
    private fun MainActivity.getTransactionDataFromEtherScan(
      startBlock: String = "0", hold: (ArrayList<TransactionListModel>) -> Unit
    ) {
      // Show loading view
      showLoadingView()

      GoldStoneAPI.getDefaultTokens {  }

      // Get transaction data from `etherScan`
      GoldStoneAPI.getTransactionListByAddress(WalletTable.current.address, startBlock) {
        val chainData = this
        if (chainData.isEmpty()) {
          runOnUiThread {
            removeLoadingView()
            // 没有数据返回空数组
            hold(arrayListOf())
          }
          return@getTransactionListByAddress
        }

        // 因为进入这里之前外部已经更新了最近的 `BlockNumber`, 所以这里的数据可以直接理解为最新的本地没有的部分
        filterCompletedData(chainData, hold)
        Log.d("DEBUG", "update the new data from chain")
      }
    }

    fun updateTransactions(
      activity: MainActivity?,
      startBlock: String = "0",
      hold: (ArrayList<TransactionListModel>) -> Unit
    ) {
      activity?.getTransactionDataFromEtherScan(startBlock, hold)
    }

    private fun MainActivity.filterCompletedData(
      data: ArrayList<TransactionTable>, hold: (ArrayList<TransactionListModel>) -> Unit
    ) {
      // 把拉取到的数据加工数据格式并插入本地数据库
      completeTransactionInfo(data) {
        forEachOrEnd { it, isEnd ->
          it.to.isNotEmpty().isTrue {
            GoldStoneDataBase.database.transactionDao().insert(it)
          }
          if (isEnd) {
            val transactions = filter { it.to.isNotEmpty() }.toArrayList()
            runOnUiThread {
              removeLoadingView()
              hold(transactions.map { TransactionListModel(it) }.toArrayList())
            }
          }
        }
      }
    }
  }
}