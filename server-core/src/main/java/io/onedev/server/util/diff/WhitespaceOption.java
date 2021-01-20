package io.onedev.server.util.diff;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public enum WhitespaceOption {
	
	DEFAULT() {

		@Override
		public String process(String line) {
			return StringUtils.stripEnd(line, " \t\r\n");
		}

		@Override
		public String getDescription() {
			return "Ignore trailing white spaces";
		}
		
	},
	IGNORE_LEADING() {

		@Override
		public String process(String line) {
			return StringUtils.stripStart(line, " \t\r\n");
		}

		@Override
		public String getDescription() {
			return "Ignore leading white spaces";
		}
		
	},
	IGNORE_CHANGE() {

		@Override
		public String process(String line) {
			return WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");
		}

		@Override
		public String getDescription() {
			return "Ignore white space changes";
		}
		
	},
	IGNORE_ALL() {

		@Override
		public String process(String line) {
			return WHITESPACE_PATTERN.matcher(line.trim()).replaceAll("");
		}

		@Override
		public String getDescription() {
			return "Ignore all white spaces";
		}
		
	},
	DO_NOT_IGNORE() {

		@Override
		public String process(String line) {
			return line;
		}

		@Override
		public String getDescription() {
			return "Do not ignore white spaces";
		}

	};

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	
	public abstract String getDescription();
	
	public abstract String process(String line);
	
	public static WhitespaceOption ofName(@Nullable String name) {
		if (name != null) 
			return valueOf(name.toUpperCase());
		else 
			return null;
	}
}
