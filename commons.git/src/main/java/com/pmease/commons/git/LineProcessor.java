package com.pmease.commons.git;

import org.apache.commons.lang3.StringUtils;

public interface LineProcessor {
	
	String process(String line);
	
	public static class IgnoreEOL implements LineProcessor {

		@Override
		public String process(String line) {
			return StringUtils.stripEnd(line, "\r\n");
		}
		
	}
	
	public static class IgnoreEOLSpaces implements LineProcessor {

		@Override
		public String process(String line) {
			return StringUtils.stripEnd(line, " \t\r\n");
		}
		
	}
	
	public static class IgnoreChangeSpaces implements LineProcessor {

		@Override
		public String process(String line) {
			return Blob.Text.WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");
		}
		
	}
}