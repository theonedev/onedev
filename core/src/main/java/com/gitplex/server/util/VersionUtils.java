package com.gitplex.server.util;

import java.io.File;
import java.io.IOException;

public class VersionUtils {
	
	private static final String INFO_VERSION_FILE = "version.txt";
	
	public static void checkInfoVersion(File infoDir, int infoVersion) {
		File versionFile = new File(infoDir, INFO_VERSION_FILE);
		int infoVersionFromFile;
		if (versionFile.exists()) {
			try {
				infoVersionFromFile = Integer.parseInt(FileUtils.readFileToString(versionFile).trim());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			infoVersionFromFile = 0;
		}
		if (infoVersionFromFile != infoVersion) {
			FileUtils.cleanDir(versionFile.getParentFile());
			FileUtils.writeFile(versionFile, String.valueOf(infoVersion));
		} 
	}
	
	public static void writeInfoVersion(File infoDir, int infoVersion) {
		File versionFile = new File(infoDir, INFO_VERSION_FILE);
		FileUtils.writeFile(versionFile, String.valueOf(infoVersion));
	}
	
}
