package yokwe.stock.us.nasdaq.symbolDirectory;

import java.util.ArrayList;
import java.util.List;

import yokwe.util.FTPUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class DownloadUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void updateNASDAQListed() {
		{
			byte[] data = FTPUtil.download(NASDAQListed.URL);
			if (data == null) {
				logger.error("Download failed  {}", NASDAQListed.URL);
				throw new UnexpectedException("Download failed");
			}
			
			FileUtil.rawWrite().file(NASDAQListed.PATH_TXT_FILE, data);
			logger.info("{} {}", data.length, NASDAQListed.PATH_TXT_FILE);
		}
		
		List<NASDAQListed> list = new ArrayList<>();
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
				
				NASDAQListed nasdaqListed = new NASDAQListed(symbol, name, marketCategory, testIssue, financialStatus, roundLotSize, etf, nextShares);
				list.add(nasdaqListed);
			}
			// save result
			logger.info("{} {}", list.size(), NASDAQListed.getPath());
			NASDAQListed.save(list);
			
		}
	}
	
	public static void updateOtherListed() {
		{
			byte[] data = FTPUtil.download(OtherListed.URL);
			if (data == null) {
				logger.error("Download failed  {}", OtherListed.URL);
				throw new UnexpectedException("Download failed");
			}

			FileUtil.rawWrite().file(OtherListed.PATH_TXT_FILE, data);
			logger.info("{} {}", data.length, OtherListed.PATH_TXT_FILE);
		}
		
		List<OtherListed> list = new ArrayList<>();
		
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
				
				String symbol       = nasdaqSymbol;
				OtherListed otherListed = new OtherListed(symbol, exchange, etf, roundLotSize, testIssue, name);
				
				list.add(otherListed);
			}
			
			// save result
			logger.info("{} {}", list.size(), OtherListed.getPath());
			OtherListed.save(list);
		}
	}
}
