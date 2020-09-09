package io.onedev.server.util;

import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

public abstract class SimpleLogger {

	private static final Pattern EOL_PATTERN = Pattern.compile("\r?\n");

	public void log(Throwable t) {
		StringBuilder builder = new StringBuilder();
		for (String line: Splitter.on(EOL_PATTERN).split(Throwables.getStackTraceAsString(t)))
			builder.append("\n    ").append(line);
		log(builder.toString());
	}
		
	public abstract void log(String message);
	
}
