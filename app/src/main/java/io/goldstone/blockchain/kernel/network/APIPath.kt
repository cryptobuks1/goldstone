package io.goldstone.blockchain.kernel.network

import com.blinnnk.extension.getRandom
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.value.WebUrl
import io.goldstone.blockchain.crypto.ethereum.SolidityCode
import io.goldstone.blockchain.crypto.multichain.ChainID
import io.goldstone.blockchain.crypto.utils.toAddressCode

/**
 * @date 31/03/2018 8:09 PM
 * @author KaySaith
 */
object APIPath {

	/** GoldStone Basic Api Address */
	var currentUrl = WebUrl.normalServer

	fun updateServerUrl(newUrl: String) {
		currentUrl = newUrl
	}

	const val serverStatus = "https://gs.blinnnk.com/index/serverStatus"
	val getCurrencyRate: (header: String) -> String = { "$it/index/exchangeRate?currency=" }
	val registerDevice: (header: String) -> String = { "$it/account/registerDevice" }
	val updateAddresses: (header: String) -> String = { "$it/account/commitAddress" }
	val getNotification: (header: String) -> String = { "$it/account/unreadMessageList" }
	val marketSearch: (header: String) -> String = { "$it/account/searchPair?pair=" }
	val terms: (header: String) -> String = { "$it/index/agreement?md5=" }
	val getConfigList: (header: String) -> String = { "$it/index/getConfigList" }
	val getCurrencyLineChartData: (header: String) -> String = { "$it/account/lineDataByDay" }
	val getPriceByAddress: (header: String) -> String = { "$it/index/priceByAddress" }
	val getCoinInfo: (header: String) -> String = { "$it/market/coinInfo?symbol=" }
	val getUnreadCount: (header: String) -> String = { "$it/account/checkUnreadMessage" }
	val getNewVersion: (header: String) -> String = { "$it/index/getNewVersion" }
	val getShareContent: (header: String) -> String = { "$it/index/getShareContent" }
	val unregeisterDevice: (header: String) -> String = { "$it/account/unregisterDevice" }
	val defaultTokenList: (
		header: String,
		md5: String
	) -> String = { header, md5 ->
		"$header/index/defaultCoinList?md5=$md5"
	}
	val getTokenInfo: (
		header: String,
		condition: String,
		chainIDs: String
	) -> String = { header, condition, chainIDs ->
		"$header/index/searchToken?symbolOrContract=$condition&chainids=$chainIDs"
	}
	val getETCTransactions: (
		header: String,
		chainID: String,
		address: String,
		startBlock: String
	) -> String = { header, chainID, address, startBlock ->
		"$header/tx/pageList?chainid=$chainID&address=$address&start_block=$startBlock"
	}

	val getQuotationCurrencyCandleChart: (
		header: String,
		pair: String,
		period: String,
		size: Int
	) -> String = { header, pair, period, size ->
		"$header/chart/lineData?pair=$pair&period=$period&size=$size"
	}
	val getQuotationCurrencyInfo: (header: String, pair: String) -> String = { header, pair ->
		"$header/market/coinDetail?pair=$pair"
	}
}

object EtherScanApi {

