package yokwe.stock.jp.sony;

import yokwe.util.UnexpectedException;

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
	BLACKROCK("070", "ブラックロック"),
	MANULIFE ("077", "マニュライフ"),
	SMDS     ("079", "三井住友DS"),
	SMTAM    ("080", "三井住友TAM"),
	UFJ      ("081", "三菱UFJ国際"),
	UBS      ("087", "UBS"),
	RAKUTEN  ("089", "楽天"),
	RHEOS    ("093", "レオス"),
	LM       ("094", "L・メイソン");
	
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