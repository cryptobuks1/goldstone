package io.goldstone.blockchain.kernel.commonmodel

import android.arch.persistence.room.*
import com.blinnnk.extension.isFalse
import com.blinnnk.extension.isNull
import com.blinnnk.extension.isTrue
import com.blinnnk.util.coroutinesTask
import io.goldstone.blockchain.common.utils.toArrayList
import io.goldstone.blockchain.crypto.GoldStoneEthCall
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase

/**
 * @date 01/04/2018 12:38 AM
 * @author KaySaith
 */

@Entity(tableName = "myTokens")
data class MyTokenTable(
  @PrimaryKey(autoGenerate = true)
  var id: Int,
  var ownerAddress: String,
  var symbol: String,
  var balance: Double
) {
  companion object {

    fun insert(model: MyTokenTable) {
      GoldStoneDataBase.database.myTokenDao().insert(model)
    }

    fun getTokensWith(walletAddress: String, callback: (ArrayList<MyTokenTable>) -> Unit = {}) {
      coroutinesTask({
        GoldStoneDataBase.database.myTokenDao().getTokensBy(walletAddress)
      }) {
        callback(it.toArrayList())
      }
    }

    fun deleteBySymbol(symbol: String, callback: () -> Unit = {}) {
      coroutinesTask({
        GoldStoneDataBase.database.myTokenDao().apply {
          getTokenBySymbol(symbol).let { it.isNull().isFalse { delete(it) } }
        }
      }) {
        callback()
      }
    }

    fun insertBySymbol(symbol: String, ownerAddress: String, callback: () -> Unit = {}) {
      coroutinesTask({
        GoldStoneDataBase.database.apply {
          // 安全判断, 如果钱包里已经有这个 `Symbol` 则不添加
          myTokenDao().getTokenBySymbol(symbol).let {
            it.isNull().isTrue {
              // 获取 `Symbol` 的 `ContractAddress`
              val symbolToken = defaultTokenDao().getTokenBySymbol(symbol)
              // 获取选中的 `Symbol` 的 `Token` 对应 `WalletAddress` 的 `Balance`
              GoldStoneEthCall.getTokenBalanceWithContract(symbolToken.contract, ownerAddress) {
                insert(MyTokenTable(0, ownerAddress, symbol, it))
              }
            }
          }
        }
      }) {
        callback()
      }
    }

  }
}

@Dao
interface MyTokenDao {
  @Query("SELECT * FROM myTokens WHERE ownerAddress LIKE :walletAddress")
  fun getTokensBy(walletAddress: String): List<MyTokenTable>

  @Query("SELECT * FROM myTokens WHERE symbol LIKE :symbol")
  fun getTokenBySymbol(symbol: String): MyTokenTable

  @Insert
  fun insert(token: MyTokenTable)

  @Update
  fun update(token: MyTokenTable)

  @Delete
  fun delete(token: MyTokenTable)
}