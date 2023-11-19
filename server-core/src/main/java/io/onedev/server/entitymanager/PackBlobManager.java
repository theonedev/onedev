package io.onedev.server.entitymanager;

import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.util.Pair;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface PackBlobManager extends EntityManager<PackBlob> {
	
	@Nullable
    PackBlob find(String hash);
	
	boolean checkPackBlobFile(Long projectId, String hash, long size);
	
	void initUpload(Long projectId, String uuid);
	
	File getUploadFile(Long projectId, String uuid);
	
	long getUploadFileSize(Long projectId, String uuid);
	
	long uploadBlob(Long projectId, String uuid, InputStream is);
	
	Long uploadBlob(Long projectId, byte[] blobBytes, String blobHash);

	void cancelUpload(Long projectId, String uuid);
	
	@Nullable
	Long finishUpload(Long projectId, String uuid, String hash);
	
	void downloadBlob(Long projectId, String hash, OutputStream os);
	
	void onDeleteProject(Project project);

	void syncPacks(Long projectId, String activeServer);

	File getPackBlobFile(Long projectId, String hash);
	
}
