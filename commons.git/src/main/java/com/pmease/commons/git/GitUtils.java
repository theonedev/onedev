package com.pmease.commons.git;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.Charsets;

public class GitUtils {
	
	/**
	 * Read lines from specified bytes. This method only treats "\n" as EOL character, 
	 * and any occurrences of "\r" will be preserved in read result. This method is 
	 * intended to read text files for comparison purpose. 
	 * 
	 * @param bytes
	 * 			bytes to read lines from
	 * @return
	 * 			<tt>null</tt> if character encoding can not be detected, for instance 
	 * 			when bytes represents binary data
	 */
	public static LineReadResult readLines(byte[] bytes) {
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
				return new LineReadResult(lines, charset, false);
			} else {
				return new LineReadResult(lines, charset, true);
			}
		} else {
			return null;
		}
	}
	
}
