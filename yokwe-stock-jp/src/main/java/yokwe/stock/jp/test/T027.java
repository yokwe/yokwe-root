package yokwe.security.japan.test;

import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.data.StockPrice;

public class T027 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T027.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		{
			List<StockPrice> list = StockPrice.getList();
			logger.info("list {}", list.size());
		}
	
		
		logger.info("STOP");
	}
}
