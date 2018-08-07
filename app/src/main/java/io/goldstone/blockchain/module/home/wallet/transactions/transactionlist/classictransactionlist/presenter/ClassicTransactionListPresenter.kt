package io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.classictransactionlist.presenter

import com.blinnnk.util.getParentFragment
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.utils.LogUtil
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.language.LoadingText
import io.goldstone.blockchain.kernel.commonmodel.TransactionTable
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import io.goldstone.blockchain.kernel.network.GoldStoneAPI
import io.goldstone.blockchain.module.home.wallet.transactions.transaction.view.TransactionFragment
import io.goldstone.blockchain.module.home.wallet.transactions.transactiondetail.model.ETCTransactionModel
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.classictransactionlist.view.ClassicTransactionListAdapter
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.classictransactionlist.view.ClassicTransactionListFragment
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.model.TransactionListModel
import io.goldstone.blockchain.module.home.wallet.transactions.transactionlist.ethereumtransactionlist.presenter.TransactionListPresenter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread

/**
 * @date 2018/6/25 12:08 PM
 * @author KaySaith
 */
class ClassicTransactionListPresenter(
	override val fragment: ClassicTransactionListFragment
) : BaseRecyclerPresenter<ClassicTransactionListFragment, TransactionListModel>() {
	
	override fun updateData() {
		fragment.asyncData = arrayListOf()
	}
	
	override fun onFragmentViewCreated() {
		super.onFragmentViewCreated()
		fragment.getParentFragment<TransactionFragment>()?.apply {
			isETCListShown = Runnable {
				fragment.showChainData()
			}
		}
	}
	
	private var hasUpdateChainData: Boolean = false
	private fun ClassicTransactionListFragment.showChainData() {
		showLoadingView(LoadingText.transactionData)
		// 首先显示本地数据
		getETCTransactionsFromDatabase {
			diffAndUpdateSingleCellAdapterData<ClassicTransactionListAdapter>(it)
			if (!hasUpdateChainData) {
				// 异步查询网络数据并决定是否更新
				getETCTransactionsFromChain(it) {
					removeLoadingView()
					showChainData()
					hasUpdateChainData = true
				}
			} else {
				removeLoadingView()
			}
		}
	}
	
	companion object {
		
		fun getETCTransactionsFromDatabase(
			hold: (ArrayList<TransactionListModel>) -> Unit = {}
		) {
			TransactionTable.getETCTransactionsByAddress(
				Config.getCurrentETCAddress()
			) {
				TransactionListPresenter.checkAddressNameInContacts(it) {
					hold(it)
				}
			}
		}
		
		fun getETCTransactionsFromChain(
			localData: ArrayList<TransactionListModel>,
			callback: () -> Unit
		) {
			doAsync {
				val blockNumber = localData.maxBy {
					it.blockNumber
				}?.blockNumber ?: "0"
				loadDataFromChain(blockNumber, localData, callback)
			}
		}
		
		private fun loadDataFromChain(
			blockNumber: String,
			localData: ArrayList<TransactionListModel>,
			callback: () -> Unit
		) {
			GoldStoneAPI.getETCTransactions(
				Config.getETCCurrentChain(),
				Config.getCurrentETCAddress(),
				blockNumber,
				{
					LogUtil.error("loadDataFromChain", it)
				}
			) { newData ->
				// 插入数据库的抽象方法
				fun List<ETCTransactionModel>.insertDataToDataBase() {
					// 生成最终的数据格式
					map {
						// 加工数据并存如数据库
						TransactionTable(it).apply {
							GoldStoneDataBase.database.transactionDao().insert(this)
						}
					}
				}
				
				if (newData.isNotEmpty()) {
					val finalNewData = newData.filterNot { new ->
						// 和本地数据去重处理
						localData.any {
							it.transactionHash.equals(new.hash, true)
						}
					}
					finalNewData.insertDataToDataBase()
					// Copy 出燃气费的部分
					val feeData = finalNewData.filter {
						it.from.equals(Config.getCurrentETCAddress(), true)
					}.apply {
						forEach { it.isFee = true }
					}
					
					feeData.insertDataToDataBase()
					
					GoldStoneAPI.context.runOnUiThread {
						callback()
					}
				} else {
					// 数据为空返回
					GoldStoneAPI.context.runOnUiThread {
						callback()
					}
				}
			}
		}
	}
}