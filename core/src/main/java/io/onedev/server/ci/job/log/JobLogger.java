package io.onedev.server.ci.job.log;

public abstract class JobLogger {

	public void trace(String message) {
		log(LogLevel.TRACE, message);
	}
	
	public void debug(String message) {
		log(LogLevel.DEBUG, message);
	}
	
	public void info(String message) {
		log(LogLevel.INFO, message);
	}
	
	public void warn(String message) {
		log(LogLevel.WARN, message);
	}
	
	public void error(String message) {
		log(LogLevel.ERROR, message);
	}
	
	public boolean isTraceEnabled() {
		return getLogLevel().ordinal() >= LogLevel.TRACE.ordinal();
	}

	public boolean isDebugEnabled() {
		return getLogLevel().ordinal() >= LogLevel.DEBUG.ordinal();
	}
	
	public boolean isInfoEnabled() {
		return getLogLevel().ordinal() >= LogLevel.INFO.ordinal();
	}
	
	public boolean isWarnEnabled() {
		return getLogLevel().ordinal() >= LogLevel.WARN.ordinal();
	}
	
	public boolean isErrorEnabled() {
		return getLogLevel().ordinal() >= LogLevel.ERROR.ordinal();
	}
	
	public abstract void log(LogLevel logLevel, String message);
	
	public abstract LogLevel getLogLevel();
	
}
