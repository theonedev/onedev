package io.onedev.server.web.util;

import java.io.Serializable;

import io.onedev.commons.utils.TaskLogger;

public interface Testable<T extends Serializable> {
	
	void test(T data, TaskLogger logger);
	
}
