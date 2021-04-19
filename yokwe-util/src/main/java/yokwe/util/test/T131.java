package yokwe.util.test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.comp.helper.Bootstrap;

public class T131 {
	private static final Logger logger = LoggerFactory.getLogger(T131.class);
		
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		logger.info("START");
		
		logger.info("options {}", Arrays.asList(Bootstrap.getDefaultOptions()));
		
		logger.info("STOP");
	}

}
