package yokwe.stock.jp.smbctb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;

public final class SMBCTBFund implements Comparable<SMBCTBFund> {
	private static final String PATH_FILE = Storage.SMBCTB.getPath("smbctb-fund.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	public static List<SMBCTBFund> getList() {
		List<SMBCTBFund> list = CSVUtil.read(SMBCTBFund.class).file(getPath());
		return (list == null) ? new ArrayList<>() : list;
	}
	public static Map<String, SMBCTBFund> getMap() {
		return getList().stream().collect(Collectors.toMap(SMBCTBFund::getSecId, Function.identity()));
	}
	
	public static void save(List<SMBCTBFund> list) {
		if (list.isEmpty()) return;
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(SMBCTBFund.class).file(getPath(), list);
	}

	public enum Currency {
		AUD, EUR, JPY, NZD, USD
	}
	
    public String   secId;
    public String   isinCode;
    public Currency currency;
    public String   fundName;
    
    public SMBCTBFund() {
    	this(null, null, null, null);
    }
    public SMBCTBFund(String secId, String isinCode, Currency currency, String fundName) {
    	this.secId    = secId;
    	this.isinCode = isinCode;
    	this.currency = currency;
    	this.fundName = fundName;
    }
    
    public String getSecId() {
    	return secId;
    }
    
    @Override
    public String toString() {
    	return String.format("{%s %s %s %s}", secId, isinCode.isEmpty() ? "-" : isinCode, currency, fundName);
    }
    
	@Override
	public int compareTo(SMBCTBFund that) {
		return this.secId.compareTo(that.secId);
	}
}
