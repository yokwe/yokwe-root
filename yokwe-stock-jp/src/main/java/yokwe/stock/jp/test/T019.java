package yokwe.security.japan.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.data.Dividend;
import yokwe.security.japan.data.Price;
import yokwe.security.japan.data.StockInfo;
import yokwe.security.japan.fsa.EDINET;
import yokwe.security.japan.fsa.Fund;
import yokwe.security.japan.jpx.Stock;
import yokwe.security.japan.tdnet.SummaryFilename;
import yokwe.security.japan.xbrl.report.REITReport;
import yokwe.security.japan.xbrl.report.StockReport;

public class T019 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T019.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		{
			List<Stock> list = Stock.getList();
			Set<String> set = new TreeSet<>();
			for(Stock e: list) {
				if (e.stockCode.isEmpty()) continue;
				set.add(e.stockCode);
			}
			logger.info("Stock       {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			List<Stock> list = Stock.getList();
			Set<String> set = new TreeSet<>();
			for(Stock e: list) {
				if (e.stockCode.isEmpty()) continue;
				if (!e.isETF()) continue;
				set.add(e.stockCode);
			}
			logger.info("Stock ETF   {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			List<Stock> list = Stock.getList();
			Set<String> set = new TreeSet<>();
			for(Stock e: list) {
				if (e.stockCode.isEmpty()) continue;
				if (!e.isREIT()) continue;
				set.add(e.stockCode);
			}
			logger.info("Stock REIT  {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			List<Stock> list = Stock.getList();
			Set<String> set = new TreeSet<>();
			for(Stock e: list) {
				if (e.stockCode.isEmpty()) continue;
				if (e.isETF()) continue;
				if (e.isREIT()) continue;
				set.add(e.stockCode);
			}
			logger.info("Stock STOCK {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			List<StockInfo> list = StockInfo.getList();
			Set<String> set = new TreeSet<>();
			for(StockInfo e: list) {
				if (e.stockCode.isEmpty()) continue;
				set.add(e.stockCode);
			}
			logger.info("StockInfo   {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			List<EDINET> list = EDINET.load();
			Set<String> set = new TreeSet<>();
			for(EDINET e: list) {
				if (e.stockCode.isEmpty()) continue;
				set.add(e.stockCode);
			}
			logger.info("EDIENT      {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			List<Fund> list = Fund.load();
			Set<String> set = new TreeSet<>();
			for(Fund e: list) {
				if (e.stockCode.isEmpty()) continue;
				set.add(e.stockCode);
			}
			logger.info("Fund        {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			List<String> list = Arrays.asList(new File(Price.PATH_DIR_DATA).list()).stream().collect(Collectors.toList());
			
			Set<String> set = new TreeSet<>();
			for(String e: list) {
				if (e.equals(".")) continue;
				if (e.equals("..")) continue;
				set.add(e);
			}
			logger.info("Price       {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			List<String> list = Arrays.asList(new File(Dividend.PATH_DIR_DATA).list()).stream().collect(Collectors.toList());
			
			Set<String> set = new TreeSet<>();
			for(String e: list) {
				set.add(e);
			}
			logger.info("Dividend    {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		
		{
			Map<SummaryFilename, StockReport> reportMap = StockReport.getMap();
			List<String> list = reportMap.keySet().stream().map(o -> o.tdnetCode).collect(Collectors.toList());

			Set<String> set = new TreeSet<>();
			for(String e: list) {
				set.add(e);
			}
			logger.info("StockReport {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		{
			Map<SummaryFilename, REITReport> reportMap = REITReport.getMap();
			List<String> list = reportMap.keySet().stream().map(o -> o.tdnetCode).collect(Collectors.toList());

			Set<String> set = new TreeSet<>();
			for(String e: list) {
				set.add(e);
			}
			logger.info("REITReport  {}  {}", String.format("%4d", set.size()), set.iterator().next());
		}
		logger.info("STOP");
	}
}
