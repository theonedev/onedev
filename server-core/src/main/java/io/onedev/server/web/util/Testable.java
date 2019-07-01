package io.onedev.server.web.util;

import java.io.Serializable;

import io.onedev.server.util.JobLogger;

public interface Testable<T extends Serializable> {
	
	void test(T data, JobLogger logger);
	
}
