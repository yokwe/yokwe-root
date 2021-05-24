package yokwe.stock.jp.xbrl.tdnet.inline;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;

import yokwe.stock.jp.xbrl.XML;
import yokwe.util.UnexpectedException;
import yokwe.util.xml.Attribute;
import yokwe.util.xml.Element;
import yokwe.util.xml.QValue;

public class NumberValue extends InlineXBRL {
	public static Set<QValue> validAttributeSet = new TreeSet<>();
	static {
		validAttributeSet.add(new QValue("", "contextRef"));
		validAttributeSet.add(new QValue("", "name"));
		validAttributeSet.add(new QValue("", "format"));
		validAttributeSet.add(new QValue("", "unitRef"));
		validAttributeSet.add(new QValue("", "decimals"));
		validAttributeSet.add(new QValue("", "scale"));
		//
		validAttributeSet.add(new QValue("", "sign"));
		validAttributeSet.add(XML.XSI_NIL);
	}

	// ixt:numdotdecimal は 999,000.00
	public final String     unitRef;
	public final int        decimals; // 値の精度情報　3 => 0.001刻み  0 => 1刻み  -3 => 1,000刻み
	public final int        scale;    // 値の意味　6 => 1の値は1,000,000を意味する, -2 => 1の値は0.01を意味する
	public final boolean    isMinus;
	public final BigDecimal unscaledValue;
	public final BigDecimal numberValue;
	public final BigDecimal precision;
	
	public NumberValue(Element element) {
		super(Kind.NUMBER, element);
		this.unitRef  = element.getAttribute("unitRef");
		
		if (isNull) {
			decimals      = 0;
			scale         = 0;
			isMinus       = false;
			unscaledValue = null;
			numberValue   = null;
			precision     = null;
		} else {
			final String decimalsString = element.getAttribute("decimals");
			final String scaleString    = element.getAttribute("scale");
			final String signString     = element.getAttributeOrNull("sign");

			decimals = Integer.parseInt(decimalsString);
			scale    = Integer.parseInt(scaleString);

			if (signString == null) {
				isMinus = false;
			} else {
				switch(signString) {
				case "-":
					isMinus = true;
					break;
				default:
					logger.error("Unexpected signString {}!", signString);
					logger.error("element {}!", element);
					throw new UnexpectedException("Unexpected attribute");
				}
			}
			
			// Remove comma
			{
				String numberString = this.value;
				numberString = normalizeNumberCharacter(numberString);
				
				// ixt::numdotdecimal
				//   1,000 => 1000
				numberString = numberString.replace(",", "");
				// ixt::numunitdecimal
				//   10円00銭 => 10.00
				numberString = numberString.replace("円", ".");
				numberString = numberString.replace("銭", "");
				unscaledValue = new BigDecimal(numberString);
			}
			if (isMinus) {
				numberValue = unscaledValue.scaleByPowerOfTen(scale).negate();
			} else {
				numberValue = unscaledValue.scaleByPowerOfTen(scale);
			}
			
			precision = BigDecimal.valueOf(1, decimals);
		}
	
		// Sanity check
		for(Attribute attribute: element.attributeList) {
			QValue value = new QValue(attribute);
			if (validAttributeSet.contains(value)) continue;
			logger.error("Unexpected attribute {}!", attribute);
			logger.error("element {}!", element);
			throw new UnexpectedException("Unexpected attribute");
		}
	}
	@Override
	public String toString() {
		if (isNull) {
			return String.format("{NUMBER %s %s %s *NULL*}", name, contextSet, unitRef);
		} else {
			return String.format("{NUMBER %s %s %s %s %s}", name, contextSet, unitRef, numberValue, precision);
		}
	}
}