package yokwe.stock.jp.tdnet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class TDNET {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String DIR_BASE = Storage.getPath("tdnet");
	public static String getPath() {
		return DIR_BASE;
	}
	public static String getPath(String path) {
		return String.format("%s/%s", DIR_BASE, path);
	}
	
	private static final String SUMMARY_DIR = getPath("summary");
	
	public static String getSummaryFilePath(SummaryFilename filename) {
		return String.format("%s/%s/%s", SUMMARY_DIR, filename.tdnetCode, filename);
	}
	
	private static final String PATH_TOUCH_FILE = String.format("%s/%s", DIR_BASE, "tdnet.touch");
	public static void touch() {
		logger.info("touch {}", TDNET.PATH_TOUCH_FILE);
		FileUtil.touch(PATH_TOUCH_FILE);
	}
	
	private static List<File> summaryFileList = null;
	public static List<File> getSummaryFileList() {
		if (summaryFileList == null) {
			summaryFileList = new ArrayList<>(getSummaryFileMap().values());
		}
		
		return summaryFileList;
	}
	public static List<File> getSummaryFileList(Category category) {
		List<File> ret = new ArrayList<>();
		for(Map.Entry<SummaryFilename, File> entry: getSummaryFileMap().entrySet()) {
			SummaryFilename financialSummary = entry.getKey();
			File            file             = entry.getValue();
			if (financialSummary.category == category) {
				ret.add(file);
			}
		}
		return ret;
	}

	private static Map<SummaryFilename, File> summaryFileMap = null;
	public static Map<SummaryFilename, File> getSummaryFileMap() {
		if (summaryFileMap == null) {
			summaryFileMap = new TreeMap<>();
			for(File file: FileUtil.listFile(SUMMARY_DIR)) {
				SummaryFilename key = SummaryFilename.getInstance(file.getName());
				if (key == null) {
					logger.error("Unexpected filename");
					logger.error("  file  {}", file.getPath());
					throw new UnexpectedException("Unexpected filename");
				}
				
				if (summaryFileMap.containsKey(key)) {
					logger.error("Duplicate key {}", key);
					logger.error("  new {}", file.getName());
					logger.error("  old {}", summaryFileMap.get(key).getName());
					throw new UnexpectedException("Duplicate key");
				} else {
					summaryFileMap.put(key, file);
				}
			}
		}
		
		return summaryFileMap;
	}


	// 適時開示システム タクソノミ設定規約書
	//   https://www.jpx.co.jp/equities/listing/xbrl/03.html

	// 決算短信財務諸表情報
	// インラインXBRLファイル名
	//   {一意の7桁数値}-{財表識別区分}-tse-{報告書}[{報告書詳細区分}]-{証券コード}-{期末日}-{提出回数}-{提出日}-ixbrl.htm
	//   0500000-qcbs01-tse-qcedjpfr-71770-2017-06-30-01-2017-07-25-ixbrl.htm
	//   0600000-qcpl11-tse-qcedjpfr-71770-2017-06-30-01-2017-07-25-ixbrl.htm
	//   0700000-qcci11-tse-qcedjpfr-71770-2017-06-30-01-2017-07-25-ixbrl.htm
		
	// 提出日     :=  報告書の提出日（YYYY-MM-DD形式）
	// 期末日     :=  報告期間の期末日（YYYY-MM-DD形式）
	
	// 財表識別区分
	// a----- 通期
	// q----- 四半期第
	// s----- 中間期  特定２Ｑ
	// -c---- 連結
	// -n---- 非連結
	// -r---- REIT
	// -e---- ETF
	// --bs-- 貸借対照表
	// --pc-- 損益及び包括利益計算書
	// --ss-- 株主資本等変動計算書
	// --cf-- キャッシュ・フロー計算書
	// --ci-- 包括利益計算書
	// --pl-- 損益計算書
	// ----99 様式
	
	// 通期第１号様式　[日本基準]（連結）
	// acbs01  連結貸借対照表
	// acpc01  連結損益及び包括利益計算書
	// acss01  連結株主資本等変動計算書
	// accf01  連結キャッシュ・フロー計算書
	// anbs01  貸借対照表
	// anpl01  損益計算書
	// anss01  株主資本等変動計算書
	// ancf01  キャッシュ・フロー計算書
	
	// 通期第２号様式　[日本基準]（非連結）
	// anbs02  貸借対照表
	// anpl02  損益計算書
	// anss02  株主資本等変動計算書
	// ancf02  キャッシュ・フロー計算書
	
	// 通期第３号様式　[IFRS]（連結）　※1計算書方式の場合
	// acbs03  連結財政状態計算書
	// acpc03  連結包括利益計算書 ?? typo accci03
	// acss03  連結持分変動計算書
	// accf03  連結キャッシュ・フロー計算書
	// anbs03  貸借対照表
	// anpl03  損益計算書
	// anss03  株主資本等変動計算書
	// ancf03  キャッシュ・フロー計算書
	
	// 通期第３号様式　[IFRS]（連結）　※2計算書方式の場合
	// acbs03  連結財政状態計算書
	// acpl03  連結損益計算書
	// acci03  連結包括利益計算書
	// acss03  連結持分変動計算書
	// accf03  連結キャッシュ・フロー計算書
	// anbs03  貸借対照表
	// anpl03  損益計算書
	// anss03  株主資本等変動計算書
	// ancf03  キャッシュ・フロー計算書
	
	// 通期[IFRS]（非連結）＊2計算書方式
	// anbs53  財政状態計算書
	// anpl53  損益計算書
	// anci53  包括利益計算書
	// anss53  持分変動計算書
	// ancf53  キャッシュ・フロー計算書
	
	// 通期第４号様式　[米国基準]（連結）
	// anbs04  貸借対照表
	// anpl04  損益計算書
	// anss04  株主資本等変動計算書
	// ancf04  キャッシュ・フロー計算書
	
	// 四半期第１号様式　[日本基準]（連結）
	// qcbs01  四半期連結貸借対照表
	// qcpc11  四半期連結損益及び包括利益計算書（四半期連結累計期間）
	// qcpc21  四半期連結損益及び包括利益計算書（四半期連結会計期間）
	// qccf01  四半期連結キャッシュ・フロー計算書
	
	// 四半期第１号様式　[日本基準]（連結）
	// qcbs01  四半期連結貸借対照表
	// qcpl11  四半期連結損益計算書（四半期連結累計期間）
	// qcpl21  四半期連結損益計算書（四半期連結会計期間）
	// qcci11  四半期包括利益計算書（四半期連結累計期間）
	// qcci21  四半期包括利益計算書（四半期連結会計期間）
	// qccf01  四半期連結キャッシュ・フロー計算書
	
	// 四半期第２号様式　[日本基準]（非連結）
	// qnbs02  四半期貸借対照表
	// qnpl12  四半期損益計算書（四半期累計期間）
	// qnpl22  四半期損益計算書（四半期会計期間）
	// qncf02  四半期キャッシュ・フロー計算書
	
	// 四半期第３号様式　[IFRS]（連結） ※単一の要約計算書の場合
	// qcfs03  要約四半期連結財政状態計算書 任意 必須 qcfs03
	// qcci13  要約四半期連結包括利益計算書（四半期連結累計期間） 任意 必須 qcci13
	// qcci23  要約四半期連結包括利益計算書（四半期連結会計期間） 任意 任意 qcci23
	// qcss03  要約四半期連結持分変動計算書 任意 必須 qcss03
	// qccf03  要約四半期連結キャッシュ・フロー計算書 任意 必須 ※開示する場合は必須 qccf03
	
	// 四半期第３号様式　[IFRS]（連結）　※要約分離損益計算書及び要約包括利益計算書の場合
	// qcfs03  要約四半期連結財政状態計算書
	// qcpl13  要約四半期連結損益計算書（四半期連結累計期間）
	// qcpl23  要約四半期連結損益計算書（四半期連結会計期間）
	// qcci13  要約四半期連結包括利益計算書（四半期連結累計期間）
	// qcci23  要約四半期連結包括利益計算書（四半期連結会計期間）
	// qcss03  要約四半期連結持分変動計算書
	// qccf03  要約四半期連結キャッシュ・フロー計算書
	
	// 四半期[IFRS]（非連結） ＊1計算書方式
	// qnfs53  要約四半期財政状態計算書
	// qnci63  要約四半期包括利益計算書（四半期連結累計期間）
	// qnci73  要約四半期包括利益計算書（四半期連結会計期間）
	// qnss53  要約四半期持分変動計算書
	// qncf53  要約四半期キャッシュ・フロー計算書
	
	// 四半期[IFRS]（非連結） ＊2計算書方式
	// qnfs53  要約四半期財政状態計算書
	// qnpl63  要約四半期損益計算書（四半期連結累計期間）
	// qnpl73  要約四半期損益計算書（四半期連結会計期間）
	// qnci63  要約四半期包括利益計算書（四半期連結累計期間）
	// qnci73  要約四半期包括利益計算書（四半期連結会計期間）
	// qnss53  要約四半期持分変動計算書
	// qncf53  要約四半期キャッシュ・フロー計算書
	
	// 四半期第５号様式　[日本基準]（連結）（特定２Ｑ）　※1計算書方式の場合
	// scbs05  中間連結貸借対照表
	// scpc05  中間連結損益及び包括利益計算書
	// scss05  中間連結株主資本等変動計算書
	// sccf05  中間連結キャッシュ・フロー計算書
	// snbs05  中間貸借対照表
	// snpl05  中間損益計算書
	// snss05  中間株主資本等変動計算書
	// sncf05  中間キャッシュ・フロー計算書
	
	// 四半期第５号様式　[日本基準]（連結）（特定２Ｑ）　※2計算書方式の場合
	// scbs05  中間連結貸借対照表
	// scpl05  中間連結損益計算書
	// scci05  中間連結包括利益計算書
	// scss05  中間連結株主資本等変動計算書
	// sccf05  中間連結キャッシュ・フロー計算書
	// snbs05  中間貸借対照表
	// snpl05  中間損益計算書
	// snss05  中間株主資本等変動計算書
	// sncf05  中間キャッシュ・フロー計算書
	
	// 四半期第６号様式　[日本基準]（非連結）（特定２Ｑ）
	// snbs06  中間貸借対照表
	// snpl06  中間損益計算書
	// snss06  中間株主資本等変動計算書
	// sncf06  中間キャッシュ・フロー計算書
	
	// 四半期第７号様式　[IFRS]（連結）（特定２Ｑ）　※単一の要約計算書の場合
	// scfs07  要約中間連結財政状態計算書
	// scci07  要約中間連結包括利益計算書
	// scss07  要約中間連結持分変動計算書
	// sccf07  要約中間連結キャッシュ・フロー計算書
	// snbs07  中間貸借対照表
	// snpl07  中間損益計算書
	// snss07  中間株主資本等変動計算書
	// sncf07  中間キャッシュ・フロー計算書
	
	// 四半期第７号様式　[IFRS]（連結）（特定２Ｑ）　※要約分離損益計算書及び要約包括利益計算書の場合
	// scfs07  要約中間連結財政状態計算書
	// scpl07  要約中間連結損益計算書
	// scci07  要約中間連結包括利益計算書
	// scss07  要約中間連結持分変動計算書
	// sccf07  要約中間連結キャッシュ・フロー計算書
	// snbs07  中間貸借対照表
	// snpl07  中間損益計算書
	// snss07  中間株主資本等変動計算書
	// sncf07  中間キャッシュ・フロー計算書
	
	// REIT様式（通期）
	// arbs01  貸借対照表 必須 必須 arbs01
	// arpl01  損益計算書 必須 必須 arpl01
	// arss01  投資主資本等変動計算書 必須 必須 arss01
	// ards01  金銭の分配に係る計算書 必須 必須 ards01
	// arcf01  キャッシュ・フロー計算書 必須 必須 arcf01
	
	// REIT様式（中間期）
	// srbs01  中間貸借対照表
	// srpl01  中間損益計算書
	// srss01  中間投資主資本等変動計算書
	// srcf01  中間キャッシュ・フロー計算書
	
	// ETF様式（通期）
	// aebs01  貸借対照表
	// aepl01  損益及び剰余金計算書
	
	// ETF様式（中間期）
	// sebs01  中間貸借対照表
	// sepl01  中間損益及び剰余金計算書
}
