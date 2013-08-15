package com.pmease.commons.util.namedentity;

public interface EntityLoader {
	
	NamedEntity get(Long id);
	
	NamedEntity get(String name);
	
}
