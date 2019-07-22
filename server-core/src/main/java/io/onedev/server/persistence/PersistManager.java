package io.onedev.server.persistence;

import java.io.File;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;

public interface PersistManager {
	
	SessionFactory getSessionFactory();
	
	void start();
	
	void stop();
	
	void exportData(File dataDir, int batchSize);

	void exportData(File dataDir);
	
	// This method should only be called by a bootstrap action. That is, when the 
	// server is stopped. So it does not need an exclusive database lock.
	void importData(Metadata metadata, File dataDir);

}
