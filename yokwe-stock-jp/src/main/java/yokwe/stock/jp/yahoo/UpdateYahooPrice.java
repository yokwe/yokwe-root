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
import yokwe.util.MarketHoliday;

public class UpdateYahooPrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final long UPDATE_DURATION_IN_DAY  =   10;
	private static final long GRACE_PERIOD_IN_HOUR    =    4;
	private static final long SLEEP_DURATION_IN_MILLI = 1000;
	
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
			
			final File    file    = new File(YahooPrice.getPath(stockCode));
			final boolean noFile  = !file.exists();
			
			String stockCodeYahoo;
			{
				if (Stock.isPreferredStock(stockCode)) {
					stockCodeYahoo = stockCode.substring(0, 4) + "P.T";
				} else {
					stockCodeYahoo = stockCode.substring(0, 4) + ".T";
				}
			}
			
			var newPriceList = yokwe.util.yahoo.finance.Download.getPrice(stockCodeYahoo, (noFile ? PERIOD_0 : PERIOD_1), PERIOD_2);
			if (newPriceList == null) {
//				logger.warn("no data  {}", stockCodeYahoo);
				newPriceList = new ArrayList<>();
			}
			
			// merger old and new
			var oldPriceMap  = YahooPrice.getList(stockCode).stream().collect(Collectors.toMap(o -> o.date, Function.identity()));

			int countMod = 0;
			for(var newPrice: newPriceList) {
				var key = newPrice.date;
				
				newPrice.open     = newPrice.open.setScale(1, RoundingMode.HALF_EVEN);
				newPrice.high     = newPrice.high.setScale(1, RoundingMode.HALF_EVEN);
				newPrice.low      = newPrice.low.setScale(1, RoundingMode.HALF_EVEN);
				newPrice.close    = newPrice.close.setScale(1, RoundingMode.HALF_EVEN);
				
				if (oldPriceMap.containsKey(key)) {
					var oldPrice = oldPriceMap.get(key);
					if (newPrice.equals(oldPrice)) {
						// same value
					} else {
						// different value
						logger.warn("diff   {}  {}", stockCode, key);
						logger.warn("  new  {}", newPrice);
						logger.warn("  old  {}", oldPrice);
						oldPriceMap.put(key, newPrice);
						countMod++;
					}
				} else {
					oldPriceMap.put(key, newPrice);
					countMod++;
				}
			}
			
			if (0 < countMod || noFile) {
				var values = oldPriceMap.values();
//				logger.info("save  {}  {}", values.size(), YahooPrice.getPath(stockCode));
				YahooPrice.save(stockCode, values);
			} else {
				FileUtil.touch(file);
			}
		}
	}
	private static void updateFile() {
		// Category A  -- no file
		// Category B  -- file contains target range
		// Category C  -- not recently modified
		// Category D  -- recently modified
		final Instant now = Instant.now();

		List<String> listA = new ArrayList<>();
		List<String> listB = new ArrayList<>();
		List<String> listC = new ArrayList<>();
		List<String> listD = new ArrayList<>();
		
		final LocalDate dateFirst;
		final LocalDate dateLast;
		{
			TreeSet<LocalDate> dateSet = new TreeSet<>();
			for(LocalDate date = PERIOD_1; !date.isAfter(PERIOD_2); date = date.plusDays(1)) {
				if (MarketHoliday.JP.isClosed(date)) continue;
				dateSet.add(date);
			}
			dateFirst = dateSet.first();
			dateLast  = dateSet.last();
		}

		for(var stockCode: Stock.getList().stream().map(o -> o.stockCode).collect(Collectors.toList())) {
			File file = new File(YahooPrice.getPath(stockCode));
			
			if (file.exists()) {
				var dateSet = YahooPrice.getList(stockCode).stream().map(o -> o.date).collect(Collectors.toSet());
				if (dateSet.contains(dateFirst) && dateSet.contains(dateLast)) {
					listB.add(stockCode);
				} else {
					long hours;
					{
						Instant  modTime = Instant.ofEpochMilli(file.lastModified());
						Duration duration = Duration.between(modTime, now);
						hours = duration.toHours();
					}
					if (hours < GRACE_PERIOD_IN_HOUR) {
						// recently modified
						listD.add(stockCode);
					} else {
						// not recently modified
						listC.add(stockCode);
					}
				}
			} else {
				listA.add(stockCode);
			}
		}
		
		logger.info("no file           {}", listA.size());
		logger.info("contains data     {}", listB.size());
		logger.info("needs update      {}", listC.size());
		logger.info("recently modified {}", listD.size());
		
		updateList("no file           ", listA);
//		updateList("contains data     ", listB);
		updateList("needs update      ", listC);
//		updateList("recently modified ", listD);

	}
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = new TreeSet<>();
		for(var e: Stock.getList()) {
			File file = new File(YahooPrice.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		// price
		FileUtil.moveUnknownFile(validNameSet, YahooPrice.getPath(), YahooPrice.getPathDelist());
	}

	public static void main(String[] args) {
		logger.info("START");
		
		logger.info("date  {}  --  {}", PERIOD_1, PERIOD_2);
		
		updateFile();
		moveUnknownFile();
		
		logger.info("STOP");
	}
}
