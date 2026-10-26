package io.onedev.server.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import jakarta.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.k8shelper.CacheAvailability;
import io.onedev.server.cluster.ClusterResource;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.support.CacheFindResult;
import io.onedev.server.service.support.PathInfo;
import io.onedev.server.util.PathIndexUtils;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;

class DefaultRunCacheServiceTest {

	@TempDir
	Path temp;

	private DefaultRunCacheService service;
	private ProjectService projects;
	private Project project;
	private Path cacheRoot;

	@BeforeEach
	void setUp() throws Exception {
		cacheRoot = Files.createDirectory(temp.resolve("cache"));
		project = new Project();
		project.setId(1L);
		projects = mock(ProjectService.class);
		when(projects.load(1L)).thenReturn(project);
		when(projects.getCacheDir(1L)).thenReturn(cacheRoot.toFile());
		doAnswer(invocation -> invocation.<ClusterTask<?>>getArgument(1).call())
				.when(projects).runOnActiveServer(anyLong(), any());
		service = new DefaultRunCacheService();
		inject(service, "projectService", projects);
	}

	@Test
	void storesArchivesByIndexAndListsOriginalPaths() throws Exception {
		var key = "cache/key: with spaces";
		var checksum = "check:sum";
		var paths = List.of("/缓存\n路径", "../../outside", "long/".repeat(1000));
		for (var path : paths) {
			upload(1L, key, checksum, path, "contents: " + path);
			var result = service.findCache(1L, key, checksum, path);
			assertNotNull(result);
			assertEquals(CacheAvailability.EXACT_MATCH, result.getCacheAvailability());
			assertTrue(result.getPathIndex() > 0);
			assertEquals(URLEncoder.encode(key, UTF_8) + ":" + URLEncoder.encode(checksum, UTF_8), result.getDirName());
			var cacheDir = cacheRoot.resolve(result.getDirName());
			assertEquals(result.getPathIndex(), PathIndexUtils.read(cacheDir.toFile()).get(path));
			assertEquals("contents: " + path, Files.readString(cacheDir.resolve(String.valueOf(result.getPathIndex()))));
			assertEquals("contents: " + path, download(result));
		}
		var caches = service.listCaches(1L);
		assertEquals(1, caches.size());
		var cache = caches.get(0);
		assertEquals(key, cache.getKey());
		assertEquals(checksum, cache.getChecksum());
		assertNotNull(cache.getLastAccessDate());
		assertEquals(paths.stream().sorted().toList(), cache.getIndexedPaths().stream().map(PathInfo::getPath).toList());
		for (var info : cache.getIndexedPaths())
			assertEquals(("contents: " + info.getPath()).getBytes(UTF_8).length, info.getSize());
	}

	@Test
	void reorderedUploadsAndRestartKeepEachPathsIdentity() throws Exception {
		upload(1L, "key", null, "/first", "first");
		upload(1L, "key", null, "/second", "second");
		var original = service.findCache(1L, "key", null, "/first");
		service = new DefaultRunCacheService();
		inject(service, "projectService", projects);
		upload(1L, "key", null, "/second", "second updated");
		upload(1L, "key", null, "/first", "first updated");
		assertEquals(original.getPathIndex(), service.findCache(1L, "key", null, "/first").getPathIndex());
		assertEquals("first updated", download(original));
		assertEquals("second updated", download(service.findCache(1L, "key", null, "/second")));
		assertEquals(2, service.listCaches(1L).get(0).getIndexedPaths().size());
	}

	@Test
	void exactAndPartialMatchesStillHonorLastAccessAndProjectHierarchy() throws Exception {
		upload(1L, "key", "old", "/path", "old");
		upload(1L, "key", "new", "/path", "new");
		upload(1L, "key", "unrelated", "/another", "unrelated");
		Files.setLastModifiedTime(cacheRoot.resolve("key:old/access"), FileTime.fromMillis(1000));
		Files.setLastModifiedTime(cacheRoot.resolve("key:new/access"), FileTime.fromMillis(2000));
		var exact = service.findCache(1L, "key", "old", "/path");
		assertTrue(exact.isExactMatch());
		assertEquals("key:old", exact.getDirName());
		var partial = service.findCache(1L, "key", "missing", "/path");
		assertFalse(partial.isExactMatch());
		assertEquals("new", download(partial));
		assertNull(service.findCache(1L, "different-key", "new", "/path"));
		assertNull(service.findCache(1L, "key", "new", "/absent"));

		var parent = new Project();
		parent.setId(2L);
		project.setParent(parent);
		when(projects.getCacheDir(2L)).thenReturn(Files.createDirectory(temp.resolve("parent-cache")).toFile());
		upload(2L, "parent", null, "/path", "parent data");
		var inherited = service.findCache(1L, "parent", null, "/path");
		assertEquals(2L, inherited.getProjectId());
		assertEquals("parent data", download(inherited));
	}

