package io.onedev.server.git;

import static com.google.common.hash.Hashing.sha256;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static jakarta.servlet.http.HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.regex.Pattern;

import org.apache.tika.mime.MediaType;
import org.jspecify.annotations.Nullable;

import com.google.common.hash.HashingInputStream;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.exception.HttpResponseAwareException;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.util.IOUtils;

public class LfsObject implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * A Git LFS oid is a lowercase hex SHA-256 digest (64 characters). Anything
	 * else is invalid and must never be used as a filesystem path component, as
	 * the on-disk layout below derives the storage path directly from it.
	 */
	private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("[0-9a-f]{64}");

	private final Long projectId;
	
	private final String objectId;
	
	public LfsObject(Long projectId, String objectId) {
		if (!isValidObjectId(objectId))
			throw new ExplicitException("Invalid LFS object id");
		this.projectId = projectId;
		this.objectId = objectId;
	}

	public static boolean isValidObjectId(@Nullable String objectId) {
		return objectId != null && OBJECT_ID_PATTERN.matcher(objectId).matches();
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getObjectId() {
		return objectId;
	}

	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
	
	private File getFile() {
		File objectDir = new File(
				getProjectService().getLfsObjectsDir(projectId), 
				objectId.substring(0, 2) + "/" + objectId.substring(2, 4));
		String lockName = "lfs-storage:" 
				+ getProjectService().getGitDir(projectId).getAbsolutePath();
		Lock lock = LockUtils.getLock(lockName);
		lock.lock();
		try {
			FileUtils.createDir(objectDir);
		} finally {
			lock.unlock();
		}
		return new File(objectDir, objectId);
	}
	
	private ReadWriteLock getLock() {
		return LockUtils.getReadWriteLock("lfs-objects:" + objectId);
	}

	public boolean exists() {
		return getProjectService().runOnActiveServer(projectId, new ClusterTask<Boolean>() {

			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public Boolean call() {
				Lock readLock = getLock().readLock();
				readLock.lock();
				try {
					return getFile().exists();
				} finally {
					readLock.unlock();
				}
			}
			
		});
	}
	
	public InputStream getInputStream() {
		Lock readLock = getLock().readLock();
		readLock.lock();
		try {
			return new FilterInputStream(new FileInputStream(getFile())) {

				@Override
				public void close() throws IOException {
					try {
						super.close();
					} finally {
						readLock.unlock();
					}
				}
				
			};
		} catch (FileNotFoundException e) {
			readLock.unlock();
			throw new RuntimeException(e);
		}
	}
	
	public static void copyUpload(InputStream input, OutputStream output) throws IOException {
		long maxSize = (long) OneDev.getInstance(SettingService.class)
				.getPerformanceSetting().getMaxGitLFSFileSize() * 1024 * 1024;
		// Read one extra byte to distinguish an exact-size upload from an oversized one.
		if (maxSize < 0 || IOUtils.copyLarge(input, output, 0, maxSize + 1, new byte[BUFFER_SIZE]) > maxSize)
			throw new HttpResponseAwareException(SC_REQUEST_ENTITY_TOO_LARGE,
					"Exceeded max acceptable LFS file size " + maxSize);
	}

	public void upload(InputStream input) throws IOException {
		File file = getFile();
		// Keep the upload on the same filesystem so publishing it can be atomic.
		var tempFile = Files.createTempFile(file.getParentFile().toPath(), "upload-", ".tmp");
		try {
			var is = new HashingInputStream(sha256(), input);
			try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(tempFile), BUFFER_SIZE)) {
				copyUpload(is, os);
			}
			if (!objectId.equals(is.hash().toString()))
				throw new ExplicitException("Invalid uploaded content: hash not equals to object id");

			Lock writeLock = getLock().writeLock();
			writeLock.lock();
			try {
				Files.move(tempFile, file.toPath(), ATOMIC_MOVE, REPLACE_EXISTING);
			} finally {
				writeLock.unlock();
			}
		} finally {
			Files.deleteIfExists(tempFile);
		}
	}
	
	public void delete() {
		getProjectService().runOnActiveServer(projectId, new ClusterTask<Void>() {

			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public Void call() {
				Lock writeLock = getLock().writeLock();
				writeLock.lock();
				try {
					FileUtils.deletePath(getFile());
				} finally {
					writeLock.unlock();
				}
				return null;
			}
			
		});
	}
	
	public MediaType detectMediaType(String fileName) {
		return getProjectService().runOnActiveServer(projectId, new ClusterTask<MediaType> () {

			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public MediaType call() {
				try (InputStream is = getInputStream()) {
					return ContentDetector.detectMediaType(is, fileName);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
	}
	
}
