package io.onedev.server.plugin.maven;

import javax.inject.Singleton;

import io.onedev.server.ci.job.log.LogLevel;
import io.onedev.server.ci.job.log.LogNormalizer;

@Singleton
public class MavenLogNormalizer implements LogNormalizer {

	@Override
	public Normalized normalize(String message) {
		if (message.startsWith("[INFO] ")) {
			return new Normalized(LogLevel.INFO, message.substring("[INFO] ".length()));
		} else if (message.startsWith("[ERROR] ")) {
			return new Normalized(LogLevel.ERROR, message.substring("[ERROR] ".length()));
		} else if (message.startsWith("[WARNING] ")) {
			return new Normalized(LogLevel.WARN, message.substring("[WARNING] ".length()));
		} else if (message.startsWith("[DEBUG] ")) {
			return new Normalized(LogLevel.DEBUG, message.substring("[DEBUG] ".length()));
		} else if (message.startsWith("[TRACE] ")) {
			return new Normalized(LogLevel.TRACE, message.substring("[TRACE] ".length()));
		} else {
			return null;
		}
	}

}
