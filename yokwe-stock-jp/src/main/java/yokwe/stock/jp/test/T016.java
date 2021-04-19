package yokwe.security.japan.test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.tdnet.Category;
import yokwe.security.japan.tdnet.Consolidate;
import yokwe.security.japan.tdnet.Detail;
import yokwe.security.japan.tdnet.Period;
import yokwe.security.japan.tdnet.SummaryFilename;
import yokwe.security.japan.tdnet.TDNET;

//
// Make DividendBriefReport for download file from ufocatch 
//

public class T016 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T016.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		
		Map<String, Integer> countCategory    = new TreeMap<>();
		Map<String, Integer> countConsolidate = new TreeMap<>();
		Map<String, Integer> countDetail      = new TreeMap<>();
		Map<String, Integer> countPeriod      = new TreeMap<>();
		Set<String>          tdnetCodeSet     = new TreeSet<>();
		Map<String, Integer> countIDYear      = new TreeMap<>();
		Map<String, Integer> countEDJPYear    = new TreeMap<>();
		
		Map<String, Set<String>> countTDNETCodeYear = new TreeMap<>();
		
		for(SummaryFilename e: TDNET.getFileMap().keySet()) {
			{
				Category category = e.category;
				String key = category.value;
				int count;
				if (countCategory.containsKey(key)) {
					count = countCategory.get(key);
				} else {
					count = 0;
				}
				countCategory.put(key, count + 1);
			}
			{
				Consolidate consolidate = e.consolidate;
				if (consolidate != null) {
					String key = consolidate.value;
					int count;
					if (countConsolidate.containsKey(key)) {
						count = countConsolidate.get(key);
					} else {
						count = 0;
					}
					countConsolidate.put(key, count + 1);
				}
			}
			{
				Detail detail = e.detail;
				if (detail != null) {
					String key = detail.value;
					int count;
					if (countDetail.containsKey(key)) {
						count = countDetail.get(key);
					} else {
						count = 0;
					}
					countDetail.put(key, count + 1);
				}
			}
			{
				Period period = e.period;
				if (period != null) {
					String key = period.value;
					int count;
					if (countPeriod.containsKey(key)) {
						count = countPeriod.get(key);
					} else {
						count = 0;
					}
					countPeriod.put(key, count + 1);
				}
			}
			{
				tdnetCodeSet.add(e.tdnetCode);
			}
			{
				String key = e.id.substring(0, 4);
				int count;
				if (countIDYear.containsKey(key)) {
					count = countIDYear.get(key);
				} else {
					count = 0;
				}
				countIDYear.put(key, count + 1);
			}
			{
				if (e.category == Category.EDJP) {
					String key = e.id.substring(0, 4);
					int count;
					if (countEDJPYear.containsKey(key)) {
						count = countEDJPYear.get(key);
					} else {
						count = 0;
					}
					countEDJPYear.put(key, count + 1);
				}
			}
			{
				if (e.category == Category.EDJP) {
					String key   = e.id.substring(0, 4);
					String value = e.tdnetCode;
					Set<String> set;
					if (countTDNETCodeYear.containsKey(key)) {
						set = countTDNETCodeYear.get(key);
					} else {
						set = new TreeSet<>();
						countTDNETCodeYear.put(key, set);
					}
					set.add(value);
				}
			}
		}
		
		logger.info("countCategory     {}", countCategory);
		logger.info("countConsolidate  {}", countConsolidate);
		logger.info("countDetail       {}", countDetail);
		logger.info("countPeriod       {}", countPeriod);
		logger.info("tdnetCodeSet      {}", tdnetCodeSet.size());
		logger.info("countIDYear       {}", countIDYear);
		logger.info("countEDJPYear     {}", countEDJPYear);

		{
			Map<String, Integer> countMap = new TreeMap<>();
			for(String year: countTDNETCodeYear.keySet()) {
				countMap.put(year, countTDNETCodeYear.get(year).size());
			}
			logger.info("countEDJPCodeYear {}", countMap);
		}

		logger.info("STOP");
	}
}
