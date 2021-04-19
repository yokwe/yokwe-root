package yokwe.stock.jp.release;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.tdnet.TDNET;
import yokwe.util.FileUtil;

public class DownloadRelease {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(DownloadRelease.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		boolean onlyToday = Boolean.getBoolean("onlyToday");
		logger.info("onlyToday {}", onlyToday);
		
		{
			// fill map with existing release info from tmp/data/releas.csv
			Map<String, Release> map = Release.getMap();
			logger.info("release map {}", map.size());

			LocalDate date  = LocalDate.now();
			int       count = 0;
			{
				for(;;) {
					Page page = Page.getInstance(date);
					if (page == null) break;
					logger.info("page {}", page);
					for(Release e: page.entryList) {
//						logger.info("  {}", e);
						
//						{
//							String filename = String.format("%s.pdf", e.id);
//							File file = Release.getDataFile(date, filename);
//
//							if (!file.exists()) {
//								logger.info("file {}", file.getPath());
//								byte[] content  = Release.downloadData(e.pdf);
//								FileUtil.rawWrite().file(file, content);
//							}
//						}
						{
							if (!e.xbrl.isEmpty()) {
								String filename = String.format("%s.zip", e.id);
								File file = Release.getDataFile(date, filename);

								if (!file.exists()) {
									logger.info("file {}", file.getPath());
									byte[] content  = Release.downloadData(e.xbrl);
									FileUtil.rawWrite().file(file, content);
								}
							}
						}

						map.put(e.id, e);
						count++;
					}
					date = date.minusDays(1);
					if (onlyToday) break; // no need to process another date
				}
			}
			
			{
				logger.info("update count {}", count);
				if (0 < count) {
					List<Release> list = new ArrayList<>(map.values());
					logger.info("save {} {}", Release.PATH_FILE, list.size());
					Release.save(list);
				}
			}
		}
		
		// Save xbrl file in zip file
		{
			Map<SummaryFilename, File> map = TDNET.getFileMap();
			logger.info("tdnet map {}", map.size());
			
			int countSave = 0;
			for(File dataFile: Release.getDataFileList()) {
				if (dataFile.getName().endsWith(".zip")) {
					try (ZipFile zipFile = new ZipFile(dataFile)) {
						Enumeration<? extends ZipEntry> entries = zipFile.entries();

					    while(entries.hasMoreElements()){
					        ZipEntry entry = entries.nextElement();
					        SummaryFilename filename = SummaryFilename.getInstance(entry.getName());
					        if (filename == null) continue;
					        
					        if (map.containsKey(filename)) {
					        	// No need to save, because the file is already saved
					        } else {
					        	countSave++;
					        	String path = TDNET.getPath(filename);
					        	logger.info("save {}", path);
					        	//
					        	File file = new File(path);
						        try (InputStream is = zipFile.getInputStream(entry)) {
						        	FileUtil.rawWrite().file(file, is);
						        }
					        }
					    }
					} catch (IOException e) {
						String exceptionName = e.getClass().getSimpleName();
						logger.error("{} {}", exceptionName, e);
						throw new UnexpectedException(exceptionName, e);
					}
				}
			}
			logger.info("save count {}", countSave);
			if (0 < countSave) {
				TDNET.touch();
			}
		}
		
		logger.info("STOP");
	}
}
