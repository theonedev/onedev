package io.onedev.server.service;

import io.onedev.server.model.JobCache;
import org.apache.commons.lang3.tuple.Pair;

import org.jspecify.annotations.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;

public interface JobCacheService extends EntityService<JobCache> {

	@Nullable
	Pair<Long, Long> getCacheInfoForDownload(Long projectId, String cacheKey);

	@Nullable
	Pair<Long, Long> getCacheInfoForDownload(Long projectId, List<String> loadKeys);
	
	@Nullable Long getCacheIdForUpload(Long projectId, String cacheKey);
	
	boolean downloadCache(Long projectId, Long cacheId, List<String> cachePaths,
						  Consumer<InputStream> cacheStreamHandler);
	
	void downloadCache(Long projectId, Long cacheId, List<String> cachePaths,
					   OutputStream cacheStream);

	void uploadCache(Long projectId, Long cacheId, List<String> cachePaths, InputStream cacheStream);

	void uploadCache(Long projectId, Long cacheId, List<String> cachePaths,
					 Consumer<OutputStream> cacheStreamHandler);
	
	@Nullable
	Long getCacheSize(Long projectId, Long cacheId);
	
}