	private val apikey: () -> String = { etherScanKeys.getRandom() }
	private const val mainHeader = "https://api.etherscan.io"
	private const val ropstenHeader = "https://api-ropsten.etherscan.io"
	private const val kovanHeader = "https://api-kovan.etherscan.io"
	private const val rinkebyHeader = "https://api-rinkeby.etherscan.io"
	private const val mainLogHeader = "https://api.etherscan.io"
	private const val kovanLogHeader = "https://kovan.etherscan.io"
	private const val ropstenLogHeader = "https://ropsten.etherscan.io"
	private const val rinkebyLogHeader = "https://rinkeby.etherscan.io"
	private val etherScanHeader: (chainID: ChainID) -> String = {
		when {
			it.isETHMain() -> mainHeader
			it.isRopsten() -> ropstenHeader
			it.isKovan() -> kovanHeader
			it.isRinkeby() -> rinkebyHeader
			else -> ropstenHeader
		}
	}
	private val etherScanLogHeader: (chainID: ChainID) -> String = {
		when {
			it.isETHMain() -> mainLogHeader
			it.isRopsten() -> ropstenLogHeader
			it.isKovan() -> kovanLogHeader
			it.isRinkeby() -> rinkebyLogHeader
			else -> ropstenLogHeader
		}
	}
	private val transactionDetailHeader: (currentChain: ChainID) -> String = {
		when {
			it.isETHMain() -> "https://etherscan.io/tx/"
			it.isRopsten() -> "https://ropsten.etherscan.io/tx/"
			it.isKovan() -> "https://kovan.etherscan.io/tx/"
			it.isRinkeby() -> "https://rinkeby.etherscan.io/tx/"
			else -> "https://etherscan.io/tx/"
		}
	}
	val gasTrackerHeader: (taxHash: String) -> String = {
		"${ChainURL.etcWebHeader()}$it"
	}
	val bitcoinTransactionDetail: (taxHash: String) -> String = {
		"${ChainURL.btcWebHeader()}$it"
	}
	val litecoinTransactionDetail: (taxHash: String) -> String = {
		"${ChainURL.ltcWebHeader()}$it"
	}
	val bitcoinCashTransactionDetail: (taxHash: String) -> String = {
		"${ChainURL.bchWebHeader()}$it"
	}
	val transactionDetail: (taxHash: String) -> String = {
		"${transactionDetailHeader(Config.getCurrentChain())}$it"
	}
	val eosTransactionDetail: (taxHash: String) -> String = {
		ChainURL.eosTransactionDetail(it)
	}
	val transactions: (address: String, startBlock: String) -> String = { address, startBlock ->
		"${etherScanHeader(Config.getCurrentChain())}/api?module=account&action=txlist&address=$address&startblock=$startBlock&endblock =99999999&sort=desc&apikey=${apikey()}"
	}
	val getTokenIncomingTransaction: (address: String, startBlock: String) -> String =
		{ address, startBlock ->
			"${etherScanLogHeader(Config.getCurrentChain())}/api?module=logs&action=getLogs&fromBlock=$startBlock&toBlock=latest&topic0=${SolidityCode.logTransferFilter}&topic2=${address.toAddressCode()}"
		}
}

