package io.onedev.server.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import io.onedev.k8shelper.CacheAvailability;
import io.onedev.server.service.support.CacheFindResult;
import io.onedev.server.service.support.RunCacheInfo;

public interface RunCacheService {

	@Nullable
	CacheFindResult findCache(Long projectId, String key, @Nullable String checksum, String path);

	CacheAvailability downloadCache(CacheFindResult cacheFindResult, Consumer<InputStream> cacheStreamHandler);

	void downloadCache(CacheFindResult cacheFindResult, OutputStream cacheStream);

	void uploadCache(Long projectId, String key, @Nullable String checksum, String path, InputStream cacheStream);

	void uploadCache(Long projectId, String key, @Nullable String checksum, String path,
					 Consumer<OutputStream> cacheStreamHandler);

	List<RunCacheInfo> listCaches(Long projectId);

	void deleteCache(Long projectId, String key, @Nullable String checksum);

}
