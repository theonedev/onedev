package io.onedev.server.data.migration;

public interface MigrationListener {
	
	void afterMigration(Object bean);
	
}
