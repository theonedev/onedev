package io.onedev.server.git;

import static com.google.common.hash.Hashing.sha256;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

import com.google.common.io.CountingInputStream;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterResource;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.exception.HttpResponseAwareException;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

class LfsUploadTest {

	enum Endpoint { DIRECT, CLUSTER, FORWARDED }

	private static final int MAX_SIZE = 1024 * 1024;

	@TempDir
	Path storage;

	private final byte[] content = "existing LFS content".getBytes(UTF_8);
	private final String objectId = sha256().hashBytes(content).toString();
	private MockedStatic<OneDev> oneDev;
	private MockedStatic<SecurityUtils> security;
	private ProjectService projects;
	private PerformanceSetting performance;
	private GitLfsFilter filter;
	private Path objectFile;
	private Response forwardedResponse;

	@BeforeEach
	void setUp() {
		projects = mock(ProjectService.class);
		when(projects.getLfsObjectsDir(1L)).thenReturn(storage.toFile());
		when(projects.getGitDir(1L)).thenReturn(storage.toFile());
		when(projects.getActiveServer(1L, true)).thenReturn("local");
		when(projects.runOnActiveServer(any(), any()))
				.thenAnswer(it -> it.getArgument(1, ClusterTask.class).call());
		var project = new Project();
		project.setId(1L);
		when(projects.findByPath("test")).thenReturn(project);
		oneDev = mockStatic(OneDev.class);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
		performance = new PerformanceSetting();
		performance.setMaxGitLFSFileSize(1);
		var settings = mock(SettingService.class);
		when(settings.getPerformanceSetting()).thenReturn(performance);
		oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
		security = mockStatic(SecurityUtils.class);
		security.when(() -> SecurityUtils.canAccessProject(project)).thenReturn(true);
		security.when(() -> SecurityUtils.canWriteCode(project)).thenReturn(true);
		var cluster = mock(ClusterService.class);
		when(cluster.getLocalServerAddress()).thenReturn("local");
		when(cluster.getServerUrl("remote")).thenReturn("http://active.example.invalid");
		filter = new GitLfsFilter(projects, null, mock(SessionService.class), settings, null, cluster, Set.of());
		objectFile = objectFile(objectId);
	}

	@AfterEach
	void tearDown() {
		security.close();
		oneDev.close();
	}

	@ParameterizedTest
	@EnumSource(Endpoint.class)
	void acceptsValidAndRepeatedUploads(Endpoint endpoint) throws Exception {
		upload(endpoint, new ByteArrayInputStream(content));
		upload(endpoint, new ByteArrayInputStream(content));
		assertStoredContent();
	}

	@ParameterizedTest
	@EnumSource(Endpoint.class)
	void invalidReuploadPreservesExistingObject(Endpoint endpoint) throws Exception {
		upload(endpoint, new ByteArrayInputStream(content));
		assertThrows(ExplicitException.class,
				() -> upload(endpoint, new ByteArrayInputStream(new byte[0])));
		assertStoredContent();
	}

	@ParameterizedTest
	@EnumSource(Endpoint.class)
	void interruptedReuploadPreservesExistingObject(Endpoint endpoint) throws Exception {
		upload(endpoint, new ByteArrayInputStream(content));
		assertInterruptedUpload(endpoint);
		assertStoredContent();
	}

	@ParameterizedTest
	@EnumSource(Endpoint.class)
	void failedNewUploadsLeaveNoObjectsOrTemporaryFiles(Endpoint endpoint) throws Exception {
		assertThrows(ExplicitException.class,
				() -> upload(endpoint, new ByteArrayInputStream(new byte[0])));
		assertFiles(List.of());
		assertInterruptedUpload(endpoint);
		assertFiles(List.of());
	}

	@ParameterizedTest
	@EnumSource(Endpoint.class)
	void acceptsUploadExactlyAtLimit(Endpoint endpoint) throws Exception {
		var bytes = new byte[MAX_SIZE];
		var id = sha256().hashBytes(bytes).toString();
		upload(endpoint, id, new ByteArrayInputStream(bytes));
		assertArrayEquals(bytes, Files.readAllBytes(objectFile(id)));
		assertFiles(List.of(objectFile(id)));
	}

	@ParameterizedTest
	@EnumSource(Endpoint.class)
	void stopsOversizedUploadsAfterFirstExcessByte(Endpoint endpoint) throws Exception {
		for (var size : new int[] {MAX_SIZE + 1, MAX_SIZE * 2}) {
			var bytes = new byte[size];
			var id = sha256().hashBytes(bytes).toString();
			try (var input = new CountingInputStream(new ByteArrayInputStream(bytes))) {
				assertSizeError(() -> upload(endpoint, id, input));
				assertEquals(MAX_SIZE + 1, input.getCount());
			}
			assertFiles(List.of());
		}
	}

	@ParameterizedTest
	@EnumSource(Endpoint.class)
	void oversizedReuploadPreservesExistingObject(Endpoint endpoint) throws Exception {
		upload(endpoint, new ByteArrayInputStream(content));
		assertSizeError(() -> upload(endpoint, new ByteArrayInputStream(new byte[MAX_SIZE + 1])));
		assertStoredContent();
	}

