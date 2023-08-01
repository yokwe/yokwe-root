package yokwe.stock.jp.yahoo;

import java.io.File;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.stock.jp.jpx.Stock;
import yokwe.util.FileUtil;

public class UpdateYahooDiv {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final long UPDATE_DURATION_IN_DAY  =   10;
	private static final long GRACE_PERIOD_IN_DAYS    =    7;
	private static final long SLEEP_DURATION_IN_MILLI = 1000;
	private static final long SHORT_FILE_LENGTH    =   20;
	
	private static final int DAY_ADJUST;
	static {
		ZoneId        japan = ZoneId.of("Asia/Tokyo");
		ZonedDateTime now   = ZonedDateTime.now(japan);
		DAY_ADJUST = (16 <= now.getHour()) ? 0 : 1;
	}
	private static final LocalDate PERIOD_0 = LocalDate.of(2017, 1, 1);                   // 5 years  2018 2019 2020 2021 2022 2023
	private static final LocalDate PERIOD_2 = LocalDate.now().minusDays(DAY_ADJUST);      // Do not include today
	private static final LocalDate PERIOD_1 = PERIOD_2.minusDays(UPDATE_DURATION_IN_DAY); // during UPDATE_DURATION_IN_DAYS
	
	private static void updateList(String label, List<String> stockCodeList) {
		// shuffle stockCodeList
		Collections.shuffle(stockCodeList);
		
		int count = 0;
		for(var stockCode: stockCodeList) {
			count++;
			if ((count % 10) == 1) logger.info("{}  {}  /  {}   {}", label, count, stockCodeList.size(), stockCode);
			
			// sleep before download
			try {
				Thread.sleep(SLEEP_DURATION_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
			
			final File    file    = new File(YahooDiv.getPath(stockCode));
			final boolean noFile  = !file.exists();
			
			String stockCodeYahoo;
			{
				if (Stock.isPreferredStock(stockCode)) {
					stockCodeYahoo = stockCode.substring(0, 4) + "P.T";
				} else {
					stockCodeYahoo = stockCode.substring(0, 4) + ".T";
				}
			}
			
			var newDivList = yokwe.util.yahoo.finance.Download.getDividend(stockCodeYahoo, (noFile ? PERIOD_0 : PERIOD_1), PERIOD_2);
			if (newDivList == null) {
//				logger.warn("no data  {}", stockCodeYahoo);
				newDivList = new ArrayList<>();
			}
			
			// merger old and new
			var oldDivMap  = YahooDiv.getList(stockCode).stream().collect(Collectors.toMap(o -> o.date, Function.identity()));

			int countMod = 0;
			for(var newDiv: newDivList) {
				var key = newDiv.date;
				
				newDiv.amount = newDiv.amount.setScale(2, RoundingMode.HALF_EVEN);
				
				if (oldDivMap.containsKey(key)) {
					var oldDiv = oldDivMap.get(key);
					if (newDiv.equals(oldDiv)) {
						// same value
					} else {
						// different value
						logger.warn("diff   {}  {}", stockCode, key);
						logger.warn("  new  {}", newDiv);
						logger.warn("  old  {}", oldDiv);
						oldDivMap.put(key, newDiv);
						countMod++;
					}
				} else {
					oldDivMap.put(key, newDiv);
					countMod++;
				}
			}
			
			if (0 < countMod || noFile) {
				var values = oldDivMap.values();
//				logger.info("save  {}  {}", values.size(), YahooDiv.getPath(stockCode));
				YahooDiv.save(stockCode, values);
			} else {
				FileUtil.touch(file);
			}
		}
	}
	private static void updateFile() {
		// Category A  -- no file
		// Category B  -- not recently modified and long file
		// Category C  -- not recently modified and short file
		// Category D  -- recently modified
		final Instant now = Instant.now();
		
		List<String> listA = new ArrayList<>();
		List<String> listB = new ArrayList<>();
		List<String> listC = new ArrayList<>();
		List<String> listD = new ArrayList<>();
		
		for(var stockCode: Stock.getList().stream().map(o -> o.stockCode).collect(Collectors.toList())) {
			File file = new File(YahooDiv.getPath(stockCode));
			
			if (file.exists()) {
				long days;
				{
					Instant  modTime = Instant.ofEpochMilli(file.lastModified());
					Duration duration = Duration.between(modTime, now);
					days = duration.toDays();
				}
				if (days < GRACE_PERIOD_IN_DAYS) {
					// recently modified
					listD.add(stockCode);
				} else {
					long length = file.length();
					if (length < SHORT_FILE_LENGTH) {
						// short file
						listC.add(stockCode);
					} else {
						// long file
						listB.add(stockCode);
					}
				}
			} else {
				// no file
				listA.add(stockCode);
			}
		}
		
		logger.info("no file           {}", listA.size());
		logger.info("long file         {}", listB.size());
		logger.info("short file        {}", listC.size());
		logger.info("recently modified {}", listD.size());
		
		updateList("no file            ", listA);
		updateList("long file          ", listB);
		updateList("short file         ", listC);
//		updateList("recently modified  ", listD);
	}
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = new TreeSet<>();
		for(var e: Stock.getList()) {
			File file = new File(YahooDiv.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		// price
		FileUtil.moveUnknownFile(validNameSet, YahooDiv.getPath(), YahooDiv.getPathDelist());
	}

	public static void main(String[] args) {
		logger.info("START");
		
		logger.info("date  {}  --  {}", PERIOD_1, PERIOD_2);

		updateFile();
		moveUnknownFile();
		
		logger.info("STOP");
	}

}
