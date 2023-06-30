package yokwe.stock.jp.toushin;


import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.nomura.NomuraFund;
import yokwe.util.ListUtil;

public class UpdateStatsNomura {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String PATH_FILE = Storage.Toushin.getPath("stats-nomura.csv");
	public static final String getPath() {
		return PATH_FILE;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		ArrayList<Stats> statsList = new ArrayList<>(Stats.load());
		logger.info("statsList  {}", statsList.size());
		
		Set<String>nikkoSet = NomuraFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		logger.info("nomuraSet   {}", nikkoSet.size());
		
		statsList.removeIf(o -> !nikkoSet.contains(o.isinCode));
				
		String path = getPath();
		logger.info("statsList  {}  {}", statsList.size(), path);
		ListUtil.save(Stats.class, path, statsList);
		
		logger.info("STOP");
	}
}
