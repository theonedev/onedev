package com.pmease.commons.git;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.util.Charsets;

@SuppressWarnings("serial")
public class GitText implements Serializable {
	
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	
	private final List<String> lines;
	
	private final boolean hasEolAtEof;
	
	private final String charset;
	
	public GitText(List<String> lines, boolean hasEolAtEof, String charset) {
		this.lines = lines;
		this.hasEolAtEof = hasEolAtEof;
		this.charset = charset;
	}

	public boolean isHasEolAtEof() {
		return hasEolAtEof;
	}

	public List<String> getLines() {
		return lines;
	}

	public String getCharset() {
		return charset;
	}

	public GitText ignoreEOL() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines) {
			if (line.endsWith("\r"))
				line = line.substring(0, line.length()-1);
			processedLines.add(line);
		}
		return new GitText(processedLines, hasEolAtEof, charset);
	}
	
	public GitText ignoreEOLSpaces() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines)
			processedLines.add(StringUtils.stripEnd(line, " \t\r"));
		return new GitText(processedLines, hasEolAtEof, charset);
	}
	
	public GitText ignoreChangeSpaces() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines) {
			line = WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");			
			processedLines.add(line);
		}
		return new GitText(processedLines, hasEolAtEof, charset);
	}

	/**
	 * Read text from specified bytes. This method only treats "\n" as EOL character, 
	 * and any occurrences of "\r" will be preserved in read result. This method is 
	 * intended to read text files for comparison purpose. 
	 * 
	 * @param bytes
	 * 			bytes to read lines from
	 * @return
	 * 			resulting GitText, <tt>null</tt> if charset can not be detected
	 */
	public static @Nullable GitText from(byte[] bytes) {
		Charset charset = Charsets.detectFrom(bytes);
		if (charset != null) {
			List<String> lines = new ArrayList<>();
			StringBuilder builder = new StringBuilder();
			String string = new String(bytes, charset);
			for (int i=0; i<string.length(); i++) {
				char ch = string.charAt(i);
				if (ch == '\n') {
					lines.add(builder.toString());
					builder = new StringBuilder();
				} else {
					builder.append(ch);
				}
			}
			if (builder.length() != 0) {
				lines.add(builder.toString());
				return new GitText(lines, false, charset.name());
			} else {
				return new GitText(lines, true, charset.name());
			}
		} else {
			return null;
		}
	}

}
