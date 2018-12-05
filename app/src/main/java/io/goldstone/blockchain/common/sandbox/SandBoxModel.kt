package io.goldstone.blockchain.common.sandbox

import java.io.Serializable

/**
 * @date: 2018-12-05.
 * @author: yangLiHai
 * @description:
 */
class SandBoxModel(
	var language: String,
	var tokens: String
) : Serializable {
	constructor() : this("", "")
}