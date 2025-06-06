package yokwe.stock.jp.edinet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import yokwe.stock.jp.xbrl.XBRL;
import yokwe.util.FileUtil;
import yokwe.util.StreamUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class UpdateManifest {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

    // asr - 14207 - 有価証券報告書
    // drs -    15 - 有価証券報告書【みなし有価証券届出書】
    // ssr -  3399 - 中間期報告書
    // q1r -  5230 - 第１四半期報告書
    // q2r -  5052 - 第２四半期報告書
    // q3r -  5036 - 第３四半期報告書
    // q4r -     7 - 第４四半期報告書
	
	public static class XML {
		public static class Manifest {
			public static class Instance {
				@XmlAttribute(name="id")
				public String id;
				@XmlAttribute(name="type")
				public String type;
				@XmlAttribute(name="preferredFilename")
				public String preferredFilename;
				
				@XmlElement(namespace=XBRL.NS_EDINET_MANIFEST, name = "ixbrl")
				public List<String> ixbr;
				
				@Override
				public String toString() {
					return ToString.withFieldName(this);
				}
				
				// Called from JAXB.unmarshal
				void afterUnmarshal(Unmarshaller u, Object parent) {
					// Sanity check
					if (ixbr.isEmpty()) {
						logger.error("ixbr is emtpy");
						logger.error("  {}", this);
						throw new UnexpectedException("ixbr is empty");
					}
				}
			}
			
			@XmlElementWrapper(namespace = XBRL.NS_EDINET_MANIFEST, name="list")
			@XmlElement(namespace = XBRL.NS_EDINET_MANIFEST, name="instance")
			public List<Instance> list;
			
			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
			
			// Called from JAXB.unmarshal
			void afterUnmarshal(Unmarshaller u, Object parent) {
				// Sanity check
				if (list == null) {
					logger.error("list is emtpy");
					logger.error("  {}", this);
					throw new UnexpectedException("list is empty");
				}
			}
		}
	}

	private static String getName(ZipEntry zipEntry) {
		Path path = Path.of(zipEntry.getName());
		return path.getFileName().toString();
	}
	private static void save(File file, ZipFile zipFile, Map<String, ZipEntry> map) {
		// return if file already exists
		if (file.exists()) return;
		
		String name = file.getName();
		
		if (map.containsKey(name)) {
			ZipEntry zipEntry = map.get(name);
			
			try (InputStream is = zipFile.getInputStream(zipEntry)) {
				byte[] bytes = is.readAllBytes();
				String content = new String(bytes, StandardCharsets.UTF_8);
				FileUtil.write().file(file, content);
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		} else {
			logger.error("no map key");
			logger.error("  name {}", name);
			logger.error("  map  {}", map.keySet());
			throw new UnexpectedException("no map key");
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<Document> documentList = Document.getList();
		Collections.sort(documentList);
		logger.info("document               {}", documentList.size());
		
		for (Iterator<Document> iterator = documentList.iterator(); iterator.hasNext();) {
		    var e = iterator.next();
		    if (e.toFile().exists()) continue;
	        iterator.remove();
		}
		logger.info("document exists        {}", documentList.size());

		for (Iterator<Document> iterator = documentList.iterator(); iterator.hasNext();) {
		    var e = iterator.next();
		    if (e.xbrlFlag) continue;
	        iterator.remove();
		}
		logger.info("document xbrl          {}", documentList.size());
						
		// Remove duplicate using TreeSet and ManifestInfo.compareTo
		Set<Manifest> result = new TreeSet<>(Manifest.getList());
		logger.info("manifest               {}", result.size());
		
		// Remove entry that did not appeared in documentSet
		{
			List<File> deleteList = new ArrayList<>();
			Set<String> documentSet = documentList.stream().map(o ->o.docID).collect(Collectors.toSet());
			for (Iterator<Manifest> iterator = result.iterator(); iterator.hasNext();) {
			    var e = iterator.next();
				if (documentSet.contains(e.docID)) continue;
				
				// remove from result
		        iterator.remove();
		        
		        // delete file
		        File dir = e.toInstance().toFile(e).getParentFile();
		        deleteList.add(dir);
			}
			logger.info("manifest unknown       {}", deleteList.size());
			for(File file: deleteList) {
		        logger.warn("  delete {}", file.getPath());
//		        FileUtil.delete(file); // FIXME
			}
			logger.info("manifest known         {}", result.size());
		}
		
		logger.info("manifest document      {}", result.size());
		logger.info("manifest instance      {}", result.stream().map(o -> o.toInstance().toString()).distinct().count());
		logger.info("manifest honbun        {}", result.stream().map(o -> o.toHonbun().toString()).distinct().count());
		
		// Remove entry if document docID appeared in result
		{
			Set<String> resultSet = result.stream().map(o -> o.docID).collect(Collectors.toSet());

			for (Iterator<Document> iterator = documentList.iterator(); iterator.hasNext();) {
			    var e = iterator.next();
			    if (resultSet.contains(e.docID)) {
			        iterator.remove();
			    }
			}
			logger.info("document manifest      {}", documentList.size());
		}
		
		int count       = 0;
		int countChange = 0;
		int countFile   = 0;
		for(var document: documentList) {
//			logger.info("file {}", file.getName());
			
			File file  = document.toFile();

			if ((count++ % 1000) == 0) {
				logger.info("{}", String.format("%5d / %5d  %s", count - 1, documentList.size(), file.getPath()));
				Manifest.save(result);
			}
			
			try (ZipFile zipFile = new ZipFile(file)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				
				Map<String, ZipEntry> map = StreamUtil.asStream(entries).
					filter(o -> o.getName().startsWith("XBRL/PublicDoc/")).
					filter(o -> !o.getName().endsWith("gif")).
					filter(o -> !o.getName().endsWith("jpg")).
					filter(o -> !o.getName().endsWith("png")).
					collect(Collectors.toMap(UpdateManifest::getName, o -> (ZipEntry)o));
				
				if (map.isEmpty()) {
					logger.warn("empty map  {}", file.getPath());
					continue;
				}
				
				XML.Manifest xmlManifest;
				if (map.containsKey(Filename.Manifest.NAME)) {
					ZipEntry zipEntry = map.get(Filename.Manifest.NAME);
					
					try (InputStream is = zipFile.getInputStream(zipEntry)) {
						xmlManifest = JAXB.unmarshal(is, XML.Manifest.class);
					}
				} else {
					logger.error("No manifest");
					logger.error("  file {}", file.getPath());
	    			throw new UnexpectedException("No manifest");
				}
				
			    // sanity check
				if (xmlManifest.list.isEmpty()) {
					logger.warn("empty list");
					logger.warn("file     {}", file.getPath());
					throw new UnexpectedException("empty list");
				}
				boolean hasWarning = false;
			    for(var e: xmlManifest.list) {
			    	Filename.Instance instance = Filename.Instance.getInstance(e.preferredFilename);
			    	for(var honbunString: e.ixbr) {
			    		Filename.Honbun honbun = Filename.Honbun.getInstance(honbunString);
			    		if (honbun == null) {
			    			logger.error("honbun is null");
			    			logger.error("  {}", file.getPath());
			    			logger.error("  {}", honbunString);
			    			throw new UnexpectedException("honbun is null");
			    		} else {
				    		if (!Filename.equals(instance, honbun)) {
				    			hasWarning = true;
				    			logger.warn("not equals");
				    			logger.warn("  {}", file.getPath());
				    			logger.warn("  instance {} {} {} {} {} {} {} {}", instance.form, instance.report, instance.reportNo, instance.code, instance.codeNo, instance.date, instance.submitNo, instance.submitDate);
				    			logger.warn("  honbun   {} {} {} {} {} {} {} {}", honbun.form, honbun.report, honbun.reportNo, honbun.code, honbun.codeNo, honbun.date, honbun.submitNo, honbun.submitDate);
//				    			throw new UnexpectedException("not equals");
				    		}
			    		}
			    	}
			    }
			    if (hasWarning) continue;

			    for(var e: xmlManifest.list) {
			    	Filename.Instance instance = Filename.Instance.getInstance(e.preferredFilename);
			    	save(instance.toFile(document), zipFile, map);
			    	countFile++;
			    	
			    	for(var honbunString: e.ixbr) {
			    		Filename.Honbun honbun = Filename.Honbun.getInstance(honbunString);
				    	save(honbun.toFile(document), zipFile, map);
				    	countFile++;
				    	
			    		Manifest manifest = new Manifest(document, honbun);
			    		result.add(manifest);
			    		countChange++;
			    	}
			    }
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
		
		logger.info("countChange {}", countChange);
		logger.info("countFile   {}", countFile);
		logger.info("save {} {}", result.size(), Manifest.getPath());
		Manifest.save(result);
		
		logger.info("STOP");
	}
}
