package com.pmease.commons.lang;

import javax.annotation.Nullable;

public interface Extractors {

	@Nullable Extractor getExtractor(String fileName);

	String getVersion();
}
