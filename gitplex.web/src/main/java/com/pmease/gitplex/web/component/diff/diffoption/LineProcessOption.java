package com.pmease.gitplex.web.component.diff.diffoption;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.git.LineProcessor;

public enum LineProcessOption implements LineProcessor {
	
	IGNORE_NOTHING("Compare using original line") {

		@Override
		public String process(String line) {
			return line;
		}
		
	},
	IGNORE_EOL("Ignore end of line differences") {

		@Override
		public String process(String line) {
			return StringUtils.stripEnd(line, "\r\n");
		}
		
	},
	IGNORE_EOL_SPACES("Ignore white spaces at line end") {

		@Override
		public String process(String line) {
			return StringUtils.stripEnd(line, " \t\r\n");
		}

	},
	IGNORE_CHANGE_SPACES("Ignore white space changes") {

		@Override
		public String process(String line) {
			return WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");
		}

	};

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	
	private final String displayName;
	
	LineProcessOption(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}
