package yokwe.stock.jp;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import yokwe.util.UnexpectedException;

public class ISINCode {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ISINCode.class);
	
	private static Map<Character, String> charToStringMap = new TreeMap<>();
	static {
		for(int i = 0; i < 10; i++) {
			charToStringMap.put((char)('0' + i), String.valueOf(i));
		}
		for(int i = 0; i < 26; i++) {
			charToStringMap.put((char)('A' + i), String.valueOf(10 + i));
		}
	}
	private static String charToString(char value) {
		if (charToStringMap.containsKey(value)) {
			return charToStringMap.get(value);
		} else {
			logger.error("Unexpected value");
			logger.error("  {} - {}!", Character.valueOf(value), value + 0);
			throw new UnexpectedException("Unexpected value");
		}
	}
	private static int sum(int[] values, int factor) {
		int ret = 0;
		for(var e: values) {
			int n = e * factor;
			ret += (n % 10) + (n / 10);
		}
		return ret;
	}

	public static void verify(String isinCode) {
		if (isinCode.length() != 12) {
			logger.error("Unexpected isinCode length");
			logger.error("  {} - {}!", isinCode.length(), isinCode);
			throw new UnexpectedException("Unexpected isinCode length");
		}

		String prefix     = isinCode.substring( 0,  2);
		String basicCode  = isinCode.substring( 2, 11);
		String checkDigit = isinCode.substring(11, 12);

		if (prefix.length() != 2) {
			logger.error("Unexpected prefix length");
			logger.error("  {} - {} - {}!", prefix, basicCode, checkDigit);
			throw new UnexpectedException("Unexpected prefix length");
		}
		if (basicCode.length() != 9) {
			logger.error("Unexpected basicCode length");
			logger.error("  {} - {} - {}!", prefix, basicCode, checkDigit);
			throw new UnexpectedException("Unexpected basicCode length");
		}
		if (checkDigit.length() != 1) {
			logger.error("Unexpected checkDigit length");
			logger.error("  {} - {} - {}!", prefix, basicCode, checkDigit);
			throw new UnexpectedException("Unexpected checkDigit length");
		}
				
		int checkDigitValue = Integer.valueOf(isinCode.substring(11, 12));
		
		String string = isinCode.substring(0, 11).chars().mapToObj(o -> charToString((char)o)).collect(Collectors.joining(""));
//		logger.info("string      {} - {}", string.length(), string);
		
		int[] values = string.chars().map(o -> o - '0').toArray();
//		logger.info("values      {} - {}", values.length, values);

		int[] oddElement;
		int[] evenElement;
		{
			AtomicInteger ai = new AtomicInteger(0);
			oddElement  = Arrays.stream(values).filter(o -> (ai.incrementAndGet() % 2) == 1).toArray();
		}
		{
			AtomicInteger ai = new AtomicInteger(0);
			evenElement = Arrays.stream(values).filter(o -> (ai.incrementAndGet() % 2) == 0).toArray();
		}
//		logger.info("oddElement  {} - {}", oddElement.length, oddElement);
//		logger.info("evenElement {} - {}", evenElement.length, evenElement);

		int checkSumValue;
		if ((values.length % 2) == 0) {
			checkSumValue = sum(oddElement, 1) + sum(evenElement, 2);
		} else {
			checkSumValue = sum(oddElement, 2) + sum(evenElement, 1);
		}
		
		checkSumValue = checkSumValue % 10;
		checkSumValue = 10 - checkSumValue;
		checkSumValue = checkSumValue % 10;
		if (checkSumValue == checkDigitValue) {
//			logger.info("{} - {} - {}", isinCode, checkSumValue, checkDigitValue);
		} else {
			logger.error("Unexpected checkDigit");
			logger.error("{} - {} - {}", isinCode, checkSumValue, checkDigitValue);
			throw new UnexpectedException("Unexpected checkDigit");
		}
	}
	
	
	private interface Builder {
		public ISINCode getInstance(String prefix, String basicCode, String checkDigit);
	}
	
	public static class JP extends ISINCode {

// See 新証券コード仕様 link in https://www.jpx.co.jp/sicc/securities-code/01.html
//   https://www.jpx.co.jp/sicc/securities-code/nlsgeu0000032d48-att/newcode_20190521.pdf
//		国コード = JP
//		基本コード
//			発行体コード(6桁)
//				属性コード(1桁):0(未定義)、1(国(国債))、2(地方公共団体)、3(公開会社等)、4(ユーザー領域)、5(外国法人)、6(未定義)、7(未定義)、8(未定義)、9(特定金融商品)
//				固有名コード(5桁)
//					国:複数の発行体とみなし、国債名称コード2桁+回号コード3桁で構成
//					地方公共団体:総務省が定めた全国地方公共団体コード(JIS X-0401,0402)から末尾のチェックデジットを除き使用
//					公開会社等:発行会社ごとに独自に割当て(基本的に、あいうえお順で付番)
//					外国法人:所属国コード3桁(ISO 3166:Numeric Code)+連番コード2桁で構成
//					特定金融商品:
//						ペーパーレスCP の場合:商品コード2桁「0A」+識別コード3桁「000～ZZZ」(I・O・Uを除く英数字)
//						未公開会社等の私募債等の場合:商品コード2桁「0B」+証券種別コード6桁(証券種類コード3桁を含む)
//						非上場投資信託の場合:商品コード2桁「0C」+識別コード6桁(証券種類コード3桁を含む)
//			証券種類コード(3桁)
//				内国株式:普通株式 000、新株式 001、第二新株式 002、新株予約権証券 009、優先株式 010、後配株式 020
//				外国株式:000から始まる連番
//				債券:通番コード、発行年コード及び発行月コード
				
		public enum IssueType {
			GOVERNMENT      ("1"),
			MUNICIPAL       ("2"),
			PUBLIC_COMPANY  ("3"),
			USER_AREA       ("4"),
			FOREIGN_COMPANY ("5"),
			FINACNE         ("9");
			
			public final String value;
			
			IssueType(String value) {
				this.value = value;
			}
			
			private static final IssueType[] VALUES = IssueType.values();
			public static IssueType getInstance(String value) {
				if (value == null || value.isEmpty()) return null;
				for(var e: VALUES) {
					if (value.equals(e.value)) return e;
				}
				logger.error("Unknown value {}!", value);
				throw new UnexpectedException("Unknown value");
			}
		}
		
		public enum StockType {
			ORDINALY    ("000"), // 普通株式			０００
			NEW_ISSUES  ("001"), // 新株式			００１
			NEW_ISSUES2 ("002"), // 第二新株式		００２
			WARRANT     ("009"), // 新株予約権証券	００９
			PREFERRED   ("010"), // 優先株式			０１０
			DEFFERED    ("020"); // 後配株式			０２０
			
			public final String value;
			
			StockType(String value) {
				this.value = value;
			}
			
			private static final StockType[] VALUES = StockType.values();
			public static StockType getInstance(String value) {
				if (value == null || value.isEmpty()) return null;
				for(var e: VALUES) {
					if (value.equals(e.value)) return e;
				}
				logger.error("Unknown value {}!", value);
				throw new UnexpectedException("Unknown value");
			}
		}

		public final IssueType issueType;
		
		private static Map<IssueType, Builder> builderMap = new TreeMap<>();
		static {
			builderMap.put(IssueType.PUBLIC_COMPANY,  ((prefix, basicCode, checkDigit) -> PublicCompany.getInstance(prefix, basicCode, checkDigit)));
			builderMap.put(IssueType.FOREIGN_COMPANY, ((prefix, basicCode, checkDigit) -> ForeignCompany.getInstance(prefix, basicCode, checkDigit)));
			builderMap.put(IssueType.FINACNE,         ((prefix, basicCode, checkDigit) -> Finance.getInstance(prefix, basicCode, checkDigit)));
		}

		private JP(String prefix, String basicCode, String checkDigit) {
			super(prefix, basicCode, checkDigit);
			
			this.issueType = IssueType.getInstance(basicCode.substring(0, 1));
		}

		public static ISINCode getInstance(String prefix, String basicCode, String checkDigit) {
			IssueType issuerType = IssueType.getInstance(basicCode.substring(0, 1));

			if (builderMap.containsKey(issuerType)) {
				return builderMap.get(issuerType).getInstance(prefix, basicCode, checkDigit);
			} else {
				logger.error("Unexpected issuerType");
				logger.error("  {} - {} - {}", prefix, basicCode, checkDigit);
				logger.error("  {}", issuerType);
				throw new UnexpectedException("Unexpected issuerType");
			}
		}
		
		public static class PublicCompany extends JP {
			// JP90C000LSS5
			// JP           - prefix
			//   3          - IssuerType.PUBLIC_COMPANY
			//    38100     - companyCode
			//         000  - StockType.ORDINALY
			//            5 - check digit
			
			public final String    companyCode;
			public final StockType stockType;

			public static PublicCompany getInstance(String prefix, String basicCode, String checkDigit) {
				return new PublicCompany(prefix, basicCode, checkDigit);
			}
			
			private PublicCompany(String prefix, String basicCode, String checkDigit) {
				super(prefix, basicCode, checkDigit);
				companyCode = basicCode.substring(1, 6);
				stockType = StockType.getInstance(basicCode.substring(6, 9));
			}
			
			@Override
			public String toString() {
				return String.format("{%s %s %s %s %s}", prefix, issueType, companyCode, stockType, checkDigit);
			}
		}
		
		public static class ForeignCompany extends JP {
			// JP5840060005
			// JP           - prefix
			//   5          - IssuerType.FOREIGN_COMPANY
			//    840       - countryCode
			//       06     - sequence
			//         000  - StockType.ORDINALY
			//            5 - check digit
			
			public final String    countryCode;
			public final String    sequence;
			public final StockType stockType;

			public static PublicCompany getInstance(String prefix, String basicCode, String checkDigit) {
				return new PublicCompany(prefix, basicCode, checkDigit);
			}
			
			private ForeignCompany(String prefix, String basicCode, String checkDigit) {
				super(prefix, basicCode, checkDigit);
				countryCode = basicCode.substring(1, 4);
				sequence    = basicCode.substring(4, 6);
				stockType   = StockType.getInstance(basicCode.substring(6, 9));
			}
			
			@Override
			public String toString() {
				return String.format("{%s %s %s %s %s %s}", prefix, issueType, countryCode, sequence, stockType, checkDigit);
			}
		}
		
		public static class Finance extends JP {
			public enum FinanceType {
				COMMERCIAL_PAPER  ("0A"), // ペーパーレスＣＰ ０Ａ
				PRIVATE_PLACEMENT ("0B"), // 未公開会社の私募債又は縁故地方公社債等 ０B
				MUTUAL_FUND       ("0C"); // 非上場投信 ０C
				
				public final String value;
				
				FinanceType(String value) {
					this.value = value;
				}
				
				private static final FinanceType[] VALUES = FinanceType.values();
				public static FinanceType getInstance(String value) {
					if (value == null || value.isEmpty()) return null;
					for(var e: VALUES) {
						if (value.equals(e.value)) return e;
					}
					logger.error("Unknown value {}!", value);
					throw new UnexpectedException("Unknown value");
				}
			}
			
			private static Map<FinanceType, Builder> builderMap = new TreeMap<>();
			static {
				builderMap.put(FinanceType.MUTUAL_FUND, ((prefix, basicCode, checkDigit) -> MutualFund.getInstance(prefix, basicCode, checkDigit)));
			}
			
			public static class MutualFund extends Finance {
				// JP90C000LSS5
				// JP           - prefix
				//   9          - IssuerType.FINANCE
				//    0C        - FunanceType.MUTUAL_FUND
				//      000LSS  - code
				//            5 - check digit
				public final String code; // 6 digit
				
				private MutualFund(String prefix, String basicCode, String checkDigit) {
					super(prefix, basicCode, checkDigit);
					
					code = basicCode.substring(3, 9);
				}
				
				public static MutualFund getInstance(String prefix, String basicCode, String checkDigit) {
					return new MutualFund(prefix, basicCode, checkDigit);
				}
				
				@Override
				public String toString() {
					return String.format("{%s %s %s %s %s}", prefix, issueType, financeType, code, checkDigit);
				}
			}
			
			public static ISINCode getInstance(String prefix, String basicCode, String checkDigit) {
				FinanceType financeType = FinanceType.getInstance(basicCode.substring(1, 3));
				if (builderMap.containsKey(financeType)) {
					return builderMap.get(financeType).getInstance(prefix, basicCode, checkDigit);
				} else {
					logger.error("Unexpected financeType");
					logger.error("  {} - {} - {}!", prefix, basicCode, checkDigit);
					logger.error("  {}", financeType);
					throw new UnexpectedException("Unexpected financeType");
				}
			}
			
			public final FinanceType financeType;
			
			private Finance(String prefix, String basicCode, String checkDigit) {
				super(prefix, basicCode, checkDigit);
				financeType = FinanceType.getInstance(basicCode.substring(1, 3));
			}
		}
	}
	
	private static Map<String, Builder> builderMap = new TreeMap<>();
	static {
		builderMap.put("JP", ((prefix, basicCode, checkDigit) -> JP.getInstance(prefix, basicCode, checkDigit)));
	}
	
	
	public static ISINCode getInstance(String isinCode) {
		verify(isinCode);
		
		String prefix     = isinCode.substring( 0,  2);
		String basicCode  = isinCode.substring( 2, 11);
		String checkDigit = isinCode.substring(11, 12);
		
		if (builderMap.containsKey(prefix)) {
			return builderMap.get(prefix).getInstance(prefix, basicCode, checkDigit);
		} else {
			logger.warn("Unexpected prefix {}", prefix);
			return new ISINCode(prefix, basicCode, checkDigit);
		}
	}

	public String prefix;     // 2 digit
	public String basicCode;  // 9 digit
	public String checkDigit; // 1 digit
	
	private ISINCode(String prefix, String basicCode, String checkDigit) {
		this.prefix     = prefix;
		this.basicCode  = basicCode;
		this.checkDigit = checkDigit;
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s %s}", prefix, basicCode, checkDigit);
	}
}
