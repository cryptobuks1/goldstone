package io.goldstone.blinnnk.crypto.eos.base

/**
 * @author KaySaith
 * @date 2018/09/05
 */

interface EOSModel {
	fun createObject(): String
	fun serialize(): String
}