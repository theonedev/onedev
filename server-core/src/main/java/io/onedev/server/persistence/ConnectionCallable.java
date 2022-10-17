package io.onedev.server.persistence;

import java.sql.Connection;

public interface ConnectionCallable<T> {
	
	T call(Connection conn);
	
}