package io.onedev.server.web.util;

import java.io.Serializable;

public interface Testable<T extends Serializable> {
	
	void test(T data);
	
}
