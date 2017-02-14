package com.gitplex.commons.hibernate;

public interface IdManager {
	
	void init();
	
	void init(Class<?> entityClass);
	
	long nextId(Class<?> entityClass);
	
}
