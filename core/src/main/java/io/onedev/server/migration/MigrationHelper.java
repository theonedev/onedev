package io.onedev.server.migration;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;

public class MigrationHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(MigrationHelper.class);
	
	private static final Pattern migrateMethodPattern = 
		Pattern.compile("^migrate(\\d+)$");
	
	private static Map<String, MigratorAnalyzeResult> migratorAnalyzeResults = 
		new ConcurrentHashMap<String, MigratorAnalyzeResult>();

	private static MigratorAnalyzeResult getMigratorAnalyzeResult(Class<?> migrator) {
		MigratorAnalyzeResult migratorAnalyzeResult = 
			migratorAnalyzeResults.get(migrator.getName());
		if (migratorAnalyzeResult == null) {
			final MigratorAnalyzeResult newMigratorAnalyzeResult = 
				new MigratorAnalyzeResult();

			Method[] methods = migrator.getDeclaredMethods();
			for (int i=0; i<methods.length; i++) {
				Method method = methods[i];
				int migrateVersion = getVersion(method);
				if (migrateVersion != 0) { 
					if (Modifier.isPrivate(method.getModifiers()) && 
							!Modifier.isStatic(method.getModifiers())) {
						method.setAccessible(true);
						newMigratorAnalyzeResult.getMigrateVersions()
								.put(method.getName(), migrateVersion);
						newMigratorAnalyzeResult.getMigrateMethods().add(method);
					} else {
						throw new RuntimeException("Migrate method should be declared " +
								"as a private non-static method.");
					}
				}
			}

			Collections.sort(newMigratorAnalyzeResult.getMigrateMethods(), 
					new Comparator<Method>() {

				public int compare(Method migrate_x, Method migrate_y) {
					return newMigratorAnalyzeResult.getMigrateVersions().get(migrate_x.getName()) - 
							newMigratorAnalyzeResult.getMigrateVersions().get(migrate_y.getName());
				}
				
			});
			migratorAnalyzeResults.put(migrator.getName(), newMigratorAnalyzeResult);
			return newMigratorAnalyzeResult;
		} else {
			return migratorAnalyzeResult;
		}
	}

	private static int getVersion(Method method) {
		Matcher matcher = migrateMethodPattern.matcher(method.getName());
		if (matcher.find()) {
			int migrateVersion = Integer.parseInt(matcher.group(1));
			if (migrateVersion == 0) {
				throw new RuntimeException("Invalid migrate method name: " + method.getName());
			}
			return migrateVersion;
		} else 
			return 0;
	}

	public static String getVersion(Class<?> migratorClass) {
		List<String> versionParts = new ArrayList<String>();
		Class<?> current = migratorClass;
		while (current != null && current != Object.class) {
			versionParts.add(String.valueOf(getMigratorAnalyzeResult(current).getDataVersion()));
			current = current.getSuperclass();
		}
		Collections.reverse(versionParts);
		return StringUtils.join(versionParts, '.');
	}
	
	public static boolean migrate(String fromVersion, Object migrator, Object customData) {
		Stack<Integer> versionParts = new Stack<Integer>();
		for (String part: StringUtils.split(fromVersion, "."))
			versionParts.push(Integer.valueOf(part));
		
		boolean migrated = false;
		
		Class<?> current = migrator.getClass();
		while (current != null && current != Object.class) {
			MigratorAnalyzeResult migratorAnalyzeResult = getMigratorAnalyzeResult(current);
			
			int version;
			if (!versionParts.empty())
				version = versionParts.pop();
			else 
				version = 0;
			int size = migratorAnalyzeResult.getMigrateMethods().size();
			int start;
			if (version != 0) {
				start = size;
				for (int i=0; i<size; i++) {
					Method method = migratorAnalyzeResult.getMigrateMethods().get(i);
					if (method.getName().equals("migrate" + version)) {
						start = i;
						break;
					}
				}
				if (start == size) {
					throw new RuntimeException("Can not find migrate method (migrator: " + current.getName() + ", method: " + "migrate" + version + ")"); 
				} else {
					start++;
				}
			} else {
				start = 0;
			}
			
			for (int i=start; i<size; i++) {
				Method migrateMethod = migratorAnalyzeResult.getMigrateMethods().get(i);
				int previousVersion;
				if (i != 0) {
					Method previousMigrateMethod =
						migratorAnalyzeResult.getMigrateMethods().get(i-1);
					previousVersion = migratorAnalyzeResult.getMigrateVersions()
							.get(previousMigrateMethod.getName());
				} else {
					previousVersion = 0;
				}
				int currentVersion = migratorAnalyzeResult.getMigrateVersions()
						.get(migrateMethod.getName());
				Object[] params = new String[]{current.getName(), 
						String.valueOf(previousVersion), 
						String.valueOf(currentVersion)};
				logger.debug("Migrating data (migrator: {}, from version: {}, " + "to version: {})", params);
				try {
					migrateMethod.invoke(migrator, customData, versionParts);
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
				migrated = true;
			}
			current = current.getSuperclass();
		}
		return migrated;
	}	
}
