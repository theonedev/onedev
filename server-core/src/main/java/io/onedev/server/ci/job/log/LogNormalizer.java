package io.onedev.server.ci.job.log;

import javax.annotation.Nullable;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface LogNormalizer {
	
	@Nullable
	Result normalize(String message);
	
	public static class Result {

		private final LogLevel level;
		
		private final String message;
		
		public Result(LogLevel level, String message) {
			this.level = level;
			this.message = message;
		}

		public LogLevel getLevel() {
			return level;
		}

		public String getMessage() {
			return message;
		}
		
	}
	
}
