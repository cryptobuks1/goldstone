package io.goldstone.blinnnk.common.error

import io.goldstone.blinnnk.common.language.ErrorText
import io.goldstone.blinnnk.common.value.ErrorTag


/**
 * @author KaySaith
 * @date  2018/09/21
 */
open class GoldStoneError(override val message: String, val tag: String = "GoldStoneError") : Throwable(message) {
	fun isNone(): Boolean = message.equals(GoldStoneError.None.message, true)
	fun hasError(): Boolean = !message.equals(GoldStoneError.None.message, true)
	fun isEmptyResult(): Boolean = message.equals(RequestError.EmptyResut.message, true)
	fun isChainError(): Boolean = message.contains(ErrorTag.chain, true)
	fun isIgnoreError(): Boolean = message.contains(AccountError.BackUpMnemonic.message, true)

	companion object {
		@JvmStatic
		val None = GoldStoneError(ErrorText.none)
	}
}