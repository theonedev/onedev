package com.pmease.commons.git;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class LineReadResult {
	
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	
	private final List<String> lines;
	
	private final Charset charset;

	private final boolean hasEolAtEof;
	
	public LineReadResult(List<String> lines, Charset charset, boolean hasEolAtEof) {
		this.lines = lines;
		this.charset = charset;
		this.hasEolAtEof = hasEolAtEof;
	}

	public boolean isHasEolAtEof() {
		return hasEolAtEof;
	}

	public List<String> getLines() {
		return lines;
	}

	public Charset getCharset() {
		return charset;
	}

	public LineReadResult ignoreEOL() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines) {
			if (line.endsWith("\r"))
				line = line.substring(0, line.length()-1);
			processedLines.add(line);
		}
		return new LineReadResult(processedLines, charset, hasEolAtEof);
	}
	
	public LineReadResult ignoreEOLWhitespaces() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines)
			processedLines.add(StringUtils.stripEnd(line, " \t\r"));
		return new LineReadResult(processedLines, charset, hasEolAtEof);
	}
	
	public LineReadResult ignoreWhitespaces() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines) {
			line = WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");			
			processedLines.add(line);
		}
		return new LineReadResult(processedLines, charset, hasEolAtEof);
	}

}
