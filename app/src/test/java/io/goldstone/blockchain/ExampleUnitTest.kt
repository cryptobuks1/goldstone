package io.goldstone.blockchain

import com.blinnnk.extension.toList
import com.google.gson.JsonArray
import io.goldstone.blockchain.common.utils.LogUtil
import io.goldstone.blockchain.crypto.bitcoin.BTCUtils
import io.goldstone.blockchain.crypto.eos.accountregister.EOSActor
import io.goldstone.blockchain.crypto.eos.netcpumodel.BandWidthModel
import io.goldstone.blockchain.crypto.eos.transaction.EOSAuthorization
import io.goldstone.blockchain.crypto.multichain.CryptoValue
import io.goldstone.blockchain.module.common.tokendetail.eosresourcetrading.common.basetradingfragment.view.StakeType
import org.json.JSONArray
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

@Suppress("DEPRECATION")
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

	@Test
	fun mergeListAndDistinct() {
		val list1 = listOf(1, 2, 3, 4, 5, 6)
		val list2 = listOf(10, 20, 30, 4, 5, 6)
		LogUtil.debug("mergeListAndDistinct", "${list1.asSequence().plus(list2).distinct().toList()}")
	}

	@Test
	fun generateObject() {
		val expectResult = "{\"account\":\"eosio\",\"name\":\"delegatebw\",\"authorization\":[{\"actor\":\"hello\",\"permission\":\"active\"}],\"data\":{\"from\":\"hello\",\"receiver\":\"love\",\"stake_net_quantity\":\"1.0000 EOS\",\"stake_cpu_quantity\":\"1.0000 EOS\"},\"hex_data\":\"\"}"
		// Test Everything
		BandWidthModel(
			listOf(EOSAuthorization("hello", EOSActor.Active)),
			"hello",
			"love",
			BigInteger.valueOf(10000L),
			BigInteger.valueOf(10000L),
			StakeType.Refund,
			true
		).let {
			Assert.assertTrue("get wrong band width object", it.createObject() == expectResult)
		}
	}

	@Test
	fun compare() {
		val list1 = listOf(Pair(1, 2), Pair(1, 3))
		val list2 = listOf(Pair(1, 2), Pair(1, 1))
		LogUtil.debug("compare", list1.containsAll(list2).toString())
		LogUtil.debug("compare", (list1.asSequence().plus(list2).distinct().toList() - list1).toString())
	}

	@Test
	fun bigNumber() {
		println(BigDecimal.valueOf(10).pow(18).add(BigDecimal.ZERO))
	}

	@Test
	fun validBTCAddressLength() {
		println(CryptoValue.isBitcoinAddressLength(" mgqkf2Y6YaiLzQckRWuvcbGXUh2ys2dbbN"))
		println(BTCUtils.isValidTestnetAddress("mgqkf2Y6YaiLzQckRWuvcbGXUh2ys2dbbN"))
	}

	@Test
	fun stringArrayConverter() {
		// expect result hello||hey||
		println(listOf("hello", "hey").map { "$it||" }.joinToString("") { it })
	}
}