	@Test
	void failedUploadsKeepPreviousArchiveAndDoNotPublishNewEntries() throws Exception {
		upload(1L, "key", null, "/existing", "usable");
		var existing = service.findCache(1L, "key", null, "/existing");
		Consumer<OutputStream> interrupted = output -> {
			try {
				output.write("partial".getBytes(UTF_8));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			throw new IllegalStateException("interrupted");
		};
		assertThrows(IllegalStateException.class, () -> service.uploadCache(1L, "key", null, "/existing", interrupted));
		assertThrows(IllegalStateException.class, () -> service.uploadCache(1L, "key", null, "/new", interrupted));
		assertEquals("usable", download(existing));
		assertNull(service.findCache(1L, "key", null, "/new"));
		assertEquals(List.of("/existing"), service.listCaches(1L).get(0).getIndexedPaths().stream().map(PathInfo::getPath).toList());
		assertEquals(Set.of("/existing"), PathIndexUtils.read(cacheRoot.resolve("key").toFile()).keySet());
		try (var files = Files.walk(cacheRoot)) {
			assertFalse(files.anyMatch(it -> it.toString().endsWith(".tmp")));
		}
	}

	@Test
	void clusterDownloadsCarryIndexAndPreserveAvailability() throws Exception {
		upload(1L, "key", "checksum", "/path", "archive");
		var resource = new ClusterResource();
		inject(resource, "cacheService", service);
		try (var security = mockStatic(SecurityUtils.class)) {
			security.when(SecurityUtils::isSystem).thenReturn(true);
			for (var checksum : List.of("checksum", "different")) {
				var result = SerializationUtils.clone(service.findCache(1L, "key", checksum, "/path"));
				try (var response = resource.downloadCache(result.getProjectId(), result.getDirName(), result.getPathIndex(), result.isExactMatch())) {
					var output = new ByteArrayOutputStream();
					((StreamingOutput) response.getEntity()).write(output);
					var input = new ByteArrayInputStream(output.toByteArray());
					assertEquals(result.getCacheAvailability().ordinal(), input.read());
					assertEquals("archive", new String(input.readAllBytes(), UTF_8));
				}
			}
		}
	}

	@Test
	void deletionBetweenLookupAndDownloadReturnsNotFound() {
		upload(1L, "key", null, "/path", "archive");
		var result = service.findCache(1L, "key", null, "/path");
		service.deleteCache(1L, "key", null);
		assertEquals(CacheAvailability.NOT_FOUND, service.downloadCache(result, input -> fail("Deleted cache was streamed")));
		var output = new ByteArrayOutputStream();
		service.downloadCache(result, output);
		assertArrayEquals(new byte[] {(byte) CacheAvailability.NOT_FOUND.ordinal()}, output.toByteArray());
		assertTrue(service.listCaches(1L).isEmpty());
	}

	@Test
	void cleanupRemovesExpiredAndIncompleteCaches() throws Exception {
		upload(1L, "expired", null, "/path", "old");
		upload(1L, "current", null, "/path", "current");
		upload(1L, "incomplete", null, "/path", "incomplete");
		Files.delete(PathIndexUtils.getIndexFile(cacheRoot.resolve("incomplete").toFile()).toPath());
		Files.setLastModifiedTime(cacheRoot.resolve("expired/access"), FileTime.fromMillis(1));
		assertNull(service.findCache(1L, "incomplete", null, "/path"));
		assertEquals(2, service.listCaches(1L).size());
		when(projects.getActiveIds()).thenReturn(Set.of(1L));
		project.getBuildSetting().setCachePreserveDays(1);
		var sessions = mock(SessionService.class);
		doAnswer(invocation -> invocation.<Callable<?>>getArgument(0).call()).when(sessions).call(any());
		inject(service, "sessionService", sessions);
		var batch = mock(BatchWorkExecutionService.class);
		doAnswer(invocation -> {
			invocation.<BatchWorker>getArgument(0).doWorks(List.of(invocation.<Prioritized>getArgument(1)));
			return null;
		}).when(batch).submit(any(), any());
		inject(service, "batchWorkExecutionService", batch);
		service.execute();
		assertFalse(Files.exists(cacheRoot.resolve("expired")));
		assertFalse(Files.exists(cacheRoot.resolve("incomplete")));
		assertEquals("current", download(service.findCache(1L, "current", null, "/path")));
		assertEquals(List.of("current"), service.listCaches(1L).stream().map(it -> it.getKey()).toList());
	}

	private void upload(Long projectId, String key, String checksum, String path, String contents) {
		service.uploadCache(projectId, key, checksum, path, new ByteArrayInputStream(contents.getBytes(UTF_8)));
	}

	private String download(CacheFindResult result) {
		var output = new ByteArrayOutputStream();
		assertEquals(result.getCacheAvailability(), service.downloadCache(result, input -> {
			try {
				input.transferTo(output);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}));
		return output.toString(UTF_8);
	}

	private void inject(Object target, String name, Object value) throws Exception {
		var field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

}
