package io.onedev.server.ci.job.log.normalizer;

import javax.inject.Singleton;

import io.onedev.server.ci.job.log.LogLevel;

@Singleton
public class MavenNormalizer implements LogNormalizer {

	@Override
	public Result normalize(String message) {
		if (message.startsWith("[INFO] ")) {
			return new Result(LogLevel.INFO, message.substring("[INFO] ".length()));
		} else if (message.startsWith("[ERROR] ")) {
			return new Result(LogLevel.ERROR, message.substring("[ERROR] ".length()));
		} else if (message.startsWith("[WARNING] ")) {
			return new Result(LogLevel.WARN, message.substring("[WARNING] ".length()));
		} else if (message.startsWith("[DEBUG] ")) {
			return new Result(LogLevel.DEBUG, message.substring("[DEBUG] ".length()));
		} else if (message.startsWith("[TRACE] ")) {
			return new Result(LogLevel.TRACE, message.substring("[TRACE] ".length()));
		} else {
			return null;
		}
	}

}
