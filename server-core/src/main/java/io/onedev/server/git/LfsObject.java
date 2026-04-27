package io.onedev.server.git;

import static io.onedev.server.util.IOUtils.BUFFER_SIZE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.regex.Pattern;

import org.apache.tika.mime.MediaType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.ContentDetector;

public class LfsObject implements Serializable {
	
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
					super.close();
					readLock.unlock();
				}
				
			};
		} catch (FileNotFoundException e) {
			readLock.unlock();
			throw new RuntimeException(e);
		}
	}
	
	public OutputStream getOutputStream() {
		Lock writeLock = getLock().writeLock();
		writeLock.lock();
		try {
			return new FilterOutputStream(new BufferedOutputStream(new FileOutputStream(getFile()), BUFFER_SIZE)) {
				@Override
				public void write(@NotNull byte[] b, int off, int len) throws IOException {
					out.write(b, off, len);
				}

				@Override
				public void close() throws IOException {
					super.close();
					writeLock.unlock();
				}
				
			};
		} catch (FileNotFoundException e) {
			writeLock.unlock();
			throw new RuntimeException(e);
		}
	}
	
	public void delete() {
		getProjectService().runOnActiveServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() {
				Lock writeLock = getLock().writeLock();
				writeLock.lock();
				try {
					FileUtils.deleteFile(getFile());
				} finally {
					writeLock.unlock();
				}
				return null;
			}
			
		});
	}
	
	public MediaType detectMediaType(String fileName) {
		return getProjectService().runOnActiveServer(projectId, new ClusterTask<MediaType> () {

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
