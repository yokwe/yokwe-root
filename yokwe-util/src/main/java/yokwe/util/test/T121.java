package yokwe.util.test;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.util.ClassUtil;

public class T121 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class A {
		public static void t001() {
			logger.info("t001");
		}
		public static void t002() {
			logger.info("t002");
		}
		public static void t003() {
			logger.info("t003");
		}
	}
	public static void main(String[] args) {
		logger.info("START");
		
		{
			Class<?> clazz = A.class;
			for(Method method: clazz.getDeclaredMethods()) {
				Runnable runnable = ClassUtil.toRunnable(method);
				
				logger.info("method {}  {}", method, runnable);
				runnable.run();
			}
		}
		
		
		logger.info("STOP");
	}

}
