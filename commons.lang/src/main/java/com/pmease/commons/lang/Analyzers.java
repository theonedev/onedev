package com.pmease.commons.lang;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public interface Analyzers {
	/**
	 * Analyze specified text in language indicated by specified file name
	 * 
	 * @param fileContent
	 * 			content of file to be analyzed
	 * @param fileName
	 * 			name of file to be analyzed
	 * @return
	 * 			analyze result, or <tt>null</tt> if fileName does not indicate a supported language 
	 */
	@Nullable AnalyzeResult analyze(String fileContent, String fileName);

	String getVersion();
	
	@Nullable
	String getVersion(String fileName);
}