	@ParameterizedTest
	@EnumSource(Endpoint.class)
	void zeroLimitAcceptsOnlyEmptyObjects(Endpoint endpoint) throws Exception {
		performance.setMaxGitLFSFileSize(0);
		var emptyId = sha256().hashBytes(new byte[0]).toString();
		upload(endpoint, emptyId, new ByteArrayInputStream(new byte[0]));
		assertEquals(0, Files.size(objectFile(emptyId)));
		assertSizeError(() -> upload(endpoint, new ByteArrayInputStream(content)));
		assertFiles(List.of(objectFile(emptyId)));
	}

	@Test
	void preservesSizeErrorReturnedByActiveNode() {
		forwardedResponse = mock(Response.class);
		when(forwardedResponse.getStatus()).thenReturn(413);
		when(forwardedResponse.readEntity(String.class)).thenReturn("Exceeded max acceptable LFS file size 0");
		assertSizeError(() -> upload(Endpoint.FORWARDED, new ByteArrayInputStream(content)));
		verify(forwardedResponse).close();
	}

	private void assertSizeError(org.junit.jupiter.api.function.Executable upload) {
		var exception = assertThrows(Exception.class, upload);
		var cause = ExceptionUtils.find(exception, HttpResponseAwareException.class);
		assertNotNull(cause);
		assertEquals(413, cause.getHttpResponse().getStatus());
		assertTrue(cause.getMessage().startsWith("Exceeded max acceptable LFS file size "));
	}

	private void assertInterruptedUpload(Endpoint endpoint) {
		var exception = assertThrows(Exception.class, () -> upload(endpoint, interruptedInput()));
		var cause = ExceptionUtils.find(exception, IOException.class);
		assertNotNull(cause);
		assertEquals("Upload interrupted", cause.getMessage());
	}

	private InputStream interruptedInput() {
		return new FilterInputStream(new ByteArrayInputStream(content)) {
			private boolean started;

			@Override
			public int read(byte[] bytes, int offset, int length) throws IOException {
				if (started)
					throw new IOException("Upload interrupted");
				started = true;
				return super.read(bytes, offset, Math.min(length, 4));
			}
		};
	}

	private void assertStoredContent() throws IOException {
		assertArrayEquals(content, Files.readAllBytes(objectFile));
		assertFiles(List.of(objectFile));
	}

	private void assertFiles(List<Path> expected) throws IOException {
		try (var files = Files.walk(storage)) {
			assertEquals(expected, files.filter(Files::isRegularFile).toList());
		}
	}

	private void upload(Endpoint endpoint, InputStream input) throws Exception {
		upload(endpoint, objectId, input);
	}

	private Path objectFile(String id) {
		return storage.resolve(id.substring(0, 2)).resolve(id.substring(2, 4)).resolve(id);
	}

	private void upload(Endpoint endpoint, String id, InputStream input) throws Exception {
		security.when(SecurityUtils::isSystem).thenReturn(endpoint == Endpoint.CLUSTER);
		if (endpoint == Endpoint.CLUSTER) {
			try (var response = new ClusterResource().uploadLfs(input, 1L, id)) {
				assertEquals(200, response.getStatus());
			}
		} else if (endpoint == Endpoint.FORWARDED) {
			when(projects.getActiveServer(1L, true)).thenReturn("remote");
			var client = mock(Client.class);
			var target = mock(WebTarget.class, RETURNS_SELF);
			var builder = mock(Invocation.Builder.class, RETURNS_SELF);
			when(client.target(anyString())).thenReturn(target);
			when(target.request()).thenReturn(builder);
			when(builder.post(any(Entity.class))).thenAnswer(it -> {
				if (forwardedResponse != null)
					return forwardedResponse;
				var output = new ByteArrayOutputStream();
				var entity = it.getArgument(0, Entity.class);
				try {
					((StreamingOutput) entity.getEntity()).write(output);
				} catch (IOException | RuntimeException e) {
					throw new ProcessingException(e);
				}
				security.when(SecurityUtils::isSystem).thenReturn(true);
				try {
					return new ClusterResource().uploadLfs(new ByteArrayInputStream(output.toByteArray()), 1L, id);
				} finally {
					security.when(SecurityUtils::isSystem).thenReturn(false);
				}
			});
			try (var clients = mockStatic(ClientBuilder.class)) {
				clients.when(ClientBuilder::newClient).thenReturn(client);
				upload(Endpoint.DIRECT, id, input);
			} finally {
				verify(client).close();
			}
		} else {
			var request = mock(HttpServletRequest.class);
			when(request.getRequestURI()).thenReturn("/test.git/lfs/objects/" + id);
			when(request.getContextPath()).thenReturn("");
			when(request.getParameter("lfs-objects")).thenReturn("true");
			when(request.getMethod()).thenReturn("PUT");
			when(request.getContentLengthLong()).thenReturn(-1L);
			when(request.getInputStream()).thenReturn(new ServletInputStream() {
				@Override
				public int read() throws IOException {
					return input.read();
				}

				@Override
				public int read(byte[] bytes, int offset, int length) throws IOException {
					return input.read(bytes, offset, length);
				}

				@Override
				public void close() throws IOException {
					input.close();
				}

				@Override
				public boolean isFinished() {
					return false;
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setReadListener(ReadListener listener) {
					throw new UnsupportedOperationException();
				}
			});
			filter.doFilter(request, mock(HttpServletResponse.class), mock(FilterChain.class));
		}
	}
}
