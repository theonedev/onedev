package io.onedev.server.notification;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.WebHookHeader;

class WebHookManagerTest {

	private static final String SECRET = "webhook-test-secret-密钥";

	private static final byte[] BODY = "{\"message\":\"Hello, 世界 👋\",\"number\":1}\n".getBytes(UTF_8);

	// Calculated independently with Python's hmac/hashlib over the UTF-8 key and body.
	private static final String SIGNATURE = "80b436ae9d4ca99ff2e78cdbce081a63408a2d11be28f425fe898bdb23c8c21a";

	private final WebHookManager manager = new WebHookManager();

	private final ObjectMapper mapper = mock(ObjectMapper.class);

	private final RefUpdated event = mock(RefUpdated.class);

	private final Project project = mock(Project.class);

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final LinkedBlockingQueue<Delivery> deliveries = new LinkedBlockingQueue<>();

	private HttpServer server;

	@BeforeEach
	void setUp() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", exchange -> {
			try {
				Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
				exchange.getRequestHeaders().forEach((name, values) -> headers.put(name, List.copyOf(values)));
				deliveries.add(new Delivery(exchange.getRequestMethod(), headers, exchange.getRequestBody().readAllBytes()));
				exchange.sendResponseHeaders(200, 2);
				exchange.getResponseBody().write("{}".getBytes(UTF_8));
			} finally {
				exchange.close();
			}
		});
		server.start();
		FieldUtils.writeField(manager, "mapper", mapper, true);
		FieldUtils.writeField(manager, "executorService", executor, true);
		when(event.getProject()).thenReturn(project);
		when(mapper.writeValueAsBytes(event)).thenReturn(BODY);
	}

	@AfterEach
	void tearDown() throws Exception {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(5, TimeUnit.SECONDS))
				executor.shutdownNow();
		} finally {
			if (server != null)
				server.stop(0);
		}
	}

	@Test
	void signsTheExactDeliveredUtf8BodyWithoutSendingTheSecret() throws Exception {
		when(project.getHierarchyWebHooks()).thenReturn(List.of(hook(SECRET)));
		deliver();
		var delivery = takeDelivery();
		assertEquals("POST", delivery.method());
		assertArrayEquals(BODY, delivery.body());
		assertEquals(List.of(SIGNATURE), delivery.headers().get("X-OneDev-Signature"));
		assertEquals(List.of("application/json"), delivery.headers().get("Content-Type"));
		assertEquals(List.of("UTF-8"), delivery.headers().get("Accept-Charset"));
		assertFalse(delivery.headers().values().stream().flatMap(List::stream).anyMatch(value -> value.contains(SECRET)));
		verify(mapper).writeValueAsBytes(event);
		assertTrue(deliveries.isEmpty());
	}

	@Test
	void bindsEachSignatureToBothTheBodyAndTheWebhookSecret() throws Exception {
		when(project.getHierarchyWebHooks()).thenReturn(List.of(hook(SECRET), hook("second-webhook-secret")));
		deliver();
		assertEquals(List.of(SIGNATURE), takeDelivery().headers().get("X-OneDev-Signature"));
		assertEquals(List.of("b6d75e6b3fda3de6b65bcd7dbc2b8569eb48aade777302fc3b047f5115dd8923"),
				takeDelivery().headers().get("X-OneDev-Signature"));

		var modifiedBody = "{\"message\":\"Hello, 世界 👋\",\"number\":2}\n".getBytes(UTF_8);
		when(mapper.writeValueAsBytes(event)).thenReturn(modifiedBody);
		deliver();
		var first = takeDelivery();
		var second = takeDelivery();
		assertArrayEquals(modifiedBody, first.body());
		assertArrayEquals(modifiedBody, second.body());
		assertEquals(List.of("38ea7a404e654f6bc5bef3b73487e6e958c283b9e24fa7fa7347c0956bcceb8d"),
				first.headers().get("X-OneDev-Signature"));
		assertEquals(List.of("7a170f752eb338c2a4e1ea029d9fd572526372e60520e37fc82a75b02935df2e"),
				second.headers().get("X-OneDev-Signature"));
	}

	@Test
	void customHeadersCannotOverrideTheSignature() throws Exception {
		var hook = hook(SECRET);
		hook.setHeaders(List.of(header("x-onedev-signature", "legacy-secret"),
				header("X-ONEDEV-SIGNATURE", "another-value"), header("Authorization", "Bearer receiver-token")));
		when(project.getHierarchyWebHooks()).thenReturn(List.of(hook));
		deliver();
		var delivery = takeDelivery();
		assertEquals(List.of(SIGNATURE), delivery.headers().get("X-OneDev-Signature"));
		assertEquals(List.of("Bearer receiver-token"), delivery.headers().get("Authorization"));
	}

	private WebHook hook(String secret) {
		var hook = new WebHook();
		hook.setPostUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/webhook");
		hook.setEventTypes(List.of(WebHook.EventType.CODE_PUSH));
		hook.setSecret(secret);
		return hook;
	}

	private WebHookHeader header(String name, String value) {
		var header = new WebHookHeader();
		header.setName(name);
		header.setValue(value);
		return header;
	}

	private void deliver() throws Exception {
		manager.on(event);
		// Wait for the queued HTTP deliveries to finish before inspecting the receiver.
		executor.submit(() -> {}).get(5, TimeUnit.SECONDS);
	}

	private Delivery takeDelivery() throws Exception {
		var delivery = deliveries.poll(5, TimeUnit.SECONDS);
		assertNotNull(delivery, "Expected a webhook POST at the local receiver");
		return delivery;
	}

	private record Delivery(String method, Map<String, List<String>> headers, byte[] body) {
	}
}
