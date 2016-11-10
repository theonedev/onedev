package com.gitplex.commons.lang.extractors;

import javax.annotation.Nullable;

public interface Extractors {

	@Nullable Extractor getExtractor(String fileName);

	String getVersion();
}
