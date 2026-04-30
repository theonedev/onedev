package io.onedev.server.util;

import static io.onedev.commons.utils.LockUtils.getLock;
import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;

public class SiteSyncUtils {

	private static final Logger logger = LoggerFactory.getLogger(SiteSyncUtils.class);

	public static final String FILE_VERSION = ".onedev-directory-version";

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

	public static void bumpVersions(File rootDir, File directory) {
		var rootPath = rootDir.toPath();	
		var currentPath = directory.toPath();
		while (currentPath.startsWith(rootPath)) {
			var currentDir = currentPath.toFile();
			increaseVersion(currentDir);
			currentPath = currentPath.getParent();
		}
	}
	
	public static void syncDirectory(String remoteServer, String path, Consumer<String> childSyncer, 
			boolean deleteLocalIfRemoteIsMissing) {
		var directory = new File(Bootstrap.getSiteDir(), path);
		
		var clusterService = getClusterService();
		long remoteVersion = clusterService.runOnServer(remoteServer, () -> readVersion(new File(Bootstrap.getSiteDir(), path)));
		long version = readVersion(directory);
		
		if (version < remoteVersion) {
			logger.debug("Syncing site directory '{}' from server '{}'...", path, remoteServer);

			Collection<String> remoteChildren = clusterService.runOnServer(remoteServer, () -> {
				var children = new HashSet<String>();
				for (var file: new File(Bootstrap.getSiteDir(), path).listFiles()) {
					if (!isVersionFile(file))
						children.add(file.getName());
				}
				return children;
			});								
			
			FileUtils.createDir(directory);
			for (var file: directory.listFiles()) {
				if (!isVersionFile(file)) {
					if (remoteChildren.remove(file.getName())) {
						childSyncer.accept(file.getName());
					} else if (deleteLocalIfRemoteIsMissing) {
						if (file.isFile())
							FileUtils.deleteFile(file);
						else
							FileUtils.deleteDir(file);
					}
				}
			}
			for (var child: remoteChildren)
				childSyncer.accept(child);
			
			writeVersion(directory, remoteVersion);
		}
	}

	private static void downloadDirectory(String remoteServer, String path, File targetDir, @Nullable String readLock) {
		Client client = ClientBuilder.newClient();
		try {
			var clusterService = getClusterService();
			String fromServerUrl = clusterService.getServerUrl(remoteServer);
			WebTarget target = client.target(fromServerUrl).path("/~api/cluster/site-files")
					.queryParam("path", path)
					.queryParam("patterns", "** -" + FILE_VERSION)
					.queryParam("readLock", readLock);
			Invocation.Builder builder = target.request();
			builder.header(AUTHORIZATION,
					BEARER + " " + clusterService.getCredential());

			try (Response response = builder.get()) {
				KubernetesHelper.checkStatus(response);
				try (InputStream is = response.readEntity(InputStream.class)) {
					TarUtils.untar(is, targetDir, false);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			client.close();
		}
	}

	public static void syncDirectory(String remoteServer, String path, 
			boolean deleteLocalIfRemoteIsMissing, @Nullable String readLock, @Nullable String writeLock) {
		var directory = new File(Bootstrap.getSiteDir(), path);

		long version;
		if (writeLock != null) 
			version = write(writeLock, () -> readVersion(directory));
		else 
			version = readVersion(directory);

		var clusterService = getClusterService();
		long remoteVersion = clusterService.runOnServer(remoteServer, () -> {
			var remoteDirectory = new File(Bootstrap.getSiteDir(), path);
			if (readLock != null)
				return read(readLock, () -> readVersion(remoteDirectory));
			else
				return readVersion(remoteDirectory);
		});

		if (version < remoteVersion) {
			logger.debug("Syncing site directory '{}' from server '{}'...", path, remoteServer);

			if (writeLock != null) {
				write(writeLock, () -> {
					var tempDir = FileUtils.createTempDir();
					try {
						downloadDirectory(remoteServer, path, tempDir, readLock);
						if (deleteLocalIfRemoteIsMissing) {
							FileUtils.deleteDir(directory);
							FileUtils.moveDirectory(tempDir, directory);
						} else {
							FileUtils.copyDirectory(tempDir, directory);
						}
						writeVersion(directory, remoteVersion);
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						FileUtils.deleteDir(tempDir);
					}
				});
			} else {
				if (deleteLocalIfRemoteIsMissing)
					FileUtils.cleanDir(directory);
				else
					FileUtils.createDir(directory);
				downloadDirectory(remoteServer, path, directory, readLock);
				writeVersion(directory, remoteVersion);
			}
		}
	}

	private static boolean downloadFile(String remoteServer, String path, File targetFile, @Nullable String readLock) {
		Client client = ClientBuilder.newClient();
		try {
			var clusterService = getClusterService();
			String fromServerUrl = clusterService.getServerUrl(remoteServer);
			WebTarget target = client.target(fromServerUrl).path("/~api/cluster/site-file")
					.queryParam("path", path)
					.queryParam("readLock", readLock);
			Invocation.Builder builder = target.request();
			builder.header(AUTHORIZATION, BEARER + " " + clusterService.getCredential());
			try (Response response = builder.get()) {
				if (response.getStatus() != NO_CONTENT.getStatusCode()) {
					FileUtils.createDir(targetFile.getParentFile());
					KubernetesHelper.checkStatus(response);
					try (
							var is = response.readEntity(InputStream.class);
							var os = new BufferedOutputStream(new FileOutputStream(targetFile), BUFFER_SIZE)) {
						IOUtils.copy(is, os, BUFFER_SIZE);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return true;
				} else {
					return false;
				}
			}
		} finally {
			client.close();
		}
	}

	public static void syncFile(String fromServer, String path, boolean deleteLocalIfRemoteIsMissing, 
				@Nullable String readLock, @Nullable String writeLock) {
		var file = new File(Bootstrap.getSiteDir(), path);
		if (writeLock != null) {
			write(writeLock, () -> {
				var tempFile = FileUtils.createTempFile();
				try {
					if (downloadFile(fromServer, path, tempFile, readLock)) {
						FileUtils.deleteFile(file);
						FileUtils.moveFile(tempFile, file);
					} else if (deleteLocalIfRemoteIsMissing && file.exists()) {
						FileUtils.deleteFile(file);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					FileUtils.deleteFile(tempFile);
				}
			});
		} else if (!downloadFile(fromServer, path, file, readLock) 
				&& deleteLocalIfRemoteIsMissing 
				&& file.exists()) {
			FileUtils.deleteFile(file);
		}
	}

	@Nullable
	public static String findNewestServer(String path) {
		var directory = new File(Bootstrap.getSiteDir(), path);
		long version = readVersion(directory);
		var remoteVersions = getClusterService().runOnAllServers(() -> {
			return readVersion(new File(Bootstrap.getSiteDir(), path));
		});
		return remoteVersions.entrySet().stream()
				.filter(it -> !it.getKey().equals(getClusterService().getLocalServerAddress()) && it.getValue() > version)
				.max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.orElse(null);
	}

	private static ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}

}
