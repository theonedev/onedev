package com.pmease.commons.git;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.collect.Lists;
import com.pmease.commons.git.extensionpoint.TextConverter;
import com.pmease.commons.git.extensionpoint.TextConverterProvider;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.MediaTypes;

@SuppressWarnings("serial")
public class BlobText implements Serializable {
	
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	
	private final List<String> lines;
	
	private final boolean hasEolAtEof;
	
	private final MediaType mediaType;
	
	private final String charset;
	
	public BlobText(List<String> lines, boolean hasEolAtEof, String charset, MediaType mediaType) {
		this.lines = lines;
		this.hasEolAtEof = hasEolAtEof;
		this.charset = charset;
		this.mediaType = mediaType;
	}

	public BlobText(List<String> lines, boolean hasEolAtEof, String charset) {
		this(lines, hasEolAtEof, charset, MediaType.TEXT_PLAIN);
	}

	public BlobText(List<String> lines) {
		this(lines, true, Charsets.UTF_8.name());
	}

	public BlobText(String line) {
		this(Lists.newArrayList(line));
	}

	public BlobText() {
		this(new ArrayList<String>());
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

	public MediaType getMediaType() {
		return mediaType;
	}

	public BlobText ignoreEOL() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines) {
			if (line.endsWith("\r"))
				line = line.substring(0, line.length()-1);
			processedLines.add(line);
		}
		return new BlobText(processedLines, hasEolAtEof, charset, mediaType);
	}
	
	public BlobText ignoreEOLSpaces() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines)
			processedLines.add(StringUtils.stripEnd(line, " \t\r"));
		return new BlobText(processedLines, hasEolAtEof, charset, mediaType);
	}
	
	public BlobText ignoreChangeSpaces() {
		List<String> processedLines = new ArrayList<>();
		for (String line: lines) {
			line = WHITESPACE_PATTERN.matcher(line.trim()).replaceAll(" ");			
			processedLines.add(line);
		}
		return new BlobText(processedLines, hasEolAtEof, charset, mediaType);
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
	public static @Nullable BlobText from(byte[] blobContent, String blobPath, int blobMode) {
		if (blobMode == FileMode.TYPE_GITLINK || blobMode == FileMode.TYPE_SYMLINK) 
			return new BlobText(new String(blobContent));
		
		MediaType mediaType = MediaTypes.detectFrom(blobContent, blobPath);

		for (TextConverterProvider provider: AppLoader.getExtensions(TextConverterProvider.class)) {
			TextConverter textConverter = provider.getTextConverter(mediaType);
			if (textConverter != null) 
				return new BlobText(textConverter.convert(blobContent), true, Charsets.UTF_8.name(), mediaType);
		}
		
		Charset charset = Charsets.detectFrom(blobContent);
		if (charset != null) {
			List<String> lines = new ArrayList<>();
			StringBuilder builder = new StringBuilder();
			String string = new String(blobContent, charset);
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
				return new BlobText(lines, false, charset.name());
			} else {
				return new BlobText(lines, true, charset.name());
			}
		} else {
			return null;
		}
	}

	public static @Nullable BlobText from(byte[] blobContent) {
		return from(blobContent, null, FileMode.TYPE_FILE);
	}
}
