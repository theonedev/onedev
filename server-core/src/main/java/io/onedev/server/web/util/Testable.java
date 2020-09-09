package io.onedev.server.web.util;

import java.io.Serializable;

import io.onedev.server.util.SimpleLogger;

public interface Testable<T extends Serializable> {
	
	void test(T data, SimpleLogger logger);
	
}