// `EtherScan` 对 `Key` 的限制很大, 用随机的办法临时解决, 降低测试的时候出问题的概率
private val etherScanKeys = arrayListOf(
	"BEXAS7YQ2W64QF6THKH8K7KG96DQTD7IG1",
	"M4FTZEE44ED4M4BQVS7IG4UG2MU8VNPNNP",
	"T3CQM4UG4736IAE4GFCYJIAZRDK68FRQFX",
	"3SQVVQWA4JHQA261EMCY72UY4U7U66MAFN",
	"H7IQI8S9F4K3RDT1GZ7Z22XSS3H24XX7AN",
	"JZHQ457YKQC23U26PN4GKF9SXGQQ5I6ET4",
	"2KFCEGCCDNAAV789R9VGM5B3WJ8R1H2Q3A",
	"Z4I3GGRJJX2UMACFYTCQTUTG8X91J3WCSB",
	"SDTCFSK9SXKYR1PYFWY3XYJC5HHKQQ296E",
	"XNEMNU3P2FBTCG3M8ZR3NQSAPWVURTWK9E",
	"DMU4639VZX46PHJQQWWGY8W5XTX8X8KRJX",
	"3FT5TNURQW6H6DA241QSG6RQ6C51ZSCDMS",
	"781DHM28M378W4DQB6SGUGIFMMR4ME19TD",
	"KFUHHFM3WN8SGY4SEHXFBT6KAC81X82ZME",
	"ETRJ2HDTPDV9M1SBCQUG5KN69P9CVFBXN8",
	"HN6MCQEP7S8ETGH7INHXQVKC4AQMFHUBJX",
	"7UVKNNVGHAI7HSY1K1GUJTPG1A6DCTMZGB",
	"I5188KMH12FSGPRF5DIFDE5FKDGI4QNJQ7",
	"82EF59XS6DRXGWPMSZAIU6AKEGDMYF15HS",
	"NB9CAXASVQEMC2MFAA1JVH1PXUIPTFQA4J",
	"I4UFNFYVHYK7AWCPTF67I246ITVA339B3Q",
	"YGFK6DX9SCF8PTYG4TXWJZSZIUB16BHDC8",
	"CYCVZGY5PYXB3WRNJTX9EZPG3C2V4GW2WF",
	"G148TGXFBT28SGAHRQG3UD9VUNF2274VFM",
	"DP8UN684ZK5XF33AH8MNJCSGKS43YUJBUG",
	"5PFXCVIAYDX2UZATVUP8268NY82GNPKEQF",
	"FGEUQI1YWB9TA9CBW3WHBA9RWHHIA7RW3W",
	"PSDV28JWDEP5Y4PAFYW3BRITDRQ69FC21F",
	"RQ4283W59187SQHXF15FW17BPGCQVTN67C",
	"NM7186V4QN6QIFHHY9BF1MFVCRT77VZZI7",
	"WH1R22MSISVX1Z5BID3BSZE1IX5YXNWZSD",
	"J64SVCG1T2KIMXRCDEPA5KR38Z8I467KS8",
	"ERXX56CYXFRYGV1K9WECDAB5T5UZUHRNFI",
	"1TFRU35R2K6VUCJT2IKT2I2TXBWISSBE1T",
	"84REKDYNIY5KWE2JRRD35P3YH9IEWRNQK3",
	"9M8YYA18IXPG98V75FG3DJJAGQSMDTAZV7",
	"PJAYS8FS3U9B3Y2KBNJBKPAT92RYXB4R1D",
	"25UGPKH6YRFPDJI3AV9JCCW17NUI9NNQNZ",
	"DFR74C4X87IIXQP5FGUYY2KQZFHJPKJUWX",
	"RADKXGMBY7KCPV6FKSFT2SF234JPH1EE6E",
	"JRJ1771Y22ZRN454Z99AJSBHX3NCF1MJJ9",
	"SJ31EMM1JN9NWA2XK4PN9Z8K3CZAJJ1P4M",
	"JVRIMC461BN38V1QUHV37YMZ6CBP79UJWE",
	"RSJWIQ7A28IP2I1MVRYRSS6KQ7EH3SDZUM",
	"UQTGFUJEHNTMMAPBD63AG2I7R9E2BR9UTZ",
	"GUAI1G78U9ZFW8KZCF1G3DZA6JA11SXBZD",
	"PVM9JRMKKHS8U8UCZBCNKBVB3ZFMJUJG8F",
	"S2H71QY5J9NSTWHA5KD9MUZK3PVK2EIR18",
	"GN9DNPIDJYFZ3FK5AUNI3B1S15323BPBUZ",
	"IBGRP7PK2FNUZWAE6II3P7AQQI4MHRRB11",
	"IKQYB5I6AY1FMHEJWVAAW54PS7MVEBZ9AA",
	"V3CDN1M2JJUWD3VHYBUW6UQQW7D8MDUJ84",
	"YDZ69YGZSWE3S411P8HBDPPQVXYMU5B1TP",
	"WDP4NU23BP3NT4RS1WBXY7QY7RWF86D3W4",
	"52BCRVWIKY7CJ8XIPEHMBC2HEA4P4D5EGP",
	"49M6VFP9AEYHZEW4WUPANZBJUSBK46BQJK",
	"7F6DNNVH6G7U6F8XGES8D85KJWCAD15G9J",
	"8CDNE3RTGG92HXNP6F8Z3HPZ6KPN6NUZAG",
	"HIIR9IGZ7K8FZEEYXEANNZWIJPWUUIS86I",
	"NX1366VFMMRT9QVVEPM8TN3HHGB3M3WINR",
	"1HZF1B2CMXG6RE3RKV9TXB6K6SIJ78P7ZT",
	"Z86QMTICJ8H7D2J36C6Z1N3JJUAZ5BIB7J",
	"U8IXZRMY3SR1IZQ56M5KQ66YC5GMTR9FTY",
	"7INYYQDVHY5494W16UKBKKK14Q1QIWI9VA",
	"2M42WWJR23HKR57X9USTPF13X6WU87J8QG",
	"13RM63V6M8JTR86RZUKXNJ2NQ183YENC4A",
	"JGSM4EBQ8N4EIIBBU796BIX2D5NI47SV98",
	"8YJYNSVSCIJSU5PH6AXDKQ2IKI4DP6SPHM",
	"EEE815RGQ8BHKR1MVTNSV78WVE2F5YRIEM",
	"QQHANEPZDWTAXMKGUNJ2K2D65SUAPE52YX",
	"KZEWGTITD3X5UK79XNUCDJM9RPZ94YXW6X",
	"BJZ6F62TQ99JVBPX5578TRYQFR7TQSBDDZ",
	"YUR355GZCJN2S48B3YV9EMPGPVVVAWSWGP",
	"HRX8BFKK81G61F7UC4CQGW2GJEJPBD9MQB",
	"FMKS844GTBUP7FKPBFK3KKK58TIE6E38YV",
	"VPQ54BB94CXT2WGN9WV6EAWJBHZDHMZEFS",
	"N18TTMDFDR5GR4DCZAFSA5YKJ9IXD2UGRM",
	"MSUY1ACZNPHP7N39IYR9XGU8DRSEKKU1YJ",
	"KTPCSCIYNQK5R6DQUT9V84JZ1TBW211I85",
	"JBB6HDUSF55AIVUCI11DEFSC5P3YRE5K3D",
	"JM3KY46TTZWB264ZH9RDR4WQF9273DU99N",
	"I8R273J4W86CJYKC6ENBAY5BJGBX7H1G4J",
	"PE8RSPH6KTWAV4IM4K3DXX97C2FBVPFXM2",
	"858EHRGQA6716MI6UVSFK3K4K5CUR2EXNJ",
	"EDUVMJ8M7T1NS6Y9XYZ4SX1R2P9DDX8598",
	"P2R7UPVXZYQVYFPTT1MEDSHT9T9TWZMRKW",
	"XRSDTQGNCW9QH5UADN5MR59Z5ZFPTD9I4M",
	"4KXKZNREIFZENP81ZZTJQHC7ARPNI4YHW7",
	"DVDBIG447HWIPQQX81UE51T9JBRUHIA8KM",
	"JQRZ2YZI72B2EEQXM66SCIVHFR851NR4FS",
	"AJKRZ3H8MA6C4BBBFUWP8AMWQQEIKSKJAK",
	"CDS9H4SF6HRMP6PX7RQ7S2SXITP8WDV6B3",
	"UJTHJ6941GR83MYCZG69PSSVZSH19MJJVZ",
	"39DS96D5H4NIC4C9NSW16HX45B2TTWD23S",
	"FKWAX7WRNT4K3TG9U7T73YNR5K62XSF3HB",
	"4II813WGADQQJIU826D8W5VS4FXUIHYN78",
	"BCGESNE23497VC8HNFBRBJ76V5T3UN36XP",
	"6CU7I5TSC6SQDM1ZW97FVM5PBDCQXP4MTP",
	"2EQ8PBXE7T5ZXHPBMBT7TEA2XAZ8HHPH78",
	"D5X6EQPWZC896QBKJJ75J2XZ1Y66J6V5R7",
	"T9U84KDJM9VJWIUGUQUQWSBW85E7ZGCSUN",
	"DVZ27NC2WNTN37DSDIA2EMK4U2853EA1UE",
	"9YXI8AZAHCUPC5SBUZPI9NDT3N3M9IKB9A",
	"X6R5P3FEBCH2GK2IQPVATASX221T6TS6S6",
	"4H9SURM1CFRK775PNXCFPUQBRTQUWIPJT2",
	"82MG8HMTDBJGNIYI2UFTQV8EEQNHBB238T",
	"W7TEM65WRQ4SCJ8FD75KEYZD1XINIEN2IJ",
	"4139XKUGUQNXQ48FVWPSN51P8Z37YE2KE5",
	"GT7SZZHE5M9Q8ES2X7DEE1I61FN6YTA6EA",
	"CQP8HVPXZ8PVN75BARWX2C6H838IZ7YHHC",
	"H1GUXWFCDVRUTUHW1GW7C5WYMWMVD7X8CF",
	"BXEJP1M8R21SJMI7ES2757ASKXUDHEK7AT",
	"H4G5PE2G44I3KV1BFQNZWBUPIRKPZPHCTT",
	"5Y8EV38HIZ1ZTT2VPCFPQU5JW5HIHUGP61",
	"DZNMGTDIX22YSMVXXWWVX1JWRWHHHSCNNP",
	"RDSC23W798P9WJNYQCY91QTXM6KDQGV1M6",
	"W3MWTSXK34TT34CEZHW9R6AQ8N7IWM7T6X",
	"QMJ6Y1KNRWCA4E1HICA2PZ3T8ARMBP9RYE",
	"JXXPJRBV87HHXKWMIXVBAJU4GA1QJIXRJH",
	"IVY9B1KPK7XXG9JGWISVJY2WT11W5396DY",
	"NSEDM4Y8G5TK2XJKIPGAZ9WKAJSDV1W4EK",
	"6ZNZN4TNJJHZTYGBNW8E945C1ZRQIXF2YG",
	"AQEFQIDSY2TZ8GPJ8V89P6J2FRTN92MS2S",
	"E3ECGEE71Y2R1C465I4PCRC7U81CXEAI47",
	"2EKUGP269FCSUCWU9ZGY1A3DZ9NQ9W6TPM",
	"JGSI3DP98NDBZFH16X4GC9EDIUQZ1DHKND",
	"FSZ6XBC5WQ26CKYQH2HDAFT49JQ8EYC9II",
	"CJ9T1N2HZIXZMT2CD49P4VR6JFXU426NG3",
	"RZ4QFBU6YCEIGSC9YXZDMW9Y6ZDZ2V6S3D",
	"WVDRKST9517CBQZAEIIQ2INJG15GQQX6KK",
	"ZTF8YDA9WIU8F5GINNTU7F5WSY9UI5KNNU",
	"BSUA5BEU6641924W41NSTZ4SZY5WMHH2XF",
	"5BNWHGFWNSM6G8WQ2Z19M3Z2GQV5YGHS56",
	"QQACZC133A8E7CHI6NMHTNQ8WAGW5KTFA2",
	"DT3FY28DSF32GW1X7HUGXQ3UKPKC97NMRF",
	"J533RQZ17WW4X52JA21B88PMXREF1526T3",
	"ZYHZ372RGSBMATQZBB8RZRAW3CTMQCQ6T8",
	"EX2TJRE9Z4EVFI3J3TX2RNM6Q8YSJMD937",
	"MZHRDAHQB1388VQ2BAESNH1U2CVK72SIB4",
	"HSED6R5EQCZ51SJY1FR1M1RKBTUI7PJGTX",
	"2E4HYJ17PN18S7KRRYP3GJKVHPXF1JW338",
	"8MKGQ5DC7SDXNCXE5DRV29TH6EYCHJUS16",
	"VWGJZTSTCVV9VP4WHUETYN3ZEKWFAS359R",
	"7S9J5VGJEA9XR5G21QY55JPA6J22AM75P1",
	"MQE17J8J4PNFECPWWUIMKKY9MFPZM66BM1",
	"JDFRF1C1JNDR9IWUVKHMMEU2RVH8Q12ZQJ",
	"VUQDPDW1BY22JA5EWN7XPHZPNSWMABX3BZ",
	"9U7WJVS379I8SN6PV227F81UTET86VFCPC",
	"5JAGKZGFVEC5YQ8ANEWR6XJFN669AZZ1GM",
	"YP12EKCII72TNDE9HI5YXF1RIZPYA5TKGH",
	"B5BJ26BXBG8FCVM9CJRIJVAIV75EUU3EWE",
	"JIFR86BHBGJPZT88QJNPVFWSNW1V4HETJY",
	"B4TMBK83JTY87C7K4HS7I5PN3D5EET2R41",
	"J6EKU8AUKKF192GSXP7GGAPR7R66M6CBF1",
	"3TVCS1UUPBP3SD5HH6JJNT4552WP8IYCNR",
	"NGR2UMJQG6U6JUA6BM5TSN24TESTPB32A8",
	"ZXIP137XK1S79Y17F5869TEX2CF198AQQQ",
	"GEKWNNTUCF355M365C9UPN1NUB7TK55CFF",
	"KFH6QTWYQNWFCGB4JPRZRYFUCDK3CKHH1X",
	"1YZ5HP6I4S72TT6FMR3CR3TRAMM553S8N5",
	"QWRJD4FY8M7YRGKUIUCCKM2Z9RHAU5WZAK",
	"YGE9AS25AFEZYQURQWTMHCEEZI9RA5KEQC",
	"F111C756M2INQEKG2X479PM9KS22BMZ87X",
	"HUD5SBZQQUF7Q96RK2JTPU85IZAXD4U8ZT",
	"734EEYPKQUVZQYUD64I4WCWZWFPS7MZN1X",
	"QZNY1KDY9HES1M6Y4B1N4JC79WRJ8B8KJH",
	"DEX1AZNUMYN3RJU5133KUT3QRE93KAQ2H9",
	"PISK7F3PE55S73DKYAJ1SWG2US6Z45E39Y",
	"7BWBPANCGGKCRRVX4V9YTKDGMBG2H3QU8I",
	"SU5IAYBQTQ9US66V1H45IMYEQDTKWERS57",
	"Z2SJMAN3TN7TIBVK28Q315WQUV5QWDZHQJ",
	"7Y3S2BA8CC91ZSH1YHKD95IJW2FTY768AJ",
	"GYJJ2PCI6X8QP8GK6QI1R95XGF1IFUFAEY",
	"BIFEGV1QZMB5D8CC2FQ9U2EHX1VH9572RE",
	"MB1IUHCHZ7RAJWPG68TDS9ZSJ59HMG3R1Q",
	"5ZAU9QW9WWTI5KXCQISEQN3E28MCYPQQCM",
	"AWXKG1CIJSS59RCMZP4GT​​GXWT23HA6547H",
	"FU7D87AI7DENX28SKF3KQEDS24Z214RS6F",
	"R2A8CJCU22SVRSWP1QR1I1GEB479I4NNHG",
	"1NMDJKKNFT56KFXVS3SI5FCTD92FFSID23",
	"7AIUBJD1UN9A5HN6IMH73649ZV7BBDVG2N",
	"76CHFI2PS6BHVWMSVRH29179WFD2PGST8G",
	"AGU26Z6DEYYHZBQJG6ZZE7Z3N1611AEI9F",
	"8R6RMIM8Q3M4UV1U1D14WY43VFK91Y749V",
	"QI84NVIMDN5X5CFP1RTEVG5VAQFU9TEUC6",
	"2CAS9TBGDVUXDHE511AJ69DBTXGQDK9AJY",
	"H2TACNAJBNPF764WAFFBAC24M5VQ86UZQ9",
	"WXVQTFWQX2VTRIU6TYW3E51PN7SJC5UQQX",
	"B6YKM1GKEZ8TECV2DV5NUQV72KNBDT864T",
	"HG7NWRZE7HUUZ2FF3GA3IHSZGGFC8GSFYF",
	"KZBRQKWI3D3RQDW4QKFKEZQ76DAM6RREWJ",
	"GF5BRQ71FDVRS2H5R61C1KA4KCA9DCNPW5",
	"G816YDAX5Q942US9BVA92JANHKE4GY35QX",
	"M3X23ABUACXJAIVPFY6M5ZTT6H26I1VBA5",
	"KJ7B2C873UGY2BU69IFB6M94VJ85BAGKA5",
	"CJCMRVUIQ843XGJ2JSYKNYDQJBDP3A944V",
	"8DJXHMYI1AMWEEBF2CHEFE6N2EH5GFZ1Y1",
	"96HN9BN4X33PXZDM8VKPGT7XW44T3KBMXD",
	"8YZIW86T9IN7645JJ2EV1EG5CHATV1NCG3",
	"FEV3TSAACRZWCTPSNXD9WIBZRGFKS7WJ6C",
	"TGEMAT36N7HTFI8ABJ1XQV4XCBBQH6WEQZ",
	"GQPTQVJH53ET5UBTZH9A2SE9PUNCMRGUQH",
	"F9HA5RT13FF6EFAWCUM8TPBE5V98VDKK4N",
	"YYDMYK58H41VV15ZEM4W7R63RXTKHIRFFZ",
	"VYUZP7P5TU51IJ7EPM631GCUWSK2MBXMT7",
	"79J8345NMK4II6Z1ZSRW3XBUCNFAUDW75X",
	"8S242TTFTZVKCFPGG5SC99YQWFNWU9RXDM",
	"YTMKYMD1BBFKPKCNETVHWITKSQ5HE7TERQ",
	"MCB712JF5WKJH23TX5S2UF12U9GWM5I2UY",
	"WVE42TN2U198MEMFFN2D5J9QMI6A8RP89U",
	"BPVYHKH521TB2W3V5YRT7FPMIT2Q6QB6X5",
	"FIMIQUKT54SY8W3UYHG4MVZ9FPJTYCPNEV",
	"EIAK8X7EAKUJK7MCRTPTIJJC429TUTE5JB",
	"UR6ZB33DF4GE1DUC29HNEN5CZ9JAHXID9X",
	"W9Q97HURWKX8GP8Q4767J7MUZ3HSVIK364",
	"HKNS31UNTJESSW3TUC82CPNV94Z2WC1W2C",
	"TPYVYFAR549R5ND3CDHXTED1TRRQZRVG3W",
	"FYXB5V9SJ3QSJQMF96KQ1EAX48PKSD8TP9",
	"8F528KN2UBB2624JHQWCW1NZ8X8P16UX3A",
	"KKP6VYG6FYEZ7RT1X4Q1JNGQRN91D2X14F",
	"M8HK6R277YFWQ2HPTEFX8X4ZF9PGTD2EWR",
	"4KHBYYZCFHAWY8HP9HJ68P4I5X3JETX11Y",
	"FS1U9XCGN9IJMN57DHYRNWSHY1FVFMAU16",
	"NVZV5XUAG2VWNAC7HNFS4JZHW9GG22YPJF",
	"Z87HD2XDDRM3C5I85MD833VH1VFD8F37B8",
	"9FV9WZUF9BPHGSQETR8PYV35QGPRRQ7PFC",
	"63EP59177YUAQT1N8ZY8BCCSIEA4PJ4135",
	"KN15KZPRQ5BFRVQ3G8Q82UQM5RZVHNUT8K",
	"W562XI7JXMYTPN7JTKB35NTXEPNIRBV4Y5",
	"DKZZQZE1XHW49SPHSKM278B9N9WNUMQUWI",
	"9MKTVBN4QA3BEVW9IBHXY69RCF5S7BS18R",
	"5TNWW8IPNGZUBPGRCVDTVQI6T93J5A3CPW",
	"DBM1B8DH1DFMBZYW13XU8EAH2M5ZJCEM6F",
	"3E7BSC5ZCW31F16YQ2DUJJGPFU7WD4Z11P",
	"6ZI2BNR5MD7QXZ5GHQG1JVCRA3KSG4PRT5",
	"25TYWD47BSXS747895RNWANSS82ZNIPFVT",
	"XZNIW3XCJJUHVGJ9UJ169RH27NB5JT3GVE",
	"H73IWZP5YQJE7C4T6ZMH91IP6T4Q744XGJ",
	"C4UYM2R2985JAUWQ9XA4NHNEC2VZYWKNXM",
	"1E1JQ6JIKUEZ4CN1ZUGZQTNV1G34JWZJNM",
	"JXESJRQPXZC1XADV5FMDHVC4PWZF6Y5YNS",
	"RM1MK15GQXBGUS7DERFJ5NJB3PS9IZWDD6",
	"JRVIWM827FS8179SIDH4M33PIV9VYR3X3P",
	"FAUGQJGHEE7P2BY186Y7K2YKH3KF8KZUEQ",
	"5S4M9UIBNZ8KBD6GFMT8SEB8ZNY1AUEC9C",
	"66VKX5PADZDXS89R5D16YRFVNMWI1UJWBZ",
	"5QMHQBHKGNADBCS8MA9I7XMXU6JXQ1ZDBX",
	"RV9TSJ83HD4K8XARAZJBX73QPISKWFPV3D",
	"MVZUHQB4F9T8YMMYRZTZ1ITJ48SN73Q7IU",
	"WDSSZDG8PD6QRKXF9IEVD5SIA3723VR2E3",
	"2536IGMTHV9X6IYQGZ38VQM8GV7F6TM5BN",
	"DW75F8AIDEN3YXM6SENX12ST8V9SWNNRUT",
	"DTX3T4QNH3ITA2FZE6E67KX452ITMZXDXW",
	"N7I6A4NGUYVJ3HEUGTQNNSWTXSJV2I56VE",
	"D9G8WAIRSZ8KY2ESH5P98WBR9IISFBTRPN",
	"VIWGJ9XVY4J7961EVTF6N43E1YBV1SX2GG",
	"RWJKGUF5ZKGEAP4BM1XPUJFG6HINWSF5YI",
	"GBZ5BQNQG1JB7JQ826VEH7E3EYBT985CGR",
	"BHVUSSQ5H5ZXWRKKYP6UFC9SP5WYU3DC88",
	"SNNF5YWH33685X6EGRJNIY945MVSAFRMQN",
	"ZYYII7YY96MFZYTEKNTJPTDIAH347NBIIH",
	"EKE9FS66AS8EXPX8EEWH843QWPS13ECGIF",
	"RBY6QIGHPX7RBB91VVHRHBHQERZ64VFRVH",
	"XKA9PB2QE5H5CGR3HFZSYFT8RYIAETHFUU",
	"RF7T8YMWRQ99H27PY2UFGED68WCNDV7PMW",
	"C4TK8ASIH68KZERCFYJW1C32JCKYHWHJNM",
	"P4WDUJNXV7XMVYX2G95RUZ2S1WPU1NPNYC",
	"6PWAUNJ8S6ZPNE889S61EWYNVT6CP19GMH",
	"JX53QMG73JIBWECRNIRAEP463TK2U7WI74",
	"7ZDZFVJNQNN54ACNJM7R2WUY1X1FCVPZ5R",
	"CSUBWG74A69T4CQ7H8WPTDA7E1N6HMQKIH",
	"3NHCNKK21VMBCERP6IHSKZKPW1MCUYXGB6",
	"KS6VX72FIKSYHXC5TEWYT33UHNY883Y516",
	"CFHS7WZNH13KSXH7G45YWBPZUE5FT1SRKE",
	"RQI19GZQI7DGI13KY5BPG1SIXSRJUH8C1D",
	"6SNFWNY6TGKDV5C82NEVYRY13GREW1Z2F6",
	"GBUJ8ME1HQC8YJSJW378QEKF5EMFGMTPV9",
	"8SA9BV58443UUR9UKPSW42RDFUZZIVVPBQ",
	"CZ9KSB4GAHDBJV6N25V1562VUGV6Q87DH5",
	"PDYP3Z5FZN7FRA6CZ4CVPSFRKZFI8G1T5N",
	"KB8X4PHYGA4GIP72V8SAWXY834IK8IMDA4",
	"YUBFB2H581II2CGVX2S1AQKZZP1JDVCCMN",
	"KBIM1RZC1KYSMU2VBN3SJSZK2S3BVN14PS",
	"21VDD8YWDHFY4SGM9X4RY9386E7DRW7GWM",
	"BJYQFJS8YEJ7VUFJF433VTXN7Z1K4IBY4H"
)
