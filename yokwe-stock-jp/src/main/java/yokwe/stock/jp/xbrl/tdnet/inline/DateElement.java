package yokwe.stock.jp.xbrl.tdnet.inline;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.stock.jp.xbrl.XBRL;
import yokwe.stock.jp.xbrl.XML;
import yokwe.util.UnexpectedException;
import yokwe.util.xml.Attribute;
import yokwe.util.xml.Element;
import yokwe.util.xml.QValue;

public class DateElement extends BaseElement {
	public static Set<QValue> validAttributeSet = new TreeSet<>();
	static {
		validAttributeSet.add(new QValue("", "contextRef"));
		validAttributeSet.add(new QValue("", "name"));
		validAttributeSet.add(new QValue("", "format"));
		validAttributeSet.add(XML.XSI_NIL);
		//
		validAttributeSet.add(new QValue("", "escape"));
	}
	
	private static Pattern PAT_DATE_YEAR_MONTH_DAY_CJK = Pattern.compile("^(?<YY>[0-9]+)年(?<MM>[0-9]+)月(?<DD>[0-9]+)日$");
	private static LocalDate convertDateYearMonthDayCJK(String value) {
		value = normalizeNumberCharacter(value);
		
		Matcher m = PAT_DATE_YEAR_MONTH_DAY_CJK.matcher(value);
		if (m.matches()) {
			int yy = Integer.parseInt(m.group("YY"));
			int mm = Integer.parseInt(m.group("MM"));
			int dd = Integer.parseInt(m.group("DD"));
			
			LocalDate ret = LocalDate.of(yy, mm, dd);
			return ret;
		} else {
			logger.error("Unexpected content !{}!", value);
			throw new UnexpectedException("Unexpected content");
		}
	}
	private static Pattern PAT_DATE_ERA_YEAR_MONTH_DAY_JP = Pattern.compile("^(?<ERA>..)(?<YY>[0-9]+)年(?<MM>[0-9]+)月(?<DD>[0-9]+)日$");
	private static LocalDate convertDateEraYearMonthDayJP(String value) {
		value = normalizeNumberCharacter(value);
		value = value.replace("令和元年", "令和1年");

		Matcher m = PAT_DATE_ERA_YEAR_MONTH_DAY_JP.matcher(value);
		if (m.matches()) {
			int yy = Integer.parseInt(m.group("YY"));
			int mm = Integer.parseInt(m.group("MM"));
			int dd = Integer.parseInt(m.group("DD"));
			
			String era = m.group("ERA");
			switch(era) {
			case "令和":
				yy += 2018;
				break;
			case "平成":
				yy += 1988;
				break;
			default:
				logger.error("Unexpeced era {}", era);
				throw new UnexpectedException("Unexpected content");
			}

			LocalDate ret = LocalDate.of(yy, mm, dd);
			return ret;
		} else {
			logger.error("Unexpected content {}!", value);
			throw new UnexpectedException("Unexpected content");
		}
	}

	public final String    escape;
	public final LocalDate dateValue;
	
	public DateElement(Element element) {
		super(Kind.DATE, element);
		
		this.escape = element.getAttributeOrNull("escape");

		if (isNull) {
			this.dateValue = null;
		} else {
			if (qFormat.equals(XBRL.IXT_DATE_YEAR_MONTH_DAY_CJK)) {
				this.dateValue = convertDateYearMonthDayCJK(element.content);
			} else if (qFormat.equals(XBRL.IXT_DATE_ERA_YEAR_MONTH_DAY_JP)) {
				this.dateValue = convertDateEraYearMonthDayJP(element.content);
			} else {
				logger.error("Unexpected format", value);
				logger.error("  format  {}", format);
				logger.error("  qFormat {}", qFormat);
				throw new UnexpectedException("Unexpected format");
			}
		}

		// Sanity check
		for(Attribute attribute: element.attributeList) {
			QValue value = new QValue(attribute);
			if (validAttributeSet.contains(value)) continue;
			logger.error("Unexpected attribute {}", attribute.name);
			logger.error("element {}!", element);
			throw new UnexpectedException("Unexpected attribute");
		}
	}
	
	@Override
	public String toString() {
		if (isNull) {
			if (format == null) {
				return String.format("{DATE %s %s *NULL*}", name, contextSet);
			} else {
				return String.format("{DATE %s %s %s *NULL*}", name, contextSet, format);
			}
		} else {
			if (format == null) {
				return String.format("{DATE %s %s %s}", name, contextSet, dateValue);
			} else {
				return String.format("{DATE %s %s %s %s", name, contextSet, format, dateValue);
			}
		}
	}
}