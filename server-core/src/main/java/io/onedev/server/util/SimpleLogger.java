package io.onedev.server.util;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

public abstract class SimpleLogger {

	private static final Pattern EOL_PATTERN = Pattern.compile("\r?\n");

	public void log(Throwable t) {
		log(null, t);
	}
		
	public void log(@Nullable String message, Throwable t) {
		StringBuilder builder = new StringBuilder();
		if (message != null)
			builder.append(message);
		for (String line: Splitter.on(EOL_PATTERN).split(Throwables.getStackTraceAsString(t))) {
			if (builder.length() == 0)
				builder.append(line);
			else
				builder.append("\n    ").append(line);
		}
		log(builder.toString());
	}
	
	public abstract void log(String message);
	
}
