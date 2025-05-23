package yokwe.finance.account.nikko;

import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

public class BalancePage {
	// MRF・お預り金予定残高
	public static class MRFInfo {
		public static final Pattern PAT = Pattern.compile(
			"<span .+?>※本日の残高は.+?<span .+?>\\s+" +
			"(?<value>[0-9,]+)</span>&nbsp;円&nbsp;です。\\s+" +
			"</span>"
		);
		public static MRFInfo getInstance(String page) {
			return ScrapeUtil.get(MRFInfo.class, PAT, page);
		}
		
		@ScrapeUtil.AsNumber
		public final int value;
		
		public MRFInfo(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("%d", value);
		}
	}
	
	
	// 国内投資信託
	public static class FundInfo {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><span .+?><a href=\"http://fund2\\.smbcnikko\\.co\\.jp.+?;KEY1=(?<fundCode>.+?)\".+?>(?<fundName>.+?)</a>（.+?）\\s+" +
			"<div .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?><span .+?>(?<accountType>.+?)</span></td>\\s+" +
			"<td .+?><span .+?>(?<units>.+?)</span></td>\\s+" +
			"<td .+?><span .+?>(?<unitPrice>.+?)<br>\\s+<a .+?>(?<unitCost>.+?)</a>\\s+</span></td>\\s+" +
			"<td .+?><span .+?>(?<value>.+?)<br><span .+?>(?<returns>.+?)</span></span></td>\\s+" +
			"</tr>\\s+" +
			"",
			Pattern.DOTALL
		);
		public static List<FundInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundInfo.class, PAT, page);
		}
		
		public final String fundCode;
		public final String fundName;
		public final String accountType;
		@ScrapeUtil.AsNumber
		public final String units;
		@ScrapeUtil.AsNumber
		public final String unitPrice;
		@ScrapeUtil.AsNumber
		public final String unitCost;
		@ScrapeUtil.AsNumber
		public final String value;
		@ScrapeUtil.AsNumber
		public final String returns;
		
		public FundInfo(String fundCode, String fundName, String accountType, String units, String unitPrice, String unitCost, String value, String returns) {
			this.fundCode    = fundCode;
			this.fundName    = fundName;
			this.accountType = accountType;
			this.units       = units;
			this.unitPrice   = unitPrice;
			this.unitCost    = unitCost;
			this.value       = value;
			this.returns     = returns;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	
	// 外国株式
	public static class ForeignStockInfo {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><span .+?>\\s+<a href=\"/InvestmentInformation/.+?>(?<stockName>.+?)</a>\\s+<br>\\s+(?<stockCode>.+?)\\(.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?><span .+?>(?<accountType>.+?)</span></td>\\s+" +
			"<td .+?><span .+?>(?<units>.+?)<br>(?<currency>.+?)</span></td>\\s+" +
			"<td .+?>\\s+<span .+?>(?<price>.+?)\\(.+?<br>(?<fxRate>.+?)</span>\\s+</td>\\s+" +
			"<td .+?>\\s+(?<unitCostJPY>.+?)<br>\\s+<span>(?<costJPY>.+?)</span>\\s+</span></td>\\s+" +
			"<td .+?><span .+?>(?<valueJPY>.+?)<br>\\s+<span .+?>(?<retrunsJPY>.+?)</span></span></td>\\s+" +
			"</tr>" +
			"",
			Pattern.DOTALL
		);
		public static List<ForeignStockInfo> getInstance(String page) {
			return ScrapeUtil.getList(ForeignStockInfo.class, PAT, page);
		}
		
		public final String stockName;
		public final String stockCode;
		public final String accountType;
		@ScrapeUtil.AsNumber
		public final String units;
		public final String currency;
		@ScrapeUtil.AsNumber
		public final String price;
		@ScrapeUtil.AsNumber
		public final String fxRate;
		@ScrapeUtil.AsNumber
		public final String unitCostJPY;
		@ScrapeUtil.AsNumber
		public final String costJPY;
		@ScrapeUtil.AsNumber
		public final String valueJPY;
		@ScrapeUtil.AsNumber
		public final String retrunsJPY;
		
		public ForeignStockInfo(
			String stockName, String stockCode, String accountType, String units, String currency,
			String price, String fxRate, String unitCostJPY, String costJPY, String valueJPY, String retrunsJPY) {
			this.stockName   = stockName;
			this.stockCode   = stockCode;
			this.accountType = accountType;
			this.units       = units;
			this.currency    = currency;
			this.price       = price;
			this.fxRate      = fxRate;
			this.unitCostJPY = unitCostJPY;
			this.costJPY     = costJPY;
			this.valueJPY    = valueJPY;
			this.retrunsJPY  = retrunsJPY;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	
	// 外貨建てMMF
	public static class ForeignMMFInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td .+?><span .+?>(?<name>[^\\s]+)\\s+（(?<currency>...)）<br><div .+?>　</div></span></td>\\s+" +
				"<td .+?>.+?<a href=\"/FundOrderConfirmation/.+?</a>.+?</td>\\s+" +
				"<td .+?><span .+?>(?<accountType>.+?)</span></td>\\s+" +			
				"<td .+?><span .+?>(?<units>.+?)</span></td>\\s+" +
				"<td .+?><span .+?>\\s+(?<value>.+?)\\s+<br>\\s+(?<unitPrice>.+?)\\s+</span></td>\\s+" +
				"<td .+?><span .+?>\\s+(?<fxRate>.+?)\\s+<br>(?<fxRateCost>.+?)\\s+</span></td>\\s+" +
				"<td .+?><span .+?>\\s+(?<valueJPY>.+?)\\s+<br>\\s+<span .+?>\\s+(?<retrunsJPY>.+?)\\s+</span>\\s+</span></td>\\s+" +
				"</tr>" +
				"",
				Pattern.DOTALL
			);
			public static List<ForeignMMFInfo> getInstance(String page) {
				return ScrapeUtil.getList(ForeignMMFInfo.class, PAT, page);
			}
			
			public final String name;
			public final String currency;
			public final String accountType;
			@ScrapeUtil.AsNumber
			public final String units;
			@ScrapeUtil.AsNumber
			public final String value;
			public final String unitPrice;
			public final String fxRate;
			public final String fxRateCost;
			
			public ForeignMMFInfo(
				String name, String currency, String accountType, String units, String value, String unitPrice,
				String fxRate, String fxRateCost) {
				this.name        = name;
				this.currency    = currency;
				this.accountType = accountType;
				this.units       = units;
				this.value       = value;
				this.unitPrice   = unitPrice;
				this.fxRate      = fxRate;
				this.fxRateCost  = fxRateCost;
			}
			
			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
	}
	
	
	// 外国債券＜転換社債含む＞s
	public static class ForeignBondInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td colspan=\"3\" .+?><span .+?>(?<name>[^（]+)（(?<code>[0-9]{4} - [0-9]{4})）</span></td>\\s+" +
				"<td .+?><span .+?>(?<accountType>.+?)</span></td>\\s+" +			
				"<td .+?><span .+?>(?<units>.+?)<br>(?<currency>[A-Z]{3})</span></td>\\s+" +
				"<td .+?><span .+?>(?<price>.+?)<br>(?<fxRate>.+?)</span></td>\\s+" +
				"<td .+?><span .+?>(?<valueJPY>.+?)<br><span .+?>(?<returnsJPY>.+?)</span></span></td></tr>\\s+" +
				"<tr>\\s+" +
				"<td .+?><span .+?>(?<redemptionDate>.+?)</span></td>\\s+" +
				"<td .+?><span .+?>(?<nextDivDate>.+?)</span></td>\\s+" +
				"<td .+?><span .+?>(?<interestRate>.+?)%</span></td></tr>\\s+" +
				"",
				Pattern.DOTALL
			);
			public static List<ForeignBondInfo> getInstance(String page) {
				return ScrapeUtil.getList(ForeignBondInfo.class, PAT, page);
			}
			
			public final String name;
			public final String code;
			public final String accountType;
			@ScrapeUtil.AsNumber
			public final String units;
			public final String currency;
			@ScrapeUtil.AsNumber
			public final String price;
			public final String fxRate;
			@ScrapeUtil.AsNumber
			public final String valueJPY;
			@ScrapeUtil.AsNumber
			public final String returnsJPY;
			public final String redemptionDate;
			public final String nextDivDate;
			public final String interestRate;
			
			public ForeignBondInfo(
					String name, String code, String accountType, String units, String currency,
					String price, String fxRate, String valueJPY, String returnsJPY,
					String redemptionDate, String nextDivDate, String interestRate) {
					this.name           = name;
					this.code           = code;
					this.accountType    = accountType;
					this.units          = units;
					this.currency       = currency;
					this.price          = price;
					this.fxRate         = fxRate;
					this.valueJPY       = valueJPY;
					this.returnsJPY     = returnsJPY;
					this.redemptionDate = redemptionDate;
					this.nextDivDate    = nextDivDate;
					this.interestRate   = interestRate;
				}
			
			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
	}

}
