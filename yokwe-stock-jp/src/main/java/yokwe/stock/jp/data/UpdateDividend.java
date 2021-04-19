package yokwe.stock.jp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.stock.jp.jpx.Stock;

public class UpdateDividend {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateDividend.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, List<Dividend>> map = new TreeMap<>();
		{
			List<DividendStock> list = DividendStock.load();
			logger.info("DividendStock {}", String.format("%5d", list.size()));
			for(DividendStock e: list) {
				String stockCode = e.stockCode;
				
				List<Dividend> dividendList;
				if (map.containsKey(stockCode)) {
					dividendList = map.get(stockCode);
				} else {
					dividendList = new ArrayList<>();
					map.put(stockCode, dividendList);
				}
				dividendList.add(new Dividend(e.date, e.stockCode, e.dividend));
			}
		}
		{
			List<DividendREIT> list = DividendREIT.load();
			logger.info("DividendREIT  {}", String.format("%5d", list.size()));
			for(DividendREIT e: list) {
				String stockCode = e.stockCode;
				
				List<Dividend> dividendList;
				if (map.containsKey(stockCode)) {
					dividendList = map.get(stockCode);
				} else {
					dividendList = new ArrayList<>();
					map.put(stockCode, dividendList);
				}
				dividendList.add(new Dividend(e.date, e.stockCode, e.dividend));
			}
		}
		{
			List<DividendETF> list = DividendETF.getList();
			logger.info("DividendETF   {}", String.format("%5d", list.size()));
			for(DividendETF e: list) {
				if (!e.currency.equals("JPY")) continue;
				
				String stockCode = e.stockCode;
				
				List<Dividend> dividendList;
				if (map.containsKey(stockCode)) {
					dividendList = map.get(stockCode);
				} else {
					dividendList = new ArrayList<>();
					map.put(stockCode, dividendList);
				}
				dividendList.add(new Dividend(e.date, e.stockCode, (e.dividend / e.unit)));
			}
		}
		logger.info("map           {}", String.format("%5d", map.size()));

		{
			Map<String, Stock> stockMap = Stock.getMap();
			logger.info("stockMap      {}", String.format("%5d", stockMap.size()));
			List<String> delist = new ArrayList<>();
			int countSave   = 0;
			int countDelist = 0;
			for(Map.Entry<String, List<Dividend>> entry: map.entrySet()) {
				String         stockCode = entry.getKey();
				List<Dividend> list      = entry.getValue();
				
				// Save dividend of stockCode that appeared in stockCodeSet
				if (stockMap.containsKey(stockCode)) {
					Dividend.save(stockCode, list);
					countSave++;
				} else {
					delist.add(stockCode);
					countDelist++;
				}
			}
			logger.info("countSave     {}", String.format("%5d", countSave));
			logger.info("countDelist   {}", String.format("%5d", countDelist));

			logger.info("delist        {}", delist);
		}
		
		logger.info("STOP");
	}
}
