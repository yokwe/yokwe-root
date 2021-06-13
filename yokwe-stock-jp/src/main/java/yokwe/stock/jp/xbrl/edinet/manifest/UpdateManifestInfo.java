package yokwe.stock.jp.xbrl.edinet.manifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jakarta.xml.bind.JAXB;
import yokwe.stock.jp.edinet.Document;
import yokwe.stock.jp.edinet.Filename;
import yokwe.util.UnexpectedException;

public class UpdateManifestInfo {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateManifestInfo.class);

    // asr - 14207 - 有価証券報告書
    // drs -    15 - 有価証券報告書【みなし有価証券届出書】
    // ssr -  3399 - 中間期報告書
    // q1r -  5230 - 第１四半期報告書
    // q2r -  5052 - 第２四半期報告書
    // q3r -  5036 - 第３四半期報告書
    // q4r -     7 - 第４四半期報告書

	public static void main(String[] args) {
		logger.info("START");
		
		List<File> fileList = Document.getDocumentFileList();
		logger.info("fileList {}", fileList.size());
		
		// Remove duplicate using TreeSet and ManifestInfo.compareTo
		Set<ManifestInfo> result = new TreeSet<>(ManifestInfo.load());
		
		Set<String> docIDSet = result.stream().map(o -> o.docID).collect(Collectors.toSet());
		
		int count = 1;
		int countChange = 0;
		for(var file: fileList) {
//			logger.info("file {}", file.getName());
			
			if ((count++ % 1000) == 0) {
				logger.info("{}", String.format("%5d / %5d", count - 1, fileList.size()));
			}
			
			String docID = file.getName();
			
			// Skip if already processed
			if (docIDSet.contains(docID)) continue;
			
			Manifest manifest = null;
			try (ZipFile zipFile = new ZipFile(file)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();

			    while(entries.hasMoreElements()) {
			        ZipEntry entry = entries.nextElement();
			        
			        String entryName = entry.getName();
			        int lastIndex = entryName.lastIndexOf("/");
			        String dir  = entryName.substring(0, lastIndex);
			        String name = entryName.substring(lastIndex + 1);
			        
			        if (dir.contains("XBRL") && dir.contains("PublicDoc")) {
						if (name.equals(Filename.Manifest.NAME)) {
//							logger.info("manifest {}", name);
							try (InputStream is = zipFile.getInputStream(entry)) {
								manifest = JAXB.unmarshal(is, Manifest.class);
							}
							break;
						}
			        }
			    }
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
			
			// Skip if no manifest
			if (manifest == null) continue;

		    // sanity check
			if (manifest.list.size() == 0) {
				logger.warn("no list       {}", file.getPath());
//					logger.warn("size     {}", manifest.list.size());
//					logger.warn("manifest {}", manifest);
//					logger.warn("file     {}", file.getPath());
				throw new UnexpectedException("manifest.list.size() != 1");
			}

		    for(var e: manifest.list) {
		    	Filename.Instance instance = Filename.Instance.getInstance(e.preferredFilename);
		    	for(var honbunString: e.ixbr) {
		    		Filename.Honbun honbun = Filename.Honbun.getInstance(honbunString);
		    		
		    		if (Filename.equals(instance, honbun)) {
			    		ManifestInfo manifestInfo = new ManifestInfo(docID, honbun);
			    		result.add(manifestInfo);
			    		countChange++;
		    		} else {
		    			logger.error("not equals");
		    			logger.error(" instance {} {} {} {} {} {} {} {}", instance.form, instance.report, instance.reportNo, instance.code, instance.codeNo, instance.date, instance.submitNo, instance.submitDate);
		    			logger.error(" honbun   {} {} {} {} {} {} {} {}", honbun.form, honbun.report, honbun.reportNo, honbun.code, honbun.codeNo, honbun.date, honbun.submitNo, honbun.submitDate);
		    			throw new UnexpectedException("not equals");
		    		}
		    	}
		    }
		}
		
		logger.info("countChange {}", countChange);
		logger.info("save {} {}", result.size(), ManifestInfo.getPath());
		ManifestInfo.save(result);
		
		logger.info("STOP");
	}
}
