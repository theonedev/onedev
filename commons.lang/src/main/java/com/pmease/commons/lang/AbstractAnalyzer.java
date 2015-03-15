package com.pmease.commons.lang;

import java.util.regex.Pattern;

import com.pmease.commons.util.StringUtils;

public abstract class AbstractAnalyzer implements Analyzer {

	protected boolean acceptExtensions(String fileName, String...exts) {
		String thisExt = StringUtils.substringAfterLast(fileName, ".");
		for (String ext: exts) {
			if (ext.equalsIgnoreCase(thisExt))
				return true;
		}
		return false;
	}

	protected boolean acceptPattern(String fileName, Pattern pattern) {
		return pattern.matcher(fileName).matches();
	}

}
