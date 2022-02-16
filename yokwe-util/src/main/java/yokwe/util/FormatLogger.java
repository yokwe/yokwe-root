package yokwe.util;

public final class FormatLogger {
	public static FormatLogger getLogger(Class<?> clazz) {
		var logger = org.slf4j.LoggerFactory.getLogger(clazz);
		return new FormatLogger(logger);
	}

	private final org.slf4j.Logger logger;
	
	private FormatLogger(org.slf4j.Logger logger) {
		this.logger = logger;
	}
	
	public void trace(String format, Object... arguments) {
		if (logger.isTraceEnabled()) {
			String message = String.format(format, arguments);
			logger.trace(message);
		}
	}
	public void debug(String format, Object... arguments) {
		if (logger.isDebugEnabled()) {
			String message = String.format(format, arguments);
			logger.debug(message);
		}
	}
	public void info(String format, Object... arguments) {
		if (logger.isInfoEnabled()) {
			String message = String.format(format, arguments);
			logger.info(message);
		}
	}
	public void warn(String format, Object... arguments) {
		if (logger.isWarnEnabled()) {
			String message = String.format(format, arguments);
			logger.warn(message);
		}
	}
	public void error(String format, Object... arguments) {
		if (logger.isErrorEnabled()) {
			String message = String.format(format, arguments);
			logger.error(message);
		}
	}
}
