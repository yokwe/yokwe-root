package yokwe.security.japan.test;

import org.slf4j.LoggerFactory;

import yokwe.util.GMail;

public class T030 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T030.class);
	
   public static void main(String[] args) {
    	logger.info("START");

//    	GMail.writeAccountFile(GMail.DEFAULT_NAME, "hasegawa.yasuhiro@gmail.com", "ptkhkqgkjuahkivd", "hasegawa.yasuhiro+ubuntu-dev@gmail.com");
    	
    	GMail.sendMessage("あああ", "いいい\nううう");
        
    	logger.info("STOP");
    }

}