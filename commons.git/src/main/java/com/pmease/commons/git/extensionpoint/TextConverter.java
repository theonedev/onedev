package com.pmease.commons.git.extensionpoint;

import java.util.List;

public interface TextConverter {
	List<String> convert(byte[] content);
}
