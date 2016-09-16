package com.pmease.commons.hibernate.migration;

public interface MigrationListener {
	
	void afterMigration(Object bean);
	
}
