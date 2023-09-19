package io.onedev.server.data;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.util.init.ManualConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.sql.Connection;
import java.util.List;

public interface DataManager {

	String checkDataVersion(Connection conn, boolean allowEmptyDB);
	
	void populateDatabase(Connection conn);
	
	void migrateData(File dataDir);

	void exportData(File exportDir);
	
	void exportData(File exportDir, int batchSize);	
	
	void importData(File dataDir);
	
	void applyConstraints(Connection conn);
	
	void createTables(Connection conn);
	
	void dropConstraints(Connection conn);
	
	void cleanDatabase(Connection conn);
	
	void scheduleBackup(@Nullable BackupSetting backupSetting);
	
	List<ManualConfig> checkData();
	
	String getTableName(Class<? extends AbstractEntity> entityClass);
	
	String getColumnName(String fieldName);
	
	Connection openConnection();
	
}
