package com.pmease.commons.lang.tokenizers;

import java.util.List;

import javax.annotation.Nullable;

public interface Tokenizers {
	/**
	 * Tokenize specified lines in language indicated by specified file name
	 * 
	 * @param lines
	 * 			list of lines to be tokenized
	 * @param fileName
	 * 			this param indicates language of passed lines
	 * @return
	 * 			tokenized lines, or <tt>null</tt> if fileName does not indicate a supported language 
	 */
	@Nullable List<List<Token>> tokenize(List<String> lines, String fileName);
}
