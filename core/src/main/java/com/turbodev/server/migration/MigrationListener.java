package com.turbodev.server.migration;

public interface MigrationListener {
	
	void afterMigration(Object bean);
	
}
