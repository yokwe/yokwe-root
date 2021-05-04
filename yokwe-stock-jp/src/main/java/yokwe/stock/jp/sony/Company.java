package yokwe.stock.jp.sony;

import yokwe.util.UnexpectedException;

// https://moneykit.net/data/fund/SFBA1700F470.js
//FundManagerMst= {
//	"1":[{"KanaCode":"002","KaisyaMei":"朝日ライフ"}],
//	"2":[{"KanaCode":"004","KaisyaMei":"アセマネOne"}],
//	"3":[{"KanaCode":"006","KaisyaMei":"アムンディ"}],
//	"4":[{"KanaCode":"009","KaisyaMei":"イーストS"}],
//	"5":[{"KanaCode":"011","KaisyaMei":"インベスコ"}],
//	"6":[{"KanaCode":"012","KaisyaMei":"HSBC"}],
//	"7":[{"KanaCode":"014","KaisyaMei":"NN"}],
//	"8":[{"KanaCode":"024","KaisyaMei":"コモンズ"}],
//	"9":[{"KanaCode":"027","KaisyaMei":"シュローダー"}],
//	"10":[{"KanaCode":"033","KaisyaMei":"JPモルガン"}],
//	"11":[{"KanaCode":"034","KaisyaMei":"ジャナス・キャピタル"}],
//	"12":[{"KanaCode":"039","KaisyaMei":"SOMPO"}],
//	"13":[{"KanaCode":"041","KaisyaMei":"大和"}],
//	"14":[{"KanaCode":"046","KaisyaMei":"T&D"}],
//	"15":[{"KanaCode":"049","KaisyaMei":"ドイチェ"}],
//	"16":[{"KanaCode":"050","KaisyaMei":"日興"}],
//	"17":[{"KanaCode":"051","KaisyaMei":"日興AMヨーロッパ"}],
//	"18":[{"KanaCode":"052","KaisyaMei":"ニッセイ"}],
//	"19":[{"KanaCode":"055","KaisyaMei":"野村"}],
//	"20":[{"KanaCode":"061","KaisyaMei":"BNPパリバ"}],
//	"21":[{"KanaCode":"063","KaisyaMei":"ピクテ"}],
//	"22":[{"KanaCode":"065","KaisyaMei":"ファイブスター"}],
//	"23":[{"KanaCode":"067","KaisyaMei":"フィデリティ"}],
//	"24":[{"KanaCode":"069","KaisyaMei":"フランクリン"}],
//	"25":[{"KanaCode":"070","KaisyaMei":"ブラックロック"}],
//	"26":[{"KanaCode":"077","KaisyaMei":"マニュライフ"}],
//	"27":[{"KanaCode":"079","KaisyaMei":"三井住友DS"}],
//	"28":[{"KanaCode":"080","KaisyaMei":"三井住友TAM"}],
//	"29":[{"KanaCode":"081","KaisyaMei":"三菱UFJ国際"}],
//	"30":[{"KanaCode":"087","KaisyaMei":"UBS"}],
//	"31":[{"KanaCode":"089","KaisyaMei":"楽天"}],
//	"32":[{"KanaCode":"093","KaisyaMei":"レオス"}]};

public enum Company {
	ASAHILIFE("002", "朝日ライフ"),
	ASEMANE  ("004", "アセマネOne"),
	AMUNDI   ("006", "アムンディ"),
	EAST     ("009", "イーストS"),
	INVESCO  ("011", "インベスコ"),
	HSBC     ("012", "HSBC"),
	NN       ("014", "NN"),
	COMMONS  ("024", "コモンズ"),
	SCHRODER ("027", "シュローダー"),
	JPM      ("033", "JPモルガン"),
	JANAS    ("034", "ジャナス・キャピタル"),
	SOMPO    ("039", "SOMPO"),
	DAIWA    ("041", "大和"),
	TD       ("046", "T&D"),
	DEUTSCH  ("049", "ドイチェ"),
	NIKKOU   ("050", "日興"),
	NIKKOUAM ("051", "日興AMヨーロッパ"),
	NISSEI   ("052", "ニッセイ"),
	NOMURA   ("055", "野村"),
	BNP      ("061", "BNPパリバ"),
	PICTE    ("063", "ピクテ"),
	FIVESTAR ("065", "ファイブスター"),
	FEDILITY ("067", "フィデリティ"),
	FRANKLIN ("069", "フランクリン"),
	BLACKROCK("070", "ブラックロック"),
	MANULIFE ("077", "マニュライフ"),
	SMDS     ("079", "三井住友DS"),
	SMTAM    ("080", "三井住友TAM"),
	UFJ      ("081", "三菱UFJ国際"),
	UBS      ("087", "UBS"),
	RAKUTEN  ("089", "楽天"),
	RHEOS    ("093", "レオス");
	
	public final String code;
	public final String name;
	Company(String code, String name) {
		this.code = code;
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}
	
	public static Company get(String code) {
		for(Company e: Company.values()) {
			if (e.code.equals(code)) return e;
		}
		Fund.logger.error("Unexpected code {}!", code);
		throw new UnexpectedException("Unexpected value");
	}
}