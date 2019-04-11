package io.onedev.server.ci.job.log;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

public abstract class JobLogger extends MarkerIgnoringBase implements Logger {

	private static final long serialVersionUID = 1L;

	private final LogLevel logLevel;
	
	public JobLogger(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	@Override
	public String getName() {
		return "Job Logger";
	}
	
	@Override
	public boolean isTraceEnabled() {
		return logLevel.compareTo(LogLevel.TRACE) >= 0;
	}

	@Override
	public void trace(String msg) {
		if (isTraceEnabled())
			log(LogLevel.TRACE, msg, null);
	}

	@Override
	public void trace(String format, Object arg) {
		if (isTraceEnabled()) {
			String msgStr = MessageFormatter.format(format, arg).getMessage();
			log(LogLevel.TRACE, msgStr, null);
		}
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
			log(LogLevel.TRACE, msgStr, null);
		}
	}

	@Override
	public void trace(String msg, Throwable t) {
		if (isTraceEnabled())
			log(LogLevel.TRACE, msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return logLevel.compareTo(LogLevel.DEBUG) >= 0;
	}

	@Override
	public void debug(String msg) {
		if (isDebugEnabled())
			log(LogLevel.DEBUG, msg, null);
	}

	@Override
	public void debug(String format, Object arg) {
		if (isDebugEnabled()) {
			String msgStr = MessageFormatter.format(format, arg).getMessage();
			log(LogLevel.DEBUG, msgStr, null);
		}
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
			log(LogLevel.DEBUG, msgStr, null);
		}
	}

	@Override
	public void debug(String msg, Throwable t) {
		if (isDebugEnabled())
			log(LogLevel.DEBUG, msg, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return logLevel.compareTo(LogLevel.INFO) >= 0;
	}

	@Override
	public void info(String msg) {
		if (isInfoEnabled())
			log(LogLevel.INFO, msg, null);
	}

	@Override
	public void info(String format, Object arg) {
		if (isInfoEnabled()) {
			String msgStr = MessageFormatter.format(format, arg).getMessage();
			log(LogLevel.INFO, msgStr, null);
		}
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
			log(LogLevel.INFO, msgStr, null);
		}
	}

	@Override
	public void info(String msg, Throwable t) {
		if (isInfoEnabled())
			log(LogLevel.INFO, msg, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return logLevel.compareTo(LogLevel.WARN) >= 0;
	}

	@Override
	public void warn(String msg) {
		if (isWarnEnabled())
			log(LogLevel.WARN, msg, null);
	}

	@Override
	public void warn(String format, Object arg) {
		if (isWarnEnabled()) {
			String msgStr = MessageFormatter.format(format, arg).getMessage();
			log(LogLevel.WARN, msgStr, null);
		}
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		if (isWarnEnabled()) {
			String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
			log(LogLevel.WARN, msgStr, null);
		}
	}

	@Override
	public void warn(String msg, Throwable t) {
		if (isWarnEnabled())
			log(LogLevel.WARN, msg, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return logLevel.compareTo(LogLevel.ERROR) >= 0;
	}

	@Override
	public void error(String msg) {
		if (isErrorEnabled())
			log(LogLevel.ERROR, msg, null);
	}

	@Override
	public void error(String format, Object arg) {
		if (isErrorEnabled()) {
			String msgStr = MessageFormatter.format(format, arg).getMessage();
			log(LogLevel.ERROR, msgStr, null);
		}
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
			log(LogLevel.ERROR, msgStr, null);
		}
	}

	@Override
	public void error(String msg, Throwable t) {
		if (isErrorEnabled())
			log(LogLevel.ERROR, msg, t);
	}

	@Override
	public void debug(String arg0, Object... arg1) {
		if (isDebugEnabled()) {
			String msgStr = MessageFormatter.arrayFormat(arg0, arg1).getMessage();
			log(LogLevel.DEBUG, msgStr, null);
		}
	}

	@Override
	public void error(String arg0, Object... arg1) {
		if (isErrorEnabled()) {
			String msgStr = MessageFormatter.arrayFormat(arg0, arg1).getMessage();
			log(LogLevel.ERROR, msgStr, null);
		}
	}

	@Override
	public void info(String arg0, Object... arg1) {
		if (isInfoEnabled()) {
			String msgStr = MessageFormatter.arrayFormat(arg0, arg1).getMessage();
			log(LogLevel.INFO, msgStr, null);
		}
	}

	@Override
	public void trace(String arg0, Object... arg1) {
		if (isTraceEnabled()) {
			String msgStr = MessageFormatter.arrayFormat(arg0, arg1).getMessage();
			log(LogLevel.TRACE, msgStr, null);
		}
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		if (isWarnEnabled()) {
			String msgStr = MessageFormatter.arrayFormat(arg0, arg1).getMessage();
			log(LogLevel.WARN, msgStr, null);
		}
	}

	protected abstract void log(LogLevel logLevel, String message, @Nullable Throwable throwable);

}
