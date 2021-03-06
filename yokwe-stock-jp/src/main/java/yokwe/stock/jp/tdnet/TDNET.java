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
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TDNET.class);
	
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


	// ???????????????????????? ??????????????????????????????
	//   https://www.jpx.co.jp/equities/listing/xbrl/03.html

	// ??????????????????????????????
	// ???????????????XBRL???????????????
	//   {?????????7?????????}-{??????????????????}-tse-{?????????}[{?????????????????????}]-{???????????????}-{?????????}-{????????????}-{?????????}-ixbrl.htm
	//   0500000-qcbs01-tse-qcedjpfr-71770-2017-06-30-01-2017-07-25-ixbrl.htm
	//   0600000-qcpl11-tse-qcedjpfr-71770-2017-06-30-01-2017-07-25-ixbrl.htm
	//   0700000-qcci11-tse-qcedjpfr-71770-2017-06-30-01-2017-07-25-ixbrl.htm
		
	// ?????????     :=  ????????????????????????YYYY-MM-DD?????????
	// ?????????     :=  ???????????????????????????YYYY-MM-DD?????????
	
	// ??????????????????
	// a----- ??????
	// q----- ????????????
	// s----- ?????????  ????????????
	// -c---- ??????
	// -n---- ?????????
	// -r---- REIT
	// -e---- ETF
	// --bs-- ???????????????
	// --pc-- ?????????????????????????????????
	// --ss-- ??????????????????????????????
	// --cf-- ????????????????????????????????????
	// --ci-- ?????????????????????
	// --pl-- ???????????????
	// ----99 ??????
	
	// ????????????????????????[????????????]????????????
	// acbs01  ?????????????????????
	// acpc01  ???????????????????????????????????????
	// acss01  ????????????????????????????????????
	// accf01  ??????????????????????????????????????????
	// anbs01  ???????????????
	// anpl01  ???????????????
	// anss01  ??????????????????????????????
	// ancf01  ????????????????????????????????????
	
	// ????????????????????????[????????????]???????????????
	// anbs02  ???????????????
	// anpl02  ???????????????
	// anss02  ??????????????????????????????
	// ancf02  ????????????????????????????????????
	
	// ????????????????????????[IFRS]??????????????????1????????????????????????
	// acbs03  ???????????????????????????
	// acpc03  ??????????????????????????? ?? typo accci03
	// acss03  ???????????????????????????
	// accf03  ??????????????????????????????????????????
	// anbs03  ???????????????
	// anpl03  ???????????????
	// anss03  ??????????????????????????????
	// ancf03  ????????????????????????????????????
	
	// ????????????????????????[IFRS]??????????????????2????????????????????????
	// acbs03  ???????????????????????????
	// acpl03  ?????????????????????
	// acci03  ???????????????????????????
	// acss03  ???????????????????????????
	// accf03  ??????????????????????????????????????????
	// anbs03  ???????????????
	// anpl03  ???????????????
	// anss03  ??????????????????????????????
	// ancf03  ????????????????????????????????????
	
	// ??????[IFRS]??????????????????2???????????????
	// anbs53  ?????????????????????
	// anpl53  ???????????????
	// anci53  ?????????????????????
	// anss53  ?????????????????????
	// ancf53  ????????????????????????????????????
	
	// ????????????????????????[????????????]????????????
	// anbs04  ???????????????
	// anpl04  ???????????????
	// anss04  ??????????????????????????????
	// ancf04  ????????????????????????????????????
	
	// ???????????????????????????[????????????]????????????
	// qcbs01  ??????????????????????????????
	// qcpc11  ?????????????????????????????????????????????????????????????????????????????????
	// qcpc21  ?????????????????????????????????????????????????????????????????????????????????
	// qccf01  ???????????????????????????????????????????????????
	
	// ???????????????????????????[????????????]????????????
	// qcbs01  ??????????????????????????????
	// qcpl11  ???????????????????????????????????????????????????????????????
	// qcpl21  ???????????????????????????????????????????????????????????????
	// qcci11  ???????????????????????????????????????????????????????????????
	// qcci21  ???????????????????????????????????????????????????????????????
	// qccf01  ???????????????????????????????????????????????????
	
	// ???????????????????????????[????????????]???????????????
	// qnbs02  ????????????????????????
	// qnpl12  ???????????????????????????????????????????????????
	// qnpl22  ???????????????????????????????????????????????????
	// qncf02  ?????????????????????????????????????????????
	
	// ???????????????????????????[IFRS]???????????? ????????????????????????????????????
	// qcfs03  ?????????????????????????????????????????? ?????? ?????? qcfs03
	// qcci13  ??????????????????????????????????????????????????????????????????????????? ?????? ?????? qcci13
	// qcci23  ??????????????????????????????????????????????????????????????????????????? ?????? ?????? qcci23
	// qcss03  ?????????????????????????????????????????? ?????? ?????? qcss03
	// qccf03  ????????????????????????????????????????????????????????? ?????? ?????? ?????????????????????????????? qccf03
	
	// ???????????????????????????[IFRS]???????????????????????????????????????????????????????????????????????????????????????
	// qcfs03  ??????????????????????????????????????????
	// qcpl13  ?????????????????????????????????????????????????????????????????????
	// qcpl23  ?????????????????????????????????????????????????????????????????????
	// qcci13  ???????????????????????????????????????????????????????????????????????????
	// qcci23  ???????????????????????????????????????????????????????????????????????????
	// qcss03  ??????????????????????????????????????????
	// qccf03  ?????????????????????????????????????????????????????????
	
	// ?????????[IFRS]??????????????? ???1???????????????
	// qnfs53  ????????????????????????????????????
	// qnci63  ?????????????????????????????????????????????????????????????????????
	// qnci73  ?????????????????????????????????????????????????????????????????????
	// qnss53  ????????????????????????????????????
	// qncf53  ???????????????????????????????????????????????????
	
	// ?????????[IFRS]??????????????? ???2???????????????
	// qnfs53  ????????????????????????????????????
	// qnpl63  ???????????????????????????????????????????????????????????????
	// qnpl73  ???????????????????????????????????????????????????????????????
	// qnci63  ?????????????????????????????????????????????????????????????????????
	// qnci73  ?????????????????????????????????????????????????????????????????????
	// qnss53  ????????????????????????????????????
	// qncf53  ???????????????????????????????????????????????????
	
	// ???????????????????????????[????????????]????????????????????????????????????1????????????????????????
	// scbs05  ???????????????????????????
	// scpc05  ?????????????????????????????????????????????
	// scss05  ??????????????????????????????????????????
	// sccf05  ????????????????????????????????????????????????
	// snbs05  ?????????????????????
	// snpl05  ?????????????????????
	// snss05  ????????????????????????????????????
	// sncf05  ??????????????????????????????????????????
	
	// ???????????????????????????[????????????]????????????????????????????????????2????????????????????????
	// scbs05  ???????????????????????????
	// scpl05  ???????????????????????????
	// scci05  ?????????????????????????????????
	// scss05  ??????????????????????????????????????????
	// sccf05  ????????????????????????????????????????????????
	// snbs05  ?????????????????????
	// snpl05  ?????????????????????
	// snss05  ????????????????????????????????????
	// sncf05  ??????????????????????????????????????????
	
	// ???????????????????????????[????????????]?????????????????????????????????
	// snbs06  ?????????????????????
	// snpl06  ?????????????????????
	// snss06  ????????????????????????????????????
	// sncf06  ??????????????????????????????????????????
	
	// ???????????????????????????[IFRS]?????????????????????????????????????????????????????????????????????
	// scfs07  ???????????????????????????????????????
	// scci07  ???????????????????????????????????????
	// scss07  ???????????????????????????????????????
	// sccf07  ??????????????????????????????????????????????????????
	// snbs07  ?????????????????????
	// snpl07  ?????????????????????
	// snss07  ????????????????????????????????????
	// sncf07  ??????????????????????????????????????????
	
	// ???????????????????????????[IFRS]?????????????????????????????????????????????????????????????????????????????????????????????????????????
	// scfs07  ???????????????????????????????????????
	// scpl07  ?????????????????????????????????
	// scci07  ???????????????????????????????????????
	// scss07  ???????????????????????????????????????
	// sccf07  ??????????????????????????????????????????????????????
	// snbs07  ?????????????????????
	// snpl07  ?????????????????????
	// snss07  ????????????????????????????????????
	// sncf07  ??????????????????????????????????????????
	
	// REIT??????????????????
	// arbs01  ??????????????? ?????? ?????? arbs01
	// arpl01  ??????????????? ?????? ?????? arpl01
	// arss01  ????????????????????????????????? ?????? ?????? arss01
	// ards01  ????????????????????????????????? ?????? ?????? ards01
	// arcf01  ???????????????????????????????????? ?????? ?????? arcf01
	
	// REIT?????????????????????
	// srbs01  ?????????????????????
	// srpl01  ?????????????????????
	// srss01  ???????????????????????????????????????
	// srcf01  ??????????????????????????????????????????
	
	// ETF??????????????????
	// aebs01  ???????????????
	// aepl01  ??????????????????????????????
	
	// ETF?????????????????????
	// sebs01  ?????????????????????
	// sepl01  ????????????????????????????????????
}
