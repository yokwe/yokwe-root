package yokwe.stock.jp.xbrl.taxonomy;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.xml.QValue;

public class LabelData {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(LabelData.class);

	private static final Map<QValue, LabelData> all = new TreeMap<>();
	static {
		for(TSE_ED_T_LABEL e: TSE_ED_T_LABEL.values()) {
			add(e.qName, e.en, e.ja, e);
		}
		for(TSE_RE_T_LABEL e: TSE_RE_T_LABEL.values()) {
			add(e.qName, e.en, e.ja, e);
		}
	}
	
	private static void add(QValue qName, String en, String ja, Enum<?> e) {
		LabelData value = new LabelData(qName, en, ja, e);
		QValue    key   = value.qName;
		
		if (all.containsKey(key)) {
			logger.error("Duplicate key");
			logger.error("  key   {}", key);
			logger.error("  value {}", value);
			throw new UnexpectedException("Duplicate key");
		} else {
			all.put(key, value);
		}
	}
	public static LabelData get(QValue key) {
		if (all.containsKey(key)) {
			return all.get(key);
		} else {
			logger.error("Unknown key");
			logger.error("  key   {}", key);
			throw new UnexpectedException("Unknown key");
		}
	}
	
    public final QValue qName;
    public final String  en;
    public final String  ja;
    public final Enum<?> e;

    private LabelData(QValue qName, String en, String ja, Enum<?> e) {
    	this.qName = qName;
    	this.en    = en;
    	this.ja    = ja;
    	this.e     = e;
    }
}
