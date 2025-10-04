package io.onedev.server.persistence;

public interface IdService {
	
	void init();
	
	long nextId(Class<?> entityClass);
	
	void useId(Class<?> entityClass, long id);
	
}
