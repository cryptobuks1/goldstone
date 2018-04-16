@file:Suppress("INACCESSIBLE_TYPE")

package io.goldstone.blockchain.crypto

import android.content.Context
import com.blinnnk.extension.isTrue
import io.goldstone.blockchain.common.utils.alert
import io.goldstone.blockchain.module.home.wallet.walletdetail.view.DecryptKeystore
import org.ethereum.geth.Account
import org.ethereum.geth.Geth
import org.ethereum.geth.KeyStore
import org.kethereum.bip39.Mnemonic
import org.kethereum.crypto.Keys
import org.kethereum.crypto.publicKeyFromPrivate
import org.walleth.khex.hexToByteArray
import org.web3j.crypto.Wallet
import java.io.File

/**
 * @date 29/03/2018 4:25 PM
 * @author KaySaith
 */

fun Context.generateWallet(
  password: String,
  holdAddress: (mnemonicCode: String, address: String) -> Unit
) {
  val keystoreFile by lazy { File(filesDir!!, "keystore") }
  val path = "m/44'/60'/0'/0/0"
  /** Generate Mnemonic */
  val mnemonicCode = Mnemonic.generateMnemonic()
  /** Generate HD Wallet */
  val masterWallet = Mnemonic.mnemonicToKey(mnemonicCode, path)
  /** Generate Keystore */
  val keyStore = KeyStore(keystoreFile.absolutePath, Geth.LightScryptN, Geth.LightScryptP)
  /** Generate Keys */
  val masterKey = masterWallet.getKeyPair()
  /** Get Public Key and Private Key*/
  val publicKey = Keys.getAddress(masterKey.publicKey)
  val address = "0x" + publicKey.toLowerCase()
  holdAddress(mnemonicCode, address)
  /** Import Private Key to Keystore */
  keyStore.importECDSAKey(masterKey.privateKey.toString(16).hexToByteArray(), password)
}

fun Context.getWalletByMnemonic(mnemonicCode: String, password: String, hold: (address: String?) -> Unit) {
  val keystoreFile by lazy { File(filesDir!!, "keystore") }
  val path = "m/44'/60'/0'/0/0"
  /** Generate HD Wallet */
  val masterWallet = Mnemonic.mnemonicToKey(mnemonicCode, path)
  /** Generate Keystore */
  val keyStore = KeyStore(keystoreFile.absolutePath, Geth.LightScryptN, Geth.LightScryptP)
  /** Generate Keys */
  val masterKey = masterWallet.getKeyPair()
  /** Get Public Key and Private Key*/
  val publicKey = Keys.getAddress(masterKey.publicKey)
  val address = "0x" + publicKey.toLowerCase()
  /** Import Private Key to Keystore */
  try {
    keyStore.importECDSAKey(masterKey.privateKey.toString(16).hexToByteArray(), password)
  } catch (error: Exception) {
    println(error)
  }
  hold(address)
}

fun Context.getWalletByPrivateKey(
  privateKey: String,
  password: String,
  hold: (address: String?) -> Unit
) {
  val keystoreFile by lazy { File(filesDir!!, "keystore") }
  /** Generate Keystore */
  val keyStore = KeyStore(keystoreFile.absolutePath, Geth.LightScryptN, Geth.LightScryptP)

  /** Convert PrivateKey To BigInteger */
  val currentPrivateKey = privateKey.toBigInteger(16)
  /** Get Public Key and Private Key*/
  val publicKey = Keys.getAddress(publicKeyFromPrivate(currentPrivateKey))
  val address = "0x" + publicKey.toLowerCase()
  /** Format PrivateKey */
  val keyString =
    when {
      privateKey.substring(0, 1) == "0" -> "0" + currentPrivateKey.toString(16)
      privateKey.length == 63 -> "0" + currentPrivateKey.toString(16)
      else -> currentPrivateKey.toString(16)
    }
  /** Import Private Key to Keystore */
  try {
    keyStore.importECDSAKey(keyString.hexToByteArray(), password)
  } catch (error: Exception) {
    println(error)
  }
  hold(address)
}

fun Context.getCurrentAccount(walletAddress: String, hold: (currentAccount: Account, keystore: KeyStore) -> Unit) {
  val keystoreFile by lazy { File(filesDir!!, "keystore") }
  val keyStore = KeyStore(keystoreFile.absolutePath, Geth.LightScryptN, Geth.LightScryptP)
  (0 until keyStore.accounts.size()).forEach { index ->
    keyStore.accounts.get(index).address.hex.let {
      it.equals(walletAddress, true).isTrue {
        hold(keyStore.accounts.get(index), keyStore)
      }
    }
  }
}

fun Context.getKeystoreFile(walletAddress: String, password: String, hold: (String) -> Unit) {
  val keystoreFile by lazy { File(filesDir!!, "keystore") }
  val keyStore = KeyStore(keystoreFile.absolutePath, Geth.LightScryptN, Geth.LightScryptP)
  (0 until keyStore.accounts.size()).forEach { index ->
    keyStore.accounts.get(index).address.hex.let {
      it.equals(walletAddress, true).isTrue {
        try {
          hold(String(keyStore.exportKey(keyStore.accounts.get(index), password, password)))
        } catch (error: Exception) {
          println(error)
        }
      }
    }
  }
}

fun Context.getPrivateKey(walletAddress: String, password: String, hold: (String) -> Unit) {
  getKeystoreFile(walletAddress, password) {
    try {
      Wallet.decrypt(password, DecryptKeystore.GenerateFile(it.convertKeystoreToModel())).let {
        hold(it.privateKey.toString(16))
      }
    } catch (error: Exception) {
      println(error)
    }
  }
}

fun Context.deleteAccount(walletAddress: String, password: String, callback: (correctPassword: Boolean) -> Unit) {
  val keystoreFile by lazy { File(filesDir!!, "keystore") }
  val keyStore = KeyStore(keystoreFile.absolutePath, Geth.LightScryptN, Geth.LightScryptP)
  // If there is't account found then return
  if (keyStore.accounts.size() == 0L) return
  (0 until keyStore.accounts.size()).forEach { index ->
    keyStore.accounts.get(index).address.hex.let {
      it.equals(walletAddress, true).isTrue {
        try {
          keyStore.deleteAccount(keyStore.accounts.get(index), password)
          callback(true)
        } catch (error: Exception) {
          alert("Wrong Password")
          callback(false)
          println(error)
        }
      }
    }
  }
}

fun Context.updatePassword(walletAddress: String, oldPassword: String, newPassword: String, callback: () -> Unit) {
  getPrivateKey(walletAddress, oldPassword) { privateKey ->
    deleteAccount(walletAddress, oldPassword) {
      getWalletByPrivateKey(privateKey, newPassword) {
        callback()
      }
    }
  }
}