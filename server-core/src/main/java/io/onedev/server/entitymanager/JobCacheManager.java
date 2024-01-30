package io.onedev.server.entitymanager;

import io.onedev.server.model.JobCache;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public interface JobCacheManager extends EntityManager<JobCache> {
	
	void downloadCache(Long projectId, String cacheKey, String cachePath, OutputStream cacheStream);

	boolean downloadCacheLocal(Long projectId, String cacheKey, String cachePath, 
							Consumer<InputStream> cacheStreamHandler);
	
	void downloadCache(Long projectId, List<String> cacheLoadKey, String cachePath, OutputStream cacheStream);

	boolean downloadCacheLocal(Long projectId, List<String> cacheLoadKey, String cachePath, 
							Consumer<InputStream> cacheStreamHandler);
	
	void downloadCacheLocal(Long projectId, Long cacheId, String cachePath, 
							OutputStream cacheStream, @Nullable AtomicBoolean cacheHit);
	
	void uploadCache(Long projectId, String cacheKey, String cachePath, InputStream cacheStream);

	void uploadCacheLocal(Long projectId, Long cacheId, String cachePath, InputStream cacheStream);

	void uploadCacheLocal(Long projectId, String cacheKey, String cachePath, 
						  Consumer<OutputStream> cacheStreamHandler);
	
	@Nullable
	Long getCacheSize(Long projectId, Long cacheId);
	
}
