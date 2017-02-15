package com.gitplex.server.persistence;

public interface IdManager {
	
	void init();
	
	void init(Class<?> entityClass);
	
	long nextId(Class<?> entityClass);
	
}
