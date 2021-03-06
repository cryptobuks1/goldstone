package io.goldstone.blinnnk.module.common.walletimport.mnemonicimport.presenter

import android.support.annotation.WorkerThread
import com.blinnnk.extension.*
import io.goldstone.blinnnk.common.base.basefragment.BasePresenter
import io.goldstone.blinnnk.common.error.AccountError
import io.goldstone.blinnnk.common.error.GoldStoneError
import io.goldstone.blinnnk.crypto.bip39.Mnemonic
import io.goldstone.blinnnk.crypto.multichain.ChainPath
import io.goldstone.blinnnk.crypto.multichain.GenerateMultiChainWallet
import io.goldstone.blinnnk.crypto.utils.JavaKeystoreUtil
import io.goldstone.blinnnk.crypto.utils.KeystoreInfo
import io.goldstone.blinnnk.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blinnnk.module.common.walletgeneration.createwallet.presenter.CreateWalletPresenter
import io.goldstone.blinnnk.module.common.walletimport.mnemonicimport.view.MnemonicImportDetailFragment
import io.goldstone.blinnnk.module.common.walletimport.walletimport.presenter.WalletImportPresenter
import io.goldstone.blinnnk.module.common.walletimport.walletimport.view.WalletImportFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @date 23/03/2018 1:46 AM
 * @author KaySaith
 */
class MnemonicImportDetailPresenter(
	override val fragment: MnemonicImportDetailFragment
) : BasePresenter<MnemonicImportDetailFragment>() {

	fun importWalletByMnemonic(
		multiChainPath: ChainPath,
		mnemonic: String,
		password: String,
		repeatPassword: String,
		passwordHint: String,
		isAgree: Boolean,
		name: String,
		callback: (GoldStoneError) -> Unit
	) = GlobalScope.launch(Dispatchers.Default) {
		if (mnemonic.isEmpty()) {
			callback(AccountError.InvalidMnemonic)
		} else if (!isValidPath(multiChainPath).isNone()) callback(AccountError.InvalidBip44Path)
		else CreateWalletPresenter.checkInputValue(
			name,
			password,
			repeatPassword,
			isAgree
		) { passwordValue, walletName, error ->
			if (error.hasError()) callback(error)
			else {
				val mnemonicContent =
					mnemonic.replaceWithPattern().replace("\n", " ").removeStartAndEndValue(" ")
				Mnemonic.validateMnemonic(mnemonicContent) isFalse {
					callback(AccountError.InvalidMnemonic)
				} otherwise {
					importWallet(
						mnemonicContent,
						multiChainPath,
						passwordValue!!,
						walletName!!,
						passwordHint,
						callback
					)
				}
			}
		}
	}

	private fun isValidPath(multiChainPath: ChainPath): AccountError {
		return if (multiChainPath.ethPath.isNotEmpty() && !isValidBIP44Path(multiChainPath.ethPath)) {
			AccountError.InvalidBip44Path
		} else if (multiChainPath.btcPath.isNotEmpty() && !isValidBIP44Path(multiChainPath.btcPath)) {
			AccountError.InvalidBip44Path
		} else if (multiChainPath.testPath.isNotEmpty() && !isValidBIP44Path(multiChainPath.testPath)) {
			AccountError.InvalidBip44Path
		} else if (multiChainPath.ltcPath.isNotEmpty() && !isValidBIP44Path(multiChainPath.ltcPath)) {
			AccountError.InvalidBip44Path
		} else if (multiChainPath.etcPath.isNotEmpty() && !isValidBIP44Path(multiChainPath.etcPath)) {
			AccountError.InvalidBip44Path
		} else AccountError.None
	}

	@WorkerThread
	private fun importWallet(
		mnemonic: String,
		multiChainPath: ChainPath,
		password: String,
		name: String,
		hint: String? = null,
		callback: (GoldStoneError) -> Unit
	) {
		// 加密 `Mnemonic` 后存入数据库, 用于用户创建子账号的时候使用
		val encryptMnemonic = JavaKeystoreUtil(KeystoreInfo.isMnemonic()).encryptData(mnemonic)
		val allWallets = WalletTable.dao.getAllWallets()
		val isExistent = allWallets.any {
			try {
				JavaKeystoreUtil(KeystoreInfo.isMnemonic()).decryptData(it.encryptMnemonic.orEmpty()).equals(mnemonic, true)
			} catch (error: Exception) {
				println("decrypt Data: ${error.message}")
				false
			}
		}
		if (isExistent) callback(AccountError.ExistAddress)
		else GenerateMultiChainWallet.import(
			fragment.context!!,
			mnemonic,
			password,
			multiChainPath
		) { multiChainAddresses ->
			// 如果地址已经存在则会返回空的多链地址 `Model`
			WalletImportPresenter.insertWalletToDatabase(
				multiChainAddresses,
				name,
				encryptMnemonic,
				multiChainPath,
				hint
			) { _, error ->
				callback(error)
			}
		}
	}

	override fun onFragmentShowFromHidden() {
		super.onFragmentShowFromHidden()
		setRootChildFragmentBackEvent<WalletImportFragment>(fragment)
		// 深度回退站恢复
		fragment.getParentFragment<WalletImportFragment> {
			showBackButton(true) {
				presenter.popFragmentFrom<MnemonicImportDetailFragment>()
			}
		}
	}

	private fun isValidBIP44Path(path: String): Boolean {
		// 最小 3 位数字
		if (path.length < 3) return false
		// 格式化无用信息
		val formatPath = path.replace("\n", "").replace(" ", "")
		// 校验前两位强制内容
		return if (formatPath.substring(0, 2).equals("m/", true)) {
			val pathNumber = formatPath.substring(1, formatPath.length).replace("/", "").replace("'", "")
			// 检验剩余部分是否全部为数字
			!pathNumber.toIntOrNull().isNull()
		} else {
			false
		}
	}
}