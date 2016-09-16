package com.pmease.commons.hibernate;

import java.io.File;
import java.util.List;

public interface PersistManager {
	
	void start();
	
	void stop();
	
	boolean isReady();
	
	String getDbDataVersion();
	
	/**
	 * @return hibernate entity types ordered using foreign key dependency
	 *         information. For example, Build type comes before Configuration
	 *         type as it has Build has foreign key reference to Configuration.
	 */
	List<Class<?>> getEntityTypes();

	void migrate(File dataDir);
	
	void exportData(File dataDir, int batchSize);

	// This method should only be called by a bootstrap action. That is, when the 
	// server is stopped. So it does not need an exclusive database lock.
	void importData(File dataDir);
	
}
