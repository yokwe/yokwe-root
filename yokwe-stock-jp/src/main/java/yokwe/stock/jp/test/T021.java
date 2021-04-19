package yokwe.security.japan.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.tdnet.Category;
import yokwe.security.japan.tdnet.SummaryFilename;
import yokwe.security.japan.tdnet.TDNET;
import yokwe.security.japan.xbrl.inline.Document;
import yokwe.security.japan.xbrl.report.StockReport;
import yokwe.util.CSVUtil;

public class T021 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T021.class);
	
	public static void a001() {
		// Output occurrence of category
		Map<Category, List<SummaryFilename>> categoryMap = new TreeMap<>();
		Map<SummaryFilename, File> fileMap = TDNET.getFileMap();
		for(SummaryFilename e: fileMap.keySet()) {
			Category category = e.category;
			List<SummaryFilename> list;
			if (categoryMap.containsKey(category)) {
				list = categoryMap.get(category);
			} else {
				list = new ArrayList<>();
				categoryMap.put(category, list);
			}
			list.add(e);
		}
		
		for(Map.Entry<Category, List<SummaryFilename>> entry: categoryMap.entrySet()) {
			logger.info("{}  {}  {}", entry.getKey().value,String.format("%5d", entry.getValue().size()), entry.getKey());
		}
	}
	public static void a002() {
		Map<String, Map<String, SummaryFilename>> map = new TreeMap<>();
		//  key is stockCode
		//              key is consolidate category detail
		
		Map<SummaryFilename, File> fileMap = TDNET.getFileMap();
		for(SummaryFilename e: fileMap.keySet()) {
			Category category = e.category;
			if (category == Category.EDIF || category == Category.EDJP || category == Category.EDUS) {
				String stockCode = e.tdnetCode;
				String key       = String.format("%s-%s",
						e.consolidate == null ? "" : e.consolidate,
						e.category    == null ? "" : e.category);
				
				Map<String, SummaryFilename> map2;
				if (map.containsKey(stockCode)) {
					map2 = map.get(stockCode);
				} else {
					map2 = new TreeMap<>();
					map.put(stockCode, map2);
				}
				if (map2.containsKey(key)) {
					//
				} else {
					map2.put(key, e);
				}
			}
		}
		
		for(String stockCode: map.keySet()) {
			Map<String, SummaryFilename> map2 = map.get(stockCode);
			if (1 < map2.size()) {
				logger.info("{}  {}", stockCode, map2);
			}
		}
	}
	
	public static class FilenameInfo implements Comparable<FilenameInfo> {
		public static final String FILE_PATH = "tmp/filenameInfo.csv";
		
		public String          stockCode;
		public String          yearEnd;
		public Integer         quarter;
		public SummaryFilename filename;
		
		public FilenameInfo(String stockCode, String yearEnd, Integer quarter, SummaryFilename filename) {
			this.stockCode = stockCode;
			this.yearEnd   = yearEnd;
			this.quarter   = quarter;
			this.filename  = filename;
		}
		public FilenameInfo() {
			this(null, null, null, null);
		}
		
		@Override
		public int compareTo(FilenameInfo that) {
			int ret = this.stockCode.compareTo(that.stockCode);
			if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
			if (ret == 0) ret = this.quarter.compareTo(that.quarter);
			if (ret == 0) ret = this.filename.compareTo(that.filename);
			return ret;
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s %s %s}", stockCode, yearEnd, quarter, filename);
		}
		
		public String getKind() {
			return String.format("%s%s%s%s",
					filename.period      == null ? "" : filename.period.value,
					filename.consolidate == null ? "" : filename.consolidate.value,
					filename.category    == null ? "" : filename.category.value,
					filename.detail      == null ? "" : filename.detail.value);
		}
	}
	
	public static void buildFilenameInfo() {
		List<FilenameInfo> list = new ArrayList<>();
		
		{
			Map<SummaryFilename, File> fileMap = TDNET.getFileMap().entrySet().stream().
					filter(o -> o.getKey().category == Category.EDJP || o.getKey().category == Category.EDIF || o.getKey().category == Category.EDUS).
					collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			logger.info("fileMap {}", fileMap.size());
			
			int count = 0;
			for(Map.Entry<SummaryFilename, File> entry: fileMap.entrySet()) {
				SummaryFilename key  = entry.getKey();
				File            file = entry.getValue();
				if ((count % 1000) == 0) {
					logger.info("{} {}", String.format("%5d / %5d", count, fileMap.size()), key);
				}
				count++;
								
				Document document = Document.getInstance(file);
				StockReport value = StockReport.getInstance(document);

				final String          stockCode = value.stockCode;
				final String          yearEnd   = value.yearEnd;
				final Integer         quarter   = value.quarterlyPeriod;
				final SummaryFilename filename  = value.filename;
				
				{
					FilenameInfo filenameInfo = new FilenameInfo(stockCode, yearEnd, quarter, filename);
					list.add(filenameInfo);
				}
			}
		}
		{
			logger.info("list {}", list.size());
			Collections.sort(list);
			CSVUtil.write(FilenameInfo.class).file(FilenameInfo.FILE_PATH, list);
		}
	}
	
	public static void processFilenameInfo() {
		Map<String, List<FilenameInfo>> map = new TreeMap<>();
		
		{
			List<FilenameInfo> list = CSVUtil.read(FilenameInfo.class).file(FilenameInfo.FILE_PATH);
			for(FilenameInfo e: list) {
				SummaryFilename filename = e.filename;
				if (filename.category == Category.EDIF || filename.category == Category.EDJP || filename.category == Category.EDUS || filename.category == Category.EDUS) {
					String key = String.format("%s %s %s", e.stockCode, e.yearEnd, e.quarter);
					List<FilenameInfo> list2;
					if (map.containsKey(key)) {
						list2 = map.get(key);
					} else {
						list2 = new ArrayList<>();
						map.put(key, list2);
					}
					list2.add(e);
				}
			}
		}
		
		for(Map.Entry<String, List<FilenameInfo>> entry: map.entrySet()) {
			List<FilenameInfo> value = entry.getValue();
			
			if (1 < value.size()) {
//				logger.info("{}  {}  {}", key, value.size(), value);
				
				FilenameInfo info0 = value.get(0);
				String       kind0  = info0.getKind();
				
				List<FilenameInfo> list = new ArrayList<>();
				list.add(info0);
				
				for(int i = 0; i < value.size(); i++) {
					FilenameInfo info = value.get(i);
					String       kind = info.getKind();
					
					if (kind.equals(kind0)) continue;
					list.add(info);
				}
				if (1 < list.size()) {
					logger.info("{}", list);
				}
				
			}
		}

	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		processFilenameInfo();
		
		logger.info("STOP");
	}
}
