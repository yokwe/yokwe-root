package yokwe.stock.jp.toushin;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.json.JSON;

public class UpdateSeller {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateSeller.class);

	public static class SellerInfo {
		public String     fdsInstCd;
		public BigDecimal salesFee;
		public String     salesInstDiv;
		public String     kanaName;
		public String     instName;
		public String     associFundCd;
		
		public SellerInfo() {
			fdsInstCd    = null;
			salesFee     = null;
			salesInstDiv = null;
			kanaName     = null;
			instName     = null;
			associFundCd = null;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	public static void update() {
		logger.info("update");

		int count = 0;
		File dir = new File(DownloadFile.getPathSeller(""));
		for(var file: dir.listFiles()) {
			if (file.isDirectory()) continue;
			String isinCode = file.getName();
			
			String string = FileUtil.read().file(file);
			
			if (string.startsWith("[")) {
				List<Seller> list = new ArrayList<>();
				for(var e: JSON.getList(SellerInfo.class, string)) {
					if (e.salesFee == null) {
						e.salesFee = BigDecimal.ZERO;
					}
					
					list.add(new Seller(isinCode, e.fdsInstCd, e.salesFee, e.instName));
				}
				
				Seller.save(isinCode, list);
				count++;
			} else {
				logger.warn("string is not JSON");
				logger.warn("string - {}", string);
			}
		}
		
		logger.info("count {}", count);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
