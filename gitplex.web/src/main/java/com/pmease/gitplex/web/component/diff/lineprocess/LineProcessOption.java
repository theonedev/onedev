package com.pmease.gitplex.web.component.diff.lineprocess;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.git.LineProcessor;

public enum LineProcessOption implements LineProcessor {
	
	IGNORE_NOTHING() {

		@Override
		public String process(String line) {
			return line;
		}

		@Override
		public String getName() {
			return "Compare using original lines";
		}
		
	},
	IGNORE_EOL() {

		@Override
		public String process(String line) {
			return StringUtils.stripEnd(line, "\r\n");
		}

		@Override
		public String getName() {
			return "Ignore end of line differences";
		}
		
	},
	IGNORE_EOL_SPACES() {

		@Override
		public String process(String line) {
			return StringUtils.stripEnd(line, " \t\r\n");
		}

		@Override
		public String getName() {
			return "Ignore white spaces at line end";
		}

	},
	IGNORE_CHANGE_SPACES() {

		@Override
		public String process(String line) {
			return WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");
		}

		@Override
		public String getName() {
			return "Ignore white space changes";
		}

	};

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	
}
