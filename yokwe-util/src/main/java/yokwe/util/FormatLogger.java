package yokwe.util;

public final class FormatLogger {
	public static FormatLogger getLogger(Class<?> clazz) {
		var logger = org.slf4j.LoggerFactory.getLogger(clazz);
		return new FormatLogger(logger);
	}
	// use caller class for logger name
	public static FormatLogger getLogger() {
//		Class<?> caller = java.lang.invoke.MethodHandles.lookup().lookupClass();
		Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		return getLogger(caller);
	}
	
	private final org.slf4j.Logger logger;
	
	private FormatLogger(org.slf4j.Logger logger) {
		this.logger = logger;
	}
	
	public void trace(String message) {
		logger.trace(message);
	}
	public void debug(String message) {
		logger.trace(message);
	}
	public void info(String message) {
		logger.info(message);
	}
	public void warn(String message) {
		logger.warn(message);
	}
	public void error(String message) {
		logger.error(message);
	}
	
	public void trace(String format, Object... arguments) {
		if (logger.isTraceEnabled()) logger.trace(String.format(format, arguments));
	}
	public void debug(String format, Object... arguments) {
		if (logger.isDebugEnabled()) logger.debug(String.format(format, arguments));
	}
	public void info(String format, Object... arguments) {
		if (logger.isInfoEnabled()) logger.info(String.format(format, arguments));
	}
	public void warn(String format, Object... arguments) {
		if (logger.isWarnEnabled()) logger.warn(String.format(format, arguments));
	}
	public void error(String format, Object... arguments) {
		if (logger.isErrorEnabled()) logger.error(String.format(format, arguments));
	}
	
}
