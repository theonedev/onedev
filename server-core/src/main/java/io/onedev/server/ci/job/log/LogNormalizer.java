package io.onedev.server.ci.job.log;

import javax.annotation.Nullable;

import io.onedev.commons.launcher.loader.ExtensionPoint;

/**
 * Sometimes job log message needs to be normalized for better display. For instance Maven command prints something 
 * like below:
 * 
 * <pre>[INFO] Scanning for projects...</pre>
 * 
 * In such case, we should extract the log level information to override OneDev's default log level, and the 
 * original message should also be modified to remove the log level information
 * 
 * @author robin
 *
 */
@ExtensionPoint
public interface LogNormalizer {
	
	/**
	 * Normalize provided job log message  
	 * @param message
	 * 			message to be normalized
	 * @return
	 * 			normalized result, or <tt>null</tt> if this normalizer does not handle this message
	 */
	@Nullable
	Normalized normalize(String message);
	
	public static class Normalized {

		private final LogLevel level;
		
		private final String message;
		
		public Normalized(@Nullable LogLevel level, String message) {
			this.level = level;
			this.message = message;
		}

		/**
		 * @return 
		 * 			Log level of this message, or <tt>null</tt> if log level information 
		 * 			is not available in this message
		 */
		@Nullable
		public LogLevel getLevel() {
			return level;
		}

		/**
		 * @return 
		 * 			normalized message  
		 */
		public String getMessage() {
			return message;
		}
		
	}
	
}
