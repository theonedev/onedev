package com.pmease.commons.hibernate;

public interface IdManager {
	
	void init();
	
	void init(Class<?> entityClass);
	
	long nextId(Class<?> entityClass);
	
}
