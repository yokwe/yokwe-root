package yokwe.stock.us.nasdaq;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Symbol;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.stock.us.nasdaq.symbolDirectory.DownloadUtil;
import yokwe.stock.us.nasdaq.symbolDirectory.NASDAQListed;
import yokwe.stock.us.nasdaq.symbolDirectory.NASDAQSymbolUtil;
import yokwe.stock.us.nasdaq.symbolDirectory.OtherListed;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateNASDAQSymbol {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateNASDAQSymbol.class);

	private static void updateNASDAQListed() {
		{
			byte[] data = DownloadUtil.download(NASDAQListed.URL);
			FileUtil.rawWrite().file(NASDAQListed.PATH_TXT_FILE, data);
			logger.info("{} {}", data.length, NASDAQListed.PATH_TXT_FILE);
		}
		
		Map<String, NASDAQListed> map = new TreeMap<>();
		{
			String string = FileUtil.read().file(NASDAQListed.PATH_TXT_FILE);
			String[] lines = string.split("\r\n");
			
			// sanity check
			{
				String firstLine = lines[0];
				if (!firstLine.equals(NASDAQListed.HEADER)) {
					logger.error("Unexpected first line");
					logger.error("  expect  {}!", NASDAQListed.HEADER);
					logger.error("  actual  {}!", firstLine);
					throw new UnexpectedException("Unexpected first line");
				}

				String lastLine = lines[lines.length - 1];
				if (!lastLine.startsWith(NASDAQListed.LAST_LINE_STARTS_WITH) || !lastLine.endsWith(NASDAQListed.LAST_LINE_ENDS_WITH)) {
					logger.error("Unexpected last line");
					logger.error("  expect  {} ... {}!", NASDAQListed.LAST_LINE_STARTS_WITH, NASDAQListed.LAST_LINE_ENDS_WITH);
					logger.error("  actual  {}!", lastLine);
					throw new UnexpectedException("Unexpected last line");
				}
			}
			for(int i = 1; i < lines.length - 1; i++) {
				String line = lines[i];
				String[] token = line.split("\\|");
				for(int j = 0; j < token.length; j++) {
					token[j] = token[j].strip();
				}
				// Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares
				String symbol          = token[0].strip();
				String name            = token[1].strip();
				String marketCategory  = token[2].strip();
				String testIssue       = token[3].strip();
				String financialStatus = token[4].strip();
				String roundLotSize    = token[5].strip();
				String etf             = token[6].strip();
				String nextShares      = token[7].strip();
				
				if (NASDAQSymbolUtil.isStock(symbol)) {
					symbol = NASDAQSymbolUtil.normalizedSymbol(symbol);
					
					NASDAQListed nasdaqListed = new NASDAQListed(symbol, name, marketCategory, testIssue, financialStatus, roundLotSize, etf, nextShares);
					if (map.containsKey(symbol)) {
						logger.warn("Duplicate symbol");
						logger.warn("  old {}", map.get(symbol));
						logger.warn("  new {}", nasdaqListed);
					} else {
						map.put(symbol, nasdaqListed);
					}
				} else {
					// logger.warn("not stock {}", symbol);
				}
			}
			// save result
			logger.info("{} {}", map.size(), NASDAQListed.getPath());
			NASDAQListed.save(map.values());
			
		}
	}
	
	private static void updateOtherListed() {
		{
			byte[] data = DownloadUtil.download(OtherListed.URL);
			FileUtil.rawWrite().file(OtherListed.PATH_TXT_FILE, data);
			logger.info("{} {}", data.length, OtherListed.PATH_TXT_FILE);
		}
		
		Map<String, OtherListed> map = new TreeMap<>();
		
		{
			String string = FileUtil.read().file(OtherListed.PATH_TXT_FILE);
			String[] lines = string.split("\r\n");
			
			// sanity check
			{
				String firstLine = lines[0];
				if (!firstLine.equals(OtherListed.HEADER)) {
					logger.error("Unexpected first line");
					logger.error("  expect  {}!", OtherListed.HEADER);
					logger.error("  actual  {}!", firstLine);
					throw new UnexpectedException("Unexpected first line");
				}

				String lastLine = lines[lines.length - 1];
				if (!lastLine.startsWith(OtherListed.LAST_LINE_STARTS_WITH) || !lastLine.endsWith(OtherListed.LAST_LINE_ENDS_WITH)) {
					logger.error("Unexpected last line");
					logger.error("  expect  {} ... {}!", OtherListed.LAST_LINE_STARTS_WITH, OtherListed.LAST_LINE_ENDS_WITH);
					logger.error("  actual  {}!", lastLine);
					throw new UnexpectedException("Unexpected last line");
				}
			}
			for(int i = 1; i < lines.length - 1; i++) {
				String line = lines[i];
				String[] token = line.split("\\|");
				for(int j = 0; j < token.length; j++) {
					token[j] = token[j].strip();
				}
				// ACT Symbol|Security Name|Exchange|CQS Symbol|ETF|Round Lot Size|Test Issue|NASDAQ Symbol
				// String actSymbol    = token[0].strip();
				String name         = token[1].strip();
				String exchange     = token[2].strip();
				// String cqsSymbol    = token[3].strip();
				String etf          = token[4].strip();
				String roundLotSize = token[5].strip();
				String testIssue    = token[6].strip();
				String nasdaqSymbol = token[7].strip();
				
				if (NASDAQSymbolUtil.isStock(nasdaqSymbol)) {
					String      symbol      = NASDAQSymbolUtil.normalizedSymbol(nasdaqSymbol);
					OtherListed otherListed = new OtherListed(symbol, name, exchange, etf, roundLotSize, testIssue);
					
					if (map.containsKey(symbol)) {
						logger.warn("Duplicate symbol");
						logger.warn("  old {}", map.get(symbol));
						logger.warn("  new {}", otherListed);
					} else {
						map.put(symbol, otherListed);
					}
				} else {
					// logger.warn("not stock {}", nasdaqSymbol);
				}
			}
			// save result
			logger.info("{} {}", map.size(), OtherListed.getPath());
			OtherListed.save(map.values());
		}
	}
	
	private static void updateNASDAQSymbol() {
		Map<String, NASDAQSymbol> map = new TreeMap<>();
		
		{
			List<NASDAQListed> nasdaqList = NASDAQListed.getList();
			
			logger.info("NASDAQListed {}", nasdaqList.size());
			for(var e: nasdaqList) {
				if (!e.testIssue.equals("N"))       continue; // skip test issue
				if (!e.financialStatus.equals("N")) continue; // skip not 
				
				String       symbol       = e.symbol;
				AssetClass   assetClass   = e.etf.equals("Y") ? AssetClass.ETF : AssetClass.STOCK;
				NASDAQSymbol nasdaqSymbol = new NASDAQSymbol(symbol, assetClass, e.name);
				
				if (symbol.length() == 5) {
					char c5 = symbol.charAt(4);
					if (c5 == 'U') continue; // UNIT
					if (c5 == 'R') continue; // RIGHTs
					if (c5 == 'W') continue; // WARRANT
				}
				
				if (map.containsKey(symbol)) {
					logger.warn("Duplicate symbol");
					logger.warn("  old {}", map.get(symbol));
					logger.warn("  new {}", nasdaqSymbol);
				} else {
					map.put(symbol, nasdaqSymbol);
				}
			}
		}
		{
			List<OtherListed> otherList = OtherListed.getList();
			
			logger.info("OtherListed  {}", otherList.size());
			for(var e: otherList) {
				if (!e.testIssue.equals("N")) continue;
				
				String       symbol       = e.symbol;
				AssetClass   assetClass   = e.etf.equals("Y") ? AssetClass.ETF : AssetClass.STOCK;
				NASDAQSymbol nasdaqSymbol = new NASDAQSymbol(symbol, assetClass, e.name);

				if (map.containsKey(symbol)) {
					logger.warn("Duplicate symbol");
					logger.warn("  old {}", map.get(symbol));
					logger.warn("  new {}", nasdaqSymbol);
				} else {
					map.put(symbol, nasdaqSymbol);
				}
			}
		}
		
		// If symbol is not in symbolList, add it.
		{
			List<Symbol> list = Symbol.getList();
			int count = 0;
			for(var e: list) {
				String symbol = e.symbol;
				
				if (map.containsKey(symbol)) {
					// OK
				} else {
					// MISSING
					logger.info("MISSING {}", symbol);
					count++;
				}
			}
			if (count != 0) logger.info("missing count {}", count);
		}
		
		logger.info("NASDAQSymbol {}  {}", map.size(), NASDAQSymbol.getPath());
		NASDAQSymbol.save(map.values());
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		updateNASDAQListed();
		updateOtherListed();
		
		updateNASDAQSymbol();
		
		logger.info("STOP");
	}
}
