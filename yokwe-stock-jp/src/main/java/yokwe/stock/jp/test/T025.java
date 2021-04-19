package yokwe.security.japan.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.security.japan.edinet.Document;
import yokwe.security.japan.fsa.InstanceFilename;
import yokwe.util.CSVUtil;

public class T025 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T025.class);
	
	public static class FileXBRL implements Comparable<FileXBRL> {
		public String           docID;
		public InstanceFilename filename;
		
		public FileXBRL(String docID, InstanceFilename filename) {
			this.docID    = docID;
			this.filename = filename;
		}
		public FileXBRL() {
			this(null, null);
		}
		@Override
		public int compareTo(FileXBRL that) {
			return this.docID.compareTo(that.docID);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
//	{
//		try {
//			List<File> dataFileList = Document.getDataFileList();
//			for(File file: dataFileList) {
//				if (file.getName().endsWith(".zip")) {
//					File newFile = new File(file.getParent(), file.getName().replace(".zip", ""));
//					logger.info("move  {}  {}", file.getPath(), newFile.getPath());
//					Files.move(file.toPath(), newFile.toPath());
//				}
//			}
//		} catch (IOException e) {
//			String exceptionName = e.getClass().getSimpleName();
//			logger.error("{} {}", exceptionName, e);
//			throw new UnexpectedException(exceptionName, e);
//		} finally {
//			System.exit(0);
//		}
//	}
//		{
//			Map<String, Document> map = Document.getMap();
//			List<File> list = Document.getDataFileList();
//			for(File file: list) {
//				String docID = file.getName();
//				if (map.containsKey(docID)) {
//					Document document = map.get(docID);
//					if (document.stockCode.isEmpty() && document.fundCode.isEmpty()) {
//						logger.info("delete {}", docID);
//						file.delete();
//					}
//				} else {
//					logger.warn("Unexpected docID {}", docID);
//				}
//			}
//			
//			System.exit(0);
//		}

		Map<String, FileXBRL> map = new TreeMap<>();
	
		{
			List<File> fileList = Document.getDataFileList();
			logger.info("fileList {}", fileList.size());
			
			int count = 0;
			for(File file: fileList) {
				String docID = file.getName();
				if ((count % 100) == 0) {
					logger.info("{} {}", String.format("%5d / %5d", count, fileList.size()), docID);
				}
				count++;
				
	//			logger.info("file  {}", file.getPath());
	
				
				try (ZipFile zipFile = new ZipFile(file)) {
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
	
				    while(entries.hasMoreElements()){
				        ZipEntry entry = entries.nextElement();
				        String name = entry.getName();
				        InstanceFilename filename = InstanceFilename.getInstance(name);
				        if (filename == null) continue;
				        
//				        File xbrlFile = Document.getXBRLFile(filename);
//				        if (!xbrlFile.exists()) {
//					        try (InputStream is = zipFile.getInputStream(entry)) {
//					        	FileUtil.rawWrite().file(xbrlFile, is);
//					        }
//				        }

				        String key   = String.format("%s %s %s %s", filename.code, filename.submitDate, filename.submitNo, filename.extraNo);
				        String value = String.format("%s %s", name, file.getPath());
				        if (map.containsKey(key)) {
				        	logger.error("duplicate key");
				        	logger.error("  key  {}", key);
				        	logger.error("  old  {}", map.get(key));
				        	logger.error("  new  {}", value);
							throw new UnexpectedException("duplicate key");
				        } else {
				        	map.put(key, new FileXBRL(docID, filename));
				        }
				    }
				} catch (IOException e) {
					String exceptionName = e.getClass().getSimpleName();
					logger.error("{} {}", exceptionName, e);
					throw new UnexpectedException(exceptionName, e);
				}
			}
		}
		
		List<FileXBRL> list = new ArrayList<>(map.values());
		logger.info("list {}", list.size());
		Collections.sort(list);
		CSVUtil.write(FileXBRL.class).file("tmp/data/aaa.csv", list);
	
		logger.info("STOP");
	}
}
