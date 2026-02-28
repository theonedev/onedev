package io.onedev.server.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import io.onedev.k8shelper.CacheAvailability;
import io.onedev.server.model.RunCache;
import io.onedev.server.service.support.CacheQueryResult;

public interface RunCacheService extends EntityService<RunCache> {

	@Nullable
	CacheQueryResult queryCache(Long projectId, String key, @Nullable String checksum);
	
	@Nullable Long createCache(Long projectId, String key, @Nullable String checksum);
	
	CacheAvailability downloadCache(CacheQueryResult cacheQueryResult, String cachePathsString,
						  Consumer<InputStream> cacheStreamHandler);
	
	void downloadCache(CacheQueryResult cacheQueryResult, String cachePathsString, OutputStream cacheStream);

	void uploadCache(Long projectId, Long cacheId, String cachePathsString, InputStream cacheStream);

	void uploadCache(Long projectId, Long cacheId, String cachePathsString,
					 Consumer<OutputStream> cacheStreamHandler);
	
	@Nullable
	Long getCacheSize(Long projectId, Long cacheId);
	
}
