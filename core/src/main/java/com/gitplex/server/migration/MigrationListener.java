package com.gitplex.server.migration;

public interface MigrationListener {
	
	void afterMigration(Object bean);
	
}
