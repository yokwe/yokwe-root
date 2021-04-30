package yokwe.stock.jp.toushin;

import java.util.List;

import org.slf4j.LoggerFactory;

public class UpdateFile {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateFile.class);

	public static void main(String[] args) {
		logger.info("START");
		
		UpdatePage.update();
		UpdatePrice.update();
		UpdateSeller.update();
		
		logger.info("update countPrice and countSeller");
		List<MutualFund> list = MutualFund.load();
		for(var e: list) {
			List<Price>	priceList = Price.load(e.isinCode);
			List<Seller> sellerList = Seller.load(e.isinCode);
			
			e.countPrice = priceList.size();
			e.countSeller = sellerList.size();
		}
		MutualFund.save(list);
		
		logger.info("STOP");
	}
}
