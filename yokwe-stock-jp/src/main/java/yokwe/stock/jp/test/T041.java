package yokwe.security.japan.test;

import java.math.BigDecimal;

import org.slf4j.LoggerFactory;

public class T041 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T041.class);
	
   public static void main(String[] args) {
    	logger.info("START");

    	{
    		String string = "0.000";
    		BigDecimal bd = new BigDecimal(string);
    		double dv = bd.doubleValue();
    		
    		logger.info("string {}!", string);
    		logger.info("bd {}!", bd.toPlainString());
    		logger.info("dv {}!", dv);
    		
    		if (dv == 0) {
    			logger.info("dv == 0");
    		}
    		if (dv == 0.0) {
    			logger.info("dv == 0.0");
    		}
    	}
        
    	logger.info("STOP");
    }

}