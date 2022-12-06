package yokwe.stock.jp.tdnet;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

// 決算短信サマリー情報及び予想修正報告
	// インラインXBRLファイル名
    //   tse-{報告書}[{報告書詳細区分}]-{証券コード}-{開示番号}-ixbrl.htm
	// 報告書   :=  [{期区分}{連結・非連結区分}]{報告区分}
	// 開示番号　:=  {提出日 8 桁}{3 から開始する連番 1桁}{証券コード 5 桁}
	//   tse-qcedjpsm-71770-20170725371770-ixbrl.htm
	//   tse-rvfc-82270-20191222439755-ixbrl.htm
	public class SummaryFilename implements Comparable<SummaryFilename> {
		private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

		private static final Pattern PAT = Pattern.compile("tse-(?<period>[asq]?)(?<consolidate>[cn]?)(?<category>[a-z]{4})(?<detail>(sm|fr)?)-(?<tdnetCode>[0-9]{5})-(?<id>[0-9]{14})-ixbrl.htm");
		
		private static final StringUtil.MatcherFunction<SummaryFilename> OP = (m -> new SummaryFilename(
				m.group("period"),
				m.group("consolidate"),
				m.group("category"),
				m.group("detail"),
				m.group("tdnetCode"),
				m.group("id")));
		public static SummaryFilename getInstance(String string) {
			List<SummaryFilename> list = StringUtil.find(string, PAT, OP).collect(Collectors.toList());
			if (list.size() == 0) return null;
			if (list.size() == 1) return list.get(0);
			logger.error("Unexpected value %s!", list);
			throw new UnexpectedException("Unexpected value");
		}

		public final Period      period;
		public final Consolidate consolidate;
		public final Category    category;
		public final Detail      detail;
		public final String      tdnetCode;
		public final String      id;
		public final String      string;
		
		public SummaryFilename(String period, String consolidate, String category, String detail, String tdnetCode, String id) {
			this.period      = Period.getInstance(period);
			this.consolidate = Consolidate.getInstance(consolidate);
			this.category    = Category.getInstance(category);
			this.detail      = Detail.getInstance(detail);
			this.tdnetCode   = tdnetCode;
			this.id          = id;
			
			{
				//   tse-qcedjpsm-71770-20170725371770-ixbrl.htm
				StringBuffer sb = new StringBuffer();
				sb.append("tse-");
				if (this.period != null) {
					sb.append(this.period.value);
				}
				if (this.consolidate != null) {
					sb.append(this.consolidate.value);
				}
				sb.append(this.category.value);
				if (this.detail != null) {
					sb.append(this.detail.value);
				}
				sb.append("-");
				sb.append(this.tdnetCode);
				sb.append("-");
				sb.append(this.id);
				sb.append("-ixbrl.htm");
				
				this.string = sb.toString();
			}
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o instanceof SummaryFilename) {
				SummaryFilename that = (SummaryFilename)o;
				return this.string.equals(that.string);
			} else {
				return false;
			}
		}
		@Override
		public String toString() {
			return string;
		}

		@Override
		public int compareTo(SummaryFilename that) {
			int ret = this.tdnetCode.compareTo(that.tdnetCode);
			if (ret == 0) ret = this.id.compareTo(that.id);
			if (ret == 0) ret = this.category.value.compareTo(that.category.value);
			if (ret == 0) ret = this.string.compareTo(that.string);
			return ret;
		}
		
	}