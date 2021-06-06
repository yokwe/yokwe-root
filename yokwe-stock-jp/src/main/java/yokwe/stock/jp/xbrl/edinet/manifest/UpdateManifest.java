package yokwe.stock.jp.xbrl.edinet.manifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jakarta.xml.bind.JAXB;
import yokwe.stock.jp.edinet.Document;
import yokwe.stock.jp.edinet.Filename;
import yokwe.util.UnexpectedException;

public class UpdateManifest {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateManifest.class);

	public static void main(String[] args) {
		logger.info("START");
		
		List<File> fileList = Document.getDocumentFileList();
		logger.info("fileList {}", fileList.size());
		
		int count = 1;
		for(var file: fileList) {
//			logger.info("file {}", file.getName());
			if ((count++ % 1000) == 0) {
				logger.info("{}", String.format("%5d / %5d", count - 1, fileList.size()));
			}
			try (ZipFile zipFile = new ZipFile(file)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();

				Map<String, ZipEntry> map = new TreeMap<>();
				Manifest manifest = null;
				
			    while(entries.hasMoreElements()) {
			        ZipEntry entry = entries.nextElement();
			        
			        String entryName = entry.getName();
			        int lastIndex = entryName.lastIndexOf("/");
			        String dir  = entryName.substring(0, lastIndex);
			        String name = entryName.substring(lastIndex + 1);
			        
			        if (!dir.contains("XBRL")) continue;
			        
			        if (!dir.endsWith("PublicDoc")) continue;
			        
			        if (Filename.Honbun.getInstance(name) != null) {
//						logger.info("honbun   {}", name);
						map.put(name, entry);
						continue;
			        }
			        if (Filename.Instance.getInstance(name) != null) {
//						logger.info("instance {}", name);
						map.put(name, entry);
						continue;
			        }
					if (name.equals(Filename.Manifest.NAME)) {
//						logger.info("manifest {}", name);
						try (InputStream is = zipFile.getInputStream(entry)) {
							manifest = JAXB.unmarshal(is, Manifest.class);
						}
						continue;
					}
//					logger.info("unknown  {}", name);
			    }
			    
			    // skip if map is empty
			    if (map.isEmpty()) continue;
			    
			    List<String> headerList = map.keySet().stream().filter(o -> o.contains("0000000")).collect(Collectors.toList());
			    String header = headerList.isEmpty() ? "null" : headerList.get(0);
			    if (manifest == null) {
					logger.warn("no manifest   {}  {}  {}", file.getPath(), header, map.size());
//					logger.warn("file     {}", file.getPath());
//					throw new UnexpectedException("manifest.list.size() != 1");
					continue;
			    }
				if (manifest.list.size() == 0) {
					logger.warn("no list       {}", file.getPath());
//					logger.warn("size     {}", manifest.list.size());
//					logger.warn("manifest {}", manifest);
//					logger.warn("file     {}", file.getPath());
					throw new UnexpectedException("manifest.list.size() != 1");
				}
				if (1 < manifest.list.size()) {
					logger.warn("1 < size      {}  {}  {}", file.getPath(), header, manifest.list.size());
//					logger.warn("list.size() != 1");
//					logger.warn("size     {}", manifest.list.size());
//					logger.warn("manifest {}", manifest);
//					logger.warn("file     {}", file.getPath());
//					throw new UnexpectedException("manifest.list.size() != 1");
					continue;
				}
				if (manifest.list.get(0).preferredFilename == null) {
					logger.warn("no preferred  {}", file.getPath());
//					logger.warn("preferredFilename == null");
//					logger.warn("manifest {}", manifest);
//					logger.warn("file     {}", file.getPath());
					throw new UnexpectedException("preferredFilename == null");
				}
				
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
			
//			break;
		}
		
		logger.info("STOP");
	}
}
