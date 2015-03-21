package com.pmease.commons.lang;

import java.util.regex.Pattern;

import com.pmease.commons.util.StringUtils;

public abstract class AbstractExtractor implements Extractor {

	protected boolean acceptExtensions(String fileName, String...exts) {
		String fileExt = StringUtils.substringAfterLast(fileName, ".");
		for (String ext: exts) {
			if (ext.equalsIgnoreCase(fileExt))
				return true;
		}
		return false;
	}

	protected boolean acceptPattern(String fileName, Pattern pattern) {
		return pattern.matcher(fileName).matches();
	}

}
