package io.onedev.server.persistence;

public interface IdManager {
	
	void init();
	
	long nextId(Class<?> entityClass);
	
	void useId(Class<?> entityClass, long id);
	
}
