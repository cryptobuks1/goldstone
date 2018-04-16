package io.goldstone.blockchain.module.common.walletimport.keystoreimport.presenter

import android.util.Log
import android.widget.EditText
import com.blinnnk.extension.isTrue
import com.blinnnk.extension.otherwise
import io.goldstone.blockchain.common.base.basefragment.BasePresenter
import io.goldstone.blockchain.common.utils.alert
import io.goldstone.blockchain.crypto.convertKeystoreToModel
import io.goldstone.blockchain.module.common.walletimport.keystoreimport.view.KeystoreImportFragment
import io.goldstone.blockchain.module.common.walletimport.privatekeyimport.presenter.PrivateKeyImportPresenter
import io.goldstone.blockchain.module.home.wallet.walletdetail.view.DecryptKeystore
import org.web3j.crypto.Wallet

/**
 * @date 23/03/2018 1:49 AM
 * @author KaySaith
 */

class KeystoreImportPresenter(
  override val fragment: KeystoreImportFragment
  ) : BasePresenter<KeystoreImportFragment>() {

  fun importKeystoreWallet(keystore: String, password: EditText, nameInput: EditText, isAgree: Boolean) {
    isAgree.isTrue {
      try {
        Wallet.decrypt(
          password.text.toString(),
          DecryptKeystore.GenerateFile(keystore.convertKeystoreToModel())
        )?.let {
          PrivateKeyImportPresenter.importWallet(
            it.privateKey.toString(16),
            password.text.toString(),
            nameInput.text.toString(),
            fragment
          )
        }
      } catch (error: Exception) {
        fragment.context?.alert("Error, Please check your keystore format or password")
        Log.e("ERROR", "import keystore")
      }
    } otherwise {
      fragment.context?.alert("You must agree terms")
    }

  }
}