package io.goldstone.blockchain.common.language

/**
 * @date 2018/8/8 2:11 AM
 * @author KaySaith
 */

object DappCenterText {
	@JvmField
	val dappSearchPlaceholderText = when (currentLanguage) {
		HoneyLanguage.English.code->"Search Dapp by keywords or URL"
		HoneyLanguage.Chinese.code->"搜索Dapp关键字或网址"
		HoneyLanguage.Japanese.code->"DappキーワードまたはURLを検索する"
		HoneyLanguage.Korean.code->"Dapp 키워드 또는 URL 검색"
		HoneyLanguage.Russian.code->"Поиск Dapp ключевых слов или URL-адресов"
		HoneyLanguage.TraditionalChinese.code->"搜索Dapp關鍵字或網址"
		else -> ""
	}
	@JvmField
	val recommendDapp = when (currentLanguage) {
		HoneyLanguage.English.code->"RECOMMENDED DAPPs"
		HoneyLanguage.Chinese.code->"推荐应用"
		HoneyLanguage.Japanese.code->"推奨アプリケーション"
		HoneyLanguage.Korean.code->"추천 응용 프로그램"
		HoneyLanguage.Russian.code->"Рекомендуемое приложение"
		HoneyLanguage.TraditionalChinese.code->"推薦應用"
		else -> ""
	}
	@JvmField
	val recentDap = when (currentLanguage) {
		HoneyLanguage.English.code->"Recently Used"
		HoneyLanguage.Chinese.code->"最近使用"
		HoneyLanguage.Japanese.code->"最近使用された"
		HoneyLanguage.Korean.code->"최근에 사용 된"
		HoneyLanguage.Russian.code->"Недавно использованный"
		HoneyLanguage.TraditionalChinese.code->"最近使用"
		else -> ""
	}
	@JvmField
	val newDapp = when (currentLanguage) {
		HoneyLanguage.English.code->"New Arrival"
		HoneyLanguage.Chinese.code->"最新上架"
		HoneyLanguage.Japanese.code->"新着 Dapps"
		HoneyLanguage.Korean.code->"최신 선반"
		HoneyLanguage.Russian.code->"Последние полки"
		HoneyLanguage.TraditionalChinese.code->"最新上架"
		else -> ""
	}
	@JvmField
	val thirdPartDappAlertTitle = when (currentLanguage) {
		HoneyLanguage.English.code->"Third-party application risk tips"
		HoneyLanguage.Chinese.code->"第三方应用风险提示"
		HoneyLanguage.Japanese.code->"リスクの警告"
		HoneyLanguage.Korean.code->"제 3 자 애플리케이션 위험 팁"
		HoneyLanguage.Russian.code->"Стороннее приложение"
		HoneyLanguage.TraditionalChinese.code->"第三方應用風險提示"
		else -> ""
	}
	@JvmField
	val thirdPartDappAlertDescription = when (currentLanguage) {
		HoneyLanguage.English.code->"This is a DAPP provided by a third party. Your use of the third party Dapp will be subject to the User Agreement and Privacy Policy of the third party Dapp."
		HoneyLanguage.Chinese.code->"这是由第三方提供的的DAPP，您在第三方Dapp上的使用行为将适用该第三方Dapp的《用户协议》和《隐私条款》。"
		HoneyLanguage.Japanese.code->"これは第三者によって提供されるDAPPです。第三者Dappの使用は、第三者Dappのユーザー同意書およびプライバシーポリシーの対象となります。"
		HoneyLanguage.Korean.code->"이것은 제 3 자에 의해 제공되는 DAPP입니다. 귀하의 제 3의 Dapp 사용은 제 3의 Dapp의 사용자 계약 및 개인 정보 보호 정책의 적용을받습니다."
		HoneyLanguage.Russian.code->"Это DAPP, предоставленный третьей стороной. Использование Dapp третьей стороной будет регулироваться Соглашением с пользователем и Политикой конфиденциальности Dapp третьей стороны."
		HoneyLanguage.TraditionalChinese.code->"這是由第三方提供的的DAPP，您在第三方Dapp上的使用行為將適用該第三方Dapp的《用戶協議》和《隱私條款》。"
		else -> ""
	}
}