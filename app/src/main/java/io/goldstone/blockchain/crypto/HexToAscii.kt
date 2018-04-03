package io.goldstone.blockchain.crypto

/**
 * @date 31/03/2018 2:47 PM
 * @author KaySaith
 */

fun String.toAscii(): String {
  /** 去掉前两个字母 `0x` */
  var hex = if (substring(0, 2) == "0x") substring(2, length) else this
  var ascii = ""
  var str: String

  // Convert hex string to "even" length
  val rmd: Int
  val length: Int = hex.length
  rmd = length % 2
  if (rmd == 1)
    hex = "0" + hex

  // split into two characters
  var i = 0
  while (i < hex.length - 1) {

    //split the hex into pairs
    val pair = hex.substring(i, i + 2)
    //convert hex to decimal
    val dec = Integer.parseInt(pair, 16)
    str = checkCode(dec)
    ascii += str
    i += 2
  }
  return ascii
}

private fun checkCode(dec: Int): String {
  var str: String

  //convert the decimal to character
  str = Character.toString(dec.toChar())

  if (dec < 32 || dec in 127 .. 160) str = ""
  return str.trim()
}

/**
 * `hash` 值转换为 `Decimal`
 */

fun String.hexToDecimal():  Double {
  // 以太坊的地址都是含有 `0x` 开头, 这里首先去掉 `0x`
  var hexNum = this.substring(2, length)
  val digits = "0123456789ABCDEF"
  hexNum = hexNum.toUpperCase()
  var value  = 0.0
  (0 until hexNum.length)
    .map { hexNum[it] }
    .map { digits.indexOf(it.toString()) }
    .forEachIndexed { index, it ->
      value += (Math.pow(16.0, hexNum.length - (index + 1.0)) * it).toLong()
    }
  return value
}