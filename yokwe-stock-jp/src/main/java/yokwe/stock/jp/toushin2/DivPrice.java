package yokwe.stock.jp.toushin2;

import yokwe.util.CSVUtil;

public class DivPrice {
	// 年月日	        基準価額(円)	純資産総額（百万円）	分配金	決算期
	// 2022年04月25日	19239	        1178200	                0       4
	// 2022年04月26日	19167	        1174580   
	@CSVUtil.ColumnName("年月日")
	public String date;
	@CSVUtil.ColumnName("基準価額(円)")
	public String price;
	@CSVUtil.ColumnName("純資産総額（百万円）")
	public String nav;
	@CSVUtil.ColumnName("分配金")
	public String dividend;
	@CSVUtil.ColumnName("決算期")
	public String period;
	
	public DivPrice() {
		this.date     = null;
		this.price    = null;
		this.nav      = null;
		this.dividend = null;
		this.period   = null;
	}
}
