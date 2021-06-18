package yokwe.stock.jp.edinet;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.DateTimeFormat;
import yokwe.util.json.JSON.Name;

public class API {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(API.class);

	public static final String URL_BASE = "https://disclosure.edinet-fsa.go.jp/api";
	
	public static enum Version {
		V1("v1");
		
		final String value;
		
		Version(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static enum DocType {
		DOC_010("010", "有価証券通知書"),
		DOC_020("020", "変更通知書(有価証券通知書)"),
		DOC_030("030", "有価証券届出書"),
		DOC_040("040", "訂正有価証券届出書"),
		DOC_050("050", "届出の取下げ願い"),
		DOC_060("060", "発行登録通知書"),
		DOC_070("070", "変更通知書(発行登録通知書)"),
		DOC_080("080", "発行登録書"),
		DOC_090("090", "訂正発行登録書"),
		DOC_100("100", "発行登録追補書類"),
		DOC_110("110", "発行登録取下届出書"),
		ANNUAL_REPORT
		       ("120", "有価証券報告書"),
		ANNUAL_REPORT_AMENDMENT
			   ("130", "訂正有価証券報告書"),
		DOC_135("135", "確認書"),
		DOC_136("136", "訂正確認書"),
		QUARTERLY_REPORT
		       ("140", "四半期報告書"),
		QUARTERLY_REPORT_AMENDMENT
			   ("150", "訂正四半期報告書"),
		SEMI_ANNUAL_REPORT
	    	   ("160", "半期報告書"),
		SEMI_ANNUAL_REPORT_AMENDMENT
			   ("170", "訂正半期報告書"),
		DOC_180("180", "臨時報告書"),
		DOC_190("190", "訂正臨時報告書"),
		DOC_200("200", "親会社等状況報告書"),
		DOC_210("210", "訂正親会社等状況報告書"),
		DOC_220("220", "自己株券買付状況報告書"),
		DOC_230("230", "訂正自己株券買付状況報告書"),
		DOC_235("235", "内部統制報告書"),
		DOC_236("236", "訂正内部統制報告書"),
		DOC_240("240", "公開買付届出書"),
		DOC_250("250", "訂正公開買付届出書"),
		DOC_260("260", "公開買付撤回届出書"),
		DOC_270("270", "公開買付報告書"),
		DOC_280("280", "訂正公開買付報告書"),
		DOC_290("290", "意見表明報告書"),
		DOC_300("300", "訂正意見表明報告書"),
		DOC_310("310", "対質問回答報告書"),
		DOC_320("320", "訂正対質問回答報告書"),
		DOC_330("330", "別途買付け禁止の特例を受けるための申出書"),
		DOC_340("340", "訂正別途買付け禁止の特例を受けるための申出書"),
		DOC_350("350", "大量保有報告書"),
		DOC_360("360", "訂正大量保有報告書"),
		DOC_370("370", "基準日の届出書"),
		DOC_380("380", "変更の届出書");

		public final String value;
		public final String description;
		
		DocType(String value, String description) {
			this.value       = value;
			this.description = description;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static enum Status {
		OK                   ("200"),
		BAD_REQUEST          ("400"),
		NOT_FOUND            ("404"),
		INTERNAL_SERVER_ERROR("500");
		
		public final String value;
		
		Status(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
				
	public static enum Flag {
		NO  ("0"),
		YES ("1");
		
		public final String value;
		
		Flag(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
		
		public boolean toBoolean() {
			return this.equals(YES);
		}
	}

	public static enum Disclose {
		NORMAL   ("0"),
		CLOSE_1  ("1"), // 財務局職員によって書類の不開示を開始
		CLOSE_2  ("2"), // 不開示とされている書類
		DISCLOSE ("3"); // 財務局職員によって書類の不開示を解除
		
		public final String value;
		
		Disclose(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static enum Withdraw {
		NORMAL     ("0"),
		WITHDRAW_1 ("1"), // 取下書
		WITHDRAW_2 ("2"); // 取り下げられた書類
		
		public final String value;
		
		Withdraw(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static class ListDocument {
		public enum Type {
			METADATA("1"),
			DATA    ("2");
			
			final String value;
			
			Type(String value) {
				this.value = value;
			}
			
			@Override
			public String toString() {
				return value;
			}
		}

		public static String getURL(Version version, LocalDate date, Type type) {
			return String.format("%s/%s/documents.json?date=%s&type=%s", URL_BASE, version.value, date.toString(), type.value);
		}
		
		public static class Metadata {
			public static class Parameter {
				public LocalDate date;
				public Type      type;
				
				public Parameter() {
					this.date = null;
					this.type = null;
				}
			}
			public static class ResultSet {
				public int count;
				
				public ResultSet() {
					this.count = 0;
				}
			}
			
			public String        title;
			public Parameter     parameter;
			@Name("resultset")
			public ResultSet     resultSet;
			@DateTimeFormat("yyyy-MM-dd HH:mm")
			public LocalDateTime processDateTime;
			public Status        status;
			public String        message;
			
			public Metadata() {
				this.title           = null;
				this.parameter       = null;
				this.resultSet       = null;
				this.processDateTime = null;
				this.status          = null;
				this.message         = null;
			}
		}

		public static class Result {
			public int           seqNumber;
			public String        docID;
			public String        edinetCode;
			@Name("secCode")
			public String        stockCode;
			public String        JCN;
			public String        filerName;
			public String        fundCode;
			public String        ordinanceCode;
			public String        formCode;
			public DocType       docTypeCode;
			public String        periodStart;
			public String        periodEnd;
			@DateTimeFormat("yyyy-MM-dd HH:mm")
			public LocalDateTime submitDateTime;
			public String        docDescription;
			public String        issuerEdinetCode;
			public String        subjectEdinetCode;
			public String        subsidiaryEdinetCode;
			public String        currentReportReason;
			public String        parentDocID;
			public String        opeDateTime;
			public Withdraw      withdrawalStatus;
			public String        docInfoEditStatus;
			public Disclose      disclosureStatus;
			public Flag          xbrlFlag;
			public Flag          pdfFlag;
			public Flag          attachDocFlag;
			public Flag          englishDocFlag;
			
			public Result() {
		    	this.seqNumber            = 0;
		    	this.docID                = null;
		    	this.edinetCode           = null;
		    	this.stockCode            = null;
		    	this.JCN                  = null;
		    	this.filerName            = null;
				this.fundCode             = null;
				this.ordinanceCode        = null;
				this.formCode             = null;
				this.docTypeCode          = null;
				this.periodStart          = null;
				this.periodEnd            = null;
				this.submitDateTime       = null;
				this.docDescription       = null;
				this.issuerEdinetCode     = null;
				this.subjectEdinetCode    = null;
				this.subsidiaryEdinetCode = null;
				this.currentReportReason  = null;
				this.parentDocID          = null;
				this.opeDateTime          = null;
				this.withdrawalStatus     = null;
				this.docInfoEditStatus    = null;
				this.disclosureStatus     = null;
				this.xbrlFlag             = null;
				this.pdfFlag              = null;
				this.attachDocFlag        = null;
				this.englishDocFlag       = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}

		public static class Response {
			public Metadata metadata;
			public Result[] results;
			
			public Response() {
				this.metadata = null;
				this.results  = null;
			}
		}
		
		public static Response getInstance(Version version, LocalDate date, Type type) {
			String   url      = getURL(version, date, type);
			String   string   = HttpUtil.getInstance().download(url).result;
			Response response = JSON.unmarshal(Response.class, string);
			return response;
		}
		public static Response getInstance(LocalDate date, Type type) {
			return getInstance(Version.V1, date, type);
		}
	}
	
	public static class Document {
		public static final String HEADER_CONTENT_TYPE       = "Content-Type";
		public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
		public static final String CONTENT_TYPE_PDF          = "application/pdf";
		public static final String CONTENT_TYPE_JSON_A       = "application/json; charset=utf-8";
		public static final String CONTENT_TYPE_JSON_B       = "application/json;charset=utf-8";
		
		public enum Type {
			WHOLE  ("1"), // 提出本文書及び監査報告書
			PDF    ("2"), // PDF
			ATTACH ("3"), // 代替書面・添付文書
			ENGLISH("4"); // 英文ファイル
			
			final String value;
			
			Type(String value) {
				this.value = value;
			}
			
			@Override
			public String toString() {
				return value;
			}
		}
		
		// v1/documents/S1234567?type=1
		public static String getURL(Version version, String docId, Type type) {
			return String.format("%s/%s/documents/%s?type=%s", URL_BASE, version.value, docId, type.value);
		}
		
		public static byte[] getInstance(Version version, String docId, Type type) {
			String url = getURL(version, docId, type);
			HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
			
			// if download failed, return null
			if (result == null) return null;
			
			String contentType = result.headerMap.get(HEADER_CONTENT_TYPE);
			switch(contentType) {
			case CONTENT_TYPE_OCTET_STREAM:
			case CONTENT_TYPE_PDF:
				return result.rawData;
			case CONTENT_TYPE_JSON_A:
			case CONTENT_TYPE_JSON_B:
				String message = new String(result.rawData, Charset.forName("UTF-8"));
				logger.warn("Unexpected result {}", message);
				return null;
			default:
				logger.error("Unexpected contentType {}!", contentType);
				throw new UnexpectedException("Unknown contentType");
			}
		}
		public static byte[] getInstance(String docId, Type type) {
			return getInstance(Version.V1, docId, type);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		LocalDate date = LocalDate.now();
		
		for(int i = 0; i < 5; i++) {
			ListDocument.Response response = ListDocument.getInstance(date, ListDocument.Type.DATA);
//			logger.info("response {}", StringUtil.toString(response));
			for(ListDocument.Result e: response.results) {
				if (e.docTypeCode == DocType.ANNUAL_REPORT ||
						e.docTypeCode == DocType.SEMI_ANNUAL_REPORT ||
						e.docTypeCode == DocType.QUARTERLY_REPORT) {
					logger.info("{}  {}  {}  {}", e.docID, e.submitDateTime, e.docTypeCode.description, e.docDescription);
				}
			}
			date = date.minusDays(1);
		}
		
		{
			byte[] result = Document.getInstance("S100I5LU", Document.Type.WHOLE);
			if (result != null) {
				logger.info("result {}", result.length);
			} else {
				logger.info("result null");
			}
		}
		
		logger.info("STOP");
	}
			
}
