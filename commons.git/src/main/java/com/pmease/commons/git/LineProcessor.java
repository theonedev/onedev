package com.pmease.commons.git;

public interface LineProcessor {
	
	String getName();
	
	String process(String line);
	
}