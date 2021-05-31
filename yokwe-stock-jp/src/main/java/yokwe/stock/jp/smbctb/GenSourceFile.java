package yokwe.stock.jp.smbctb;

import java.io.File;

public class GenSourceFile {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GenSourceFile.class);

	public static void main(String[] args) {
    	logger.info("START");

    	GenearateJSONStub.generate("yokwe.security.japan.smbctb.json", "Screener", new File("tmp/screener.json")); // FIXME
    	GenearateJSONStub.generate("yokwe.security.japan.smbctb.json", "Security", new File("tmp/F000005MIQ.json")); // FIXME
    	GenearateJSONStub.generate("yokwe.security.japan.smbctb.json", "Price",    new File("tmp/F000000MU9-price.json")); // FIXME
    	GenearateJSONStub.generate("yokwe.security.japan.smbctb.json", "Dividend", new File("tmp/F000000MU9-div.json")); // FIXME
        
    	logger.info("STOP");
    }

}
