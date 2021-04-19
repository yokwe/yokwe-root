package yokwe.security.japan.test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.data.Price;
import yokwe.security.japan.data.StockInfo;
import yokwe.security.japan.fsa.EDINET;
import yokwe.security.japan.fsa.Fund;
import yokwe.security.japan.jpx.Stock;
import yokwe.security.japan.tdnet.TDNET;
import yokwe.util.FileUtil;

public class T022 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T022.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		{
			Set<String> codeSet1 = Stock.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
			Set<String> codeSet2 = StockInfo.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
			Set<String> codeSet3 = TDNET.getFileMap().keySet().stream().map(o -> o.tdnetCode).collect(Collectors.toSet());
			Set<String> codeSet4 = EDINET.load().stream().map(o -> o.stockCode).filter(o -> !o.isEmpty()).collect(Collectors.toSet());
			Set<String> codeSet5 = EDINET.load().stream().map(o -> o.edinetCode).filter(o -> !o.isEmpty()).collect(Collectors.toSet());
			Set<String> codeSet6 = FileUtil.listFile(Price.PATH_DIR_DATA).stream().map(o -> o.getName()).filter(o -> o.endsWith(".csv")).collect(Collectors.toSet());

			Set<String> codeSet7 = Fund.load().stream().map(o -> o.edinetCode).filter(o -> !o.isEmpty()).collect(Collectors.toSet());
			Set<String> codeSet8 = Fund.load().stream().map(o -> o.fundCode).filter(o -> !o.isEmpty()).collect(Collectors.toSet());
			Set<String> codeSet9 = Fund.load().stream().map(o -> o.stockCode).filter(o -> !o.isEmpty()).collect(Collectors.toSet());
			Set<String> codeSetA = new TreeSet<>(codeSet4); codeSetA.addAll(codeSet9);
			Set<String> codeSetB = new TreeSet<>(codeSetA); codeSetB.retainAll(codeSet1);

			Set<String> codeSetC = Stock.getList().stream().filter(o -> o.isETF()).map(o -> o.stockCode).collect(Collectors.toSet());
			Set<String> codeSetD = Stock.getList().stream().filter(o -> o.isREIT()).map(o -> o.stockCode).collect(Collectors.toSet());
			Set<String> codeSetE = Stock.getList().stream().filter(o -> ((!o.isREIT()) && (!o.isETF()))).map(o -> o.stockCode).collect(Collectors.toSet());

			// NOTE Stock contains listed stock in Tokyo Stock Exchange ONLY. contains no other Stock Exchage linke Hokkaido or Nagoya
			logger.info("codeSet1 {}  Stock",             codeSet1.size());
			logger.info("codeSet2 {}  StockInfo",         codeSet2.size());
			
			// stockCode that has price
			logger.info("codeSet6 {}  Price",             codeSet6.size());

			// TDNET contains ALL Tokyo Stock Exchange in Japan
			logger.info("codeSet3 {}  TDNET  tdnetCode",  codeSet3.size());
			
			// EDINET contains company and individual that sends report to FSA (listed company, not listed company and individuals)
			logger.info("codeSet4 {}  EDINET stockCode",  codeSet4.size());
			logger.info("codeSet5 {}  EDINET edientCode", codeSet5.size());
			
			// Fund contains list of investment trust and exchange trade fund
			logger.info("codeSet7  {}  Fund edinetCode",  codeSet7.size());
			logger.info("codeSet8 {}  Fund fundCode",     codeSet8.size());
			logger.info("codeSet9  {}  Fund stockCode",    codeSet9.size());
			logger.info("codeSetA {}  Fund + EDINET stockCode",    codeSetA.size());
			logger.info("codeSetB {}  codeSetA AND codeSet1",    codeSetB.size());

			logger.info("codeSetC  {}  stock ETF",   codeSetC.size());
			logger.info("codeSetD   {}  stock REIT",  codeSetD.size());
			logger.info("codeSetE {}  stock stock", codeSetE.size());
			
			{
				int countStock = 0;
				Map<String, Stock> stockMap = Stock.getMap();
				for(String stockCode: codeSet2) {
					Stock stock = stockMap.get(stockCode);
					if (stock.isETF()) continue;
					if (stock.isREIT()) continue;
					if (stock.isPROMarket()) continue;
					if (stock.isCertificate()) continue;

					countStock++;
					if (!codeSet4.contains(stockCode)) {
						logger.info("Not in EDINET  {}  {}  {}", stock.stockCode, stock.market, stock.name);
					}
				}
				logger.info("countStock {}", countStock);
			}
		}
		
		logger.info("STOP");
	}
}
