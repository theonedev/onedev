package io.onedev.server.util;

import io.onedev.commons.utils.FileUtils;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

import static io.onedev.commons.utils.LockUtils.getLock;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DirectoryVersionUtils {

	public static String FILE_VERSION = ".onedev-directory-version";

	private static <T> T callWithVersionLock(java.nio.file.Path path, Callable<T> callable) {
		Lock lock = getLock("directory-version: " + path.normalize().toString());
		lock.lock();
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}
	
	public static long readVersion(File directory) {
		return callWithVersionLock(directory.toPath(), () -> {
			var versionFile = new File(directory, FILE_VERSION);
			if (versionFile.exists()) {
				try {
					return Long.parseLong(FileUtils.readFileToString(versionFile, UTF_8).trim());
				} catch (Exception e) {
					throw new RuntimeException("Error reading directory version from file: " + versionFile, e);
				}
			} else {
				return 0L;
			}
		});
	}

	public static void writeVersion(File directory, long version) {
		callWithVersionLock(directory.toPath(), () -> {
			var versionFile = new File(directory, FILE_VERSION);
			FileUtils.writeStringToFile(versionFile, String.valueOf(version), UTF_8);
			return null;
		});
	}
	
	public static void increaseVersion(File directory) {
		callWithVersionLock(directory.toPath(), () -> {
			writeVersion(directory, readVersion(directory)+1);
			return null;
		});
	}
	
	public static boolean isVersionFile(File file) {
		return file.getName().equals(FILE_VERSION);
	}
	
}
