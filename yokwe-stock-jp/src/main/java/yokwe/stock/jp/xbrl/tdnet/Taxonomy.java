package yokwe.stock.jp.xbrl.tdnet;

import yokwe.stock.jp.Storage;

public class Taxonomy {
	// 決算短信タクソノミ
	public static final String URL_TAXONOMY_ED = "https://www.jpx.co.jp/equities/listing/xbrl/tvdivq00000088ai-att/61_taxonomy.zip";
	
	// Directory location = 61_taxonomy/tse-ed-2014-01-12/taxonomy/jp/tse/tdnet/ed/t/2014-01-12/tse-ed-t-2014-01-12.xsd
	public static final String PATH_DIR_TSE_ED_T = Storage.XBRL.TDNET.getPath("61_taxonomy/tse-ed-2014-01-12/taxonomy/jp/tse/tdnet/ed/t/2014-01-12/");
	
	// CG報告書タクソノミ
	public static final String URL_TAXONOMY_CG = "https://www.jpx.co.jp/equities/listing/xbrl/tvdivq00000088ai-att/tse-cg-2015-04-01.zip";
	
	// Directory location = tse-cg-2015-04-01/jp/br/tdnet/t/cg/2007-06-30/tse-t-cg-2007-06-30.xsd
	public static final String PATH_DIR_TSE_CG_T = Storage.XBRL.getPath("tse-cg-2015-04-01/jp/br/tdnet/t/cg/2007-06-30/");
}
