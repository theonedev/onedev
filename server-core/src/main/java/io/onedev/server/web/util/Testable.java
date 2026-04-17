package io.onedev.server.web.util;

import java.io.Serializable;

import io.onedev.commons.utils.TaskLogger;

public interface Testable<T extends Serializable> {
	
	void test(T data, TaskLogger logger);
	
	public static class None implements Serializable {

		private static final long serialVersionUID = 1L;

	}
	
}
