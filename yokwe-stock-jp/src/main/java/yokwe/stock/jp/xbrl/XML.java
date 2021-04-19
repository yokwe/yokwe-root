package yokwe.stock.jp.xbrl;

import yokwe.util.xml.QValue;

public class XML {
	// Namespace
	public static final String NS_XML   = "http://www.w3.org/XML/1998/namespace";
	public static final String NS_XLINK = "http://www.w3.org/1999/xlink";
	public static final String NS_XSD   = "http://www.w3.org/2001/XMLSchema";
	public static final String NS_XSI   = "http://www.w3.org/2001/XMLSchema-instance";

	// Value
	public static final QValue XSI_NIL = new QValue(NS_XSI, "nil");
}
