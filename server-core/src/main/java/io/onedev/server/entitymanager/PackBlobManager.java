package io.onedev.server.entitymanager;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.persistence.dao.EntityManager;

public interface PackBlobManager extends EntityManager<PackBlob> {
	
	@Nullable
    PackBlob findBySha256Hash(Long projectId, String sha256Hash);
	
	String getSha512Hash(PackBlob packBlob);
	
	String getSha1Hash(PackBlob packBlob);
	
	String getMd5Hash(PackBlob packBlob);
	
	boolean checkPackBlobFile(Long projectId, String sha256Hash, long size);
	
	@Nullable
	PackBlob checkPackBlob(Long projectId, String sha256Hash);
	
	void initUpload(Long projectId, String uuid);
	
	File getUploadFile(Long projectId, String uuid);
	
	long getUploadFileSize(Long projectId, String uuid);
	
	long uploadBlob(Long projectId, String uuid, InputStream is);

	@Nullable
	Long uploadBlob(Long projectId, byte[] blobBytes, @Nullable String sha256Hash);
	
	@Nullable
	Long uploadBlob(Long projectId, InputStream is, @Nullable String sha256Hash);

	void cancelUpload(Long projectId, String uuid);
	
	@Nullable
	Long finishUpload(Long projectId, String uuid, @Nullable String sha256Hash);
	
	void downloadBlob(Long projectId, String sha256Hash, OutputStream os);
	
	byte[] readBlob(Long projectId, String sha256Hash);
	
	void syncPacks(Long projectId, String activeServer);

	File getPackBlobFile(Long projectId, String sha256Hash);

    void populateBlobs(Collection<Pack> packs);
	
}
