package yokwe.finance.trade2.rakuten;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.trade2.FundPriceInfoJPType;
import yokwe.util.UnexpectedException;

public class FundPriceInfoJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static Map<String, FundPriceInfoJPType> map;
	
	public static void load() {
		map = new TreeMap<>();
		StorageRakuten.FundPriceInfoJP.getList().stream().forEach(o -> map.put(o.code, o));
		logger.info("load  {}  {}", map.size(), StorageRakuten.FundPriceInfoJP.getPath());
	}
	public static void save() {
		logger.info("save  {}  {}", map.size(), StorageRakuten.FundPriceInfoJP.getPath());
		StorageRakuten.FundPriceInfoJP.save(map.values());
	}
	
	public static void add(String code, String name, TradeHistoryINVST e) {
		FundPriceInfoJPType newValue;
		{
			int amountJPY;
			{
				var string = e.amountJPY.replace(",", "");
				int i = string.indexOf("(");
				amountJPY = Integer.valueOf(i == -1 ? string : string.substring(0, i));
			}
			var unitPrice = new BigDecimal(e.unitPrice.replace(",", ""));
			var units     = new BigDecimal(e.units.replace(",", ""));
			
			var value = unitPrice.multiply(units).divide(BigDecimal.valueOf(amountJPY), -2, RoundingMode.HALF_UP).intValue();
			newValue = new FundPriceInfoJPType(code, name, value);
		}
		var oldValue = map.get(code);
		if (oldValue == null) {
			map.put(newValue.code, newValue);
		} else {
			if (newValue.equals(oldValue)) {
				// expected
			} else {
				// not expected
				logger.error("Unexpeced value");
				logger.error("  code      {}", oldValue.code);
				logger.error("  name      {}", oldValue.name);
				logger.error("  oldValue  {}", oldValue.value);
				logger.error("  newValue  {}", newValue.value);
				throw new UnexpectedException("Unexpeced value");
			}
		}
	}
}
