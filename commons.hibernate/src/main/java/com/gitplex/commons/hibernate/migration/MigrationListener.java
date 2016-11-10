package com.gitplex.commons.hibernate.migration;

public interface MigrationListener {
	
	void afterMigration(Object bean);
	
}
