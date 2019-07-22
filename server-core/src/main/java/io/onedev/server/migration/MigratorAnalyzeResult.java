package io.onedev.server.migration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigratorAnalyzeResult {
	
	private Map<String, Integer> migrateVersions = new HashMap<String, Integer>();
	
	private List<Method> migrateMethods = new ArrayList<Method>();

	public Map<String, Integer> getMigrateVersions() {
		return migrateVersions;
	}
	
	public List<Method> getMigrateMethods() {
		return migrateMethods;
	}
	
	public int getDataVersion() {
		int size = migrateMethods.size();
		if (size != 0)
			return migrateVersions.get(migrateMethods.get(size - 1).getName());
		else
			return 0;
	}
}	
