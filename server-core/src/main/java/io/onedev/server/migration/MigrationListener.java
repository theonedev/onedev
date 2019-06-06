package io.onedev.server.migration;

public interface MigrationListener {
	
	void afterMigration(Object bean);
	
}
