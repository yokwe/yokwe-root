package yokwe.security.japan.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.release.Release;
import yokwe.security.japan.tdnet.SummaryFilename;
import yokwe.util.FileUtil;

public class T018 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T018.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		List<File> fileList = Release.getDataFileList();
		for(File file: fileList) {
//			logger.info("{}", file.getName());
			
			try (ZipFile zipFile = new ZipFile(file)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();

			    while(entries.hasMoreElements()){
			        ZipEntry entry = entries.nextElement();
			        String name = entry.getName();
			        SummaryFilename filename = SummaryFilename.getInstance(name);
			        if (filename == null) continue;
			        
			        logger.info("entry  {}", filename);
			        
			        String path = String.format("tmp/%s", filename.toString());
			        File outputFile = new File(path);
			        
			        try (InputStream is = zipFile.getInputStream(entry)) {
				        FileUtil.rawWrite().file(outputFile, is);
			        }
			    }
			}
			break;
		}
		
		logger.info("STOP");
	}
}
