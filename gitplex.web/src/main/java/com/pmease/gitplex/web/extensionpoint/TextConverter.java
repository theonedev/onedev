package com.pmease.gitplex.web.extensionpoint;

import java.util.List;

public interface TextConverter {
	List<String> convert(byte[] content);
}
