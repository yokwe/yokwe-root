package yokwe.stock.jp.nikkei;

public class Fund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// QPCOMMON01
    public String code;
    // QPCOMMON02
    public String name;
    // QPFDINFO
	public String category1;
	public String category2;
	public String category3;
	public String settlementFrequency;
	public String establishmentDate;
	public String redemptionDate;
	public String salesType;
	public String fundType;
	public String initialFee;
	public String trustFee;
	// QPBASPR
	public String asOf;
	public String uamValue;
	public String divHealthValue;
	public String flowValue;
	public String returnValue;
	public String riskValue;
	public String sharpRatioValue;
	public String quickFundRiskValue;
	// QPINVPO
	public String policy;
	public String target;
    
 }
