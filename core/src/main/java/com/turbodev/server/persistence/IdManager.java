package com.turbodev.server.persistence;

public interface IdManager {
	
	void init();
	
	void init(Class<?> entityClass);
	
	long nextId(Class<?> entityClass);
	
}
