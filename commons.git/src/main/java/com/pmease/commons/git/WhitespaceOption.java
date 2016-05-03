package com.pmease.commons.git;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.RawTextComparator;

public enum WhitespaceOption {
	
	DEFAULT() {

		@Override
		public String process(String line) {
			return line;
		}

		@Override
		public RawTextComparator getComparator() {
			return RawTextComparator.DEFAULT;
		}

		@Override
		public String getDescription() {
			return "Do not ignore white spaces";
		}

	},
	IGNORE_LEADING() {

		@Override
		public String process(String line) {
			return StringUtils.stripStart(line, " \t\r\n");
		}

		@Override
		public RawTextComparator getComparator() {
			return RawTextComparator.WS_IGNORE_LEADING;
		}

		@Override
		public String getDescription() {
			return "Ignore leading white spaces";
		}
		
	},
	IGNORE_TRAILING() {

		@Override
		public String process(String line) {
			return StringUtils.stripEnd(line, " \t\r\n");
		}

		@Override
		public RawTextComparator getComparator() {
			return RawTextComparator.WS_IGNORE_TRAILING;
		}

		@Override
		public String getDescription() {
			return "Ignore trailing white spaces";
		}
		
	},
	IGNORE_CHANGE() {

		@Override
		public String process(String line) {
			return WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");
		}

		@Override
		public RawTextComparator getComparator() {
			return RawTextComparator.WS_IGNORE_CHANGE;
		}
		
		@Override
		public String getDescription() {
			return "Ignore white space changes";
		}
		
	},
	IGNORE_ALL() {

		@Override
		public String process(String line) {
			return WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");
		}

		@Override
		public RawTextComparator getComparator() {
			return RawTextComparator.WS_IGNORE_ALL;
		}
		
		@Override
		public String getDescription() {
			return "Ignore all white spaces";
		}
		
	};	

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	
	public abstract String getDescription();
	
	public abstract String process(String line);
	
	public abstract RawTextComparator getComparator();
	
	public static WhitespaceOption of(@Nullable String name) {
		if (name != null) {
			return valueOf(name);
		} else {
			return DEFAULT;
		}
	}
}
