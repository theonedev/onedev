package io.onedev.server.util;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

public abstract class JobLogger {

	private static final Pattern EOL_PATTERN = Pattern.compile("\r?\n");

	public void log(String message, @Nullable Throwable t) {
		if (t != null) {
			for (String line: Splitter.on(EOL_PATTERN).split(Throwables.getStackTraceAsString(t)))
				message += "\n    " + line;
		}
		log(message);
	}
	
	public abstract void log(String message);
	
}
