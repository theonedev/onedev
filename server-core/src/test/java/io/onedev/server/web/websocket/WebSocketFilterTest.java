package io.onedev.server.web.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServerContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.security.CorsFilter;
import io.onedev.server.service.SettingService;

class WebSocketFilterTest {

    private final WebSocketFilter filter = new WebSocketFilter();

    private final SettingService settingService = mock(SettingService.class);

    private final SystemSetting systemSetting = new SystemSetting();

    private final JettyWebSocketServerContainer container = mock(JettyWebSocketServerContainer.class);

    private final Subject subject = mock(Subject.class);

    private final SubjectThreadState threadState = new SubjectThreadState(subject);

    @BeforeEach
    void setUp() throws Exception {
        systemSetting.setServerUrl("https://onedev.example.com/");
        when(settingService.getSystemSetting()).thenReturn(systemSetting);
        when(container.upgrade(any(), any(), any())).thenReturn(true);
        FieldUtils.writeField(filter, "settingService", settingService, true);
        FieldUtils.writeField(filter, "webSocketContainer", container, true);
        threadState.bind();
    }

    @AfterEach
    void tearDown() {
        threadState.restore();
    }

    @Test
    void acceptsDirectAccessWithADifferentConfiguredServerUrl() throws Exception {
        for (String host : List.of("localhost", "127.0.0.1", "server", "[::1]"))
            assertAccepted(request("http", host, 6610, "http://" + host + ":6610"));
        assertAccepted(request("http", "::1", 6610, "http://[::1]:6610"));
    }

    @Test
    void acceptsConfiguredPublicOriginBehindAReverseProxy() throws Exception {
        assertAccepted(request("http", "backend", 6610, "https://onedev.example.com"));
        assertAccepted(request("http", "onedev_backend", 6610, "https://onedev.example.com"));
        systemSetting.setServerUrl("https://onedev.example.com:8443/");
        assertAccepted(request("http", "backend", 6610, "https://onedev.example.com:8443"));
        assertRejected(request("http", "backend", 6610, "https://onedev.example.com"));
    }

    @Test
    void comparesHostAndSchemeIgnoringCaseAndNormalizesDefaultPorts() throws Exception {
        assertAccepted(request("http", "SERVER", 80, "http://server"));
        assertAccepted(request("http", "server", 80, "http://SERVER:80"));
        assertAccepted(request("https", "server", 443, "https://server"));
        assertAccepted(request("http", "backend", 6610, "HTTPS://ONEDEV.EXAMPLE.COM:443"));
        systemSetting.setServerUrl("https://ONEDEV.EXAMPLE.COM:443/");
        assertAccepted(request("http", "backend", 6610, "https://onedev.example.com"));
    }

    @Test
    void allowsDirectAccessDuringInitialSetup() throws Exception {
        when(settingService.getSystemSetting()).thenReturn(null);
        assertAccepted(request("http", "server", 6610, "http://server:6610"));
        assertRejected(request("http", "server", 6610, "http://other.example"));
        when(settingService.getSystemSetting()).thenReturn(new SystemSetting());
        assertAccepted(request("http", "server", 6610, "http://server:6610"));
        assertRejected(request("http", "server", 6610, "http://other.example"));
    }

    @Test
    void rejectsMissingNullMalformedAndMultipleOrigins() throws Exception {
        assertRejected(request("http", "server", 6610));
        for (String origin : List.of("null", "", " ", "server:6610", "//server:6610",
                "ws://server:6610", "file:///", "http://server:6610/", "http://server:6610/path",
                "http://user@server:6610", "http://server:6610?query", "http://server:6610#fragment",
                "http://server:99999", "http://server:invalid", "http://[invalid",
                "http://server:6610 https://onedev.example.com",
                "http://server:6610,https://onedev.example.com")) {
            assertRejected(request("http", "server", 6610, origin));
        }
        assertRejected(request("http", "server", 6610, "http://server:6610", "http://other.example"));
        assertRejected(request("http", "server", 6610, "http://server:6610", "http://server:6610"));
    }

    @Test
    void rejectsDifferentHostsSchemesAndPorts() throws Exception {
        for (String origin : List.of("http://other.example", "https://server:6610", "http://server:8000",
                "http://server", "http://127.0.0.1:6610", "http://onedev.example.com",
                "https://onedev.example.com:8443", "https://onedev.example.com.other.example")) {
            assertRejected(request("http", "server", 6610, origin));
        }
    }

    @Test
    void doesNotTrustArbitraryForwardingHeaders() throws Exception {
        var request = request("http", "server", 6610, "https://other.example");
        when(request.getHeader("Forwarded")).thenReturn("host=other.example;proto=https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("other.example");
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        assertRejected(request);
    }

    @Test
    void corsPermissionDoesNotAuthorizeWicketWebSockets() throws Exception {
        var securitySetting = new SecuritySetting();
        securitySetting.setCorsAllowedOrigins(List.of("https://other.example"));
        when(settingService.getSecuritySetting()).thenReturn(securitySetting);
        var request = request("http", "server", 6610, "https://other.example");
        var response = mock(HttpServletResponse.class);
        new CorsFilter(settingService).doFilter(request, response, (req, resp) -> {
            assertFalse(filter.acceptWebSocket((HttpServletRequest) req, (HttpServletResponse) resp));
        });
        verify(response).addHeader("Access-Control-Allow-Origin", "https://other.example");
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(container);
    }

    @Test
    void leavesOrdinaryHttpRequestsAlone() throws Exception {
        var request = request("http", "server", 6610);
        when(request.getHeaders("Upgrade")).thenReturn(Collections.emptyEnumeration());
        var response = mock(HttpServletResponse.class);
        assertFalse(filter.acceptWebSocket(request, response));
        verifyNoInteractions(response, container, settingService);
    }

    private void assertAccepted(HttpServletRequest request) throws Exception {
        clearInvocations(container);
        var response = mock(HttpServletResponse.class);
        assertTrue(filter.acceptWebSocket(request, response), request.getHeader("Origin"));
        verify(container).upgrade(any(), same(request), same(response));
        verify(request).setAttribute(WebSocketFilter.SHIRO_SUBJECT, subject);
        verify(response, never()).sendError(anyInt());
    }

    private void assertRejected(HttpServletRequest request) throws Exception {
        clearInvocations(container);
        var response = mock(HttpServletResponse.class);
        assertFalse(filter.acceptWebSocket(request, response), request.getHeader("Origin"));
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(container);
        verify(request, never()).setAttribute(eq(WebSocketFilter.SHIRO_SUBJECT), any());
    }

    private HttpServletRequest request(String scheme, String host, int port, String... origins) {
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getScheme()).thenReturn(scheme);
        when(request.getServerName()).thenReturn(host);
        when(request.getServerPort()).thenReturn(port);
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put("Upgrade", List.of("websocket"));
        headers.put("Connection", List.of("Upgrade"));
        headers.put("Sec-WebSocket-Version", List.of("13"));
        headers.put("Sec-WebSocket-Key", List.of("dGhlIHNhbXBsZSBub25jZQ=="));
        headers.put("Origin", Arrays.asList(origins));
        when(request.getHeaders(anyString())).thenAnswer(invocation ->
                Collections.enumeration(headers.getOrDefault(invocation.getArgument(0), List.of())));
        when(request.getHeader(anyString())).thenAnswer(invocation -> {
            var values = headers.getOrDefault(invocation.getArgument(0), List.of());
            return values.isEmpty() ? null : values.get(0);
        });
        return request;
    }
}
