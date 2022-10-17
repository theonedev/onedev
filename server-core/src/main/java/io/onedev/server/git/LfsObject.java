package io.onedev.server.git;

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

import org.apache.tika.mime.MediaType;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ContentDetector;

public class LfsObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final String objectId;
	
	public LfsObject(Long projectId, String objectId) {
		this.projectId = projectId;
		this.objectId = objectId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getObjectId() {
		return objectId;
	}

	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private StorageManager getStorageManager() {
		return OneDev.getInstance(StorageManager.class);
	}
	
	private File getFile() {
		File objectDir = new File(
				getProjectManager().getLfsObjectsDir(projectId), 
				objectId.substring(0, 2) + "/" + objectId.substring(2, 4));
		String lockName = "lfs-storage:" 
				+ getStorageManager().getProjectGitDir(projectId).getAbsolutePath();
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
		return getProjectManager().runOnProjectServer(projectId, new ClusterTask<Boolean>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Boolean call() throws Exception {
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
			return new FilterOutputStream(new FileOutputStream(getFile())) {

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
		getProjectManager().runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
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
		return getProjectManager().runOnProjectServer(projectId, new ClusterTask<MediaType> () {

			private static final long serialVersionUID = 1L;

			@Override
			public MediaType call() throws Exception {
				try (InputStream is = getInputStream()) {
					return ContentDetector.detectMediaType(is, fileName);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
	}
	
}
