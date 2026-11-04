package io.onedev.server.oauth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;

import io.onedev.server.persistence.SessionService;

class OAuthEndpointServletTest {
	private OAuthEndpointServlet servlet;
	private OAuthProviderService providerService;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private StringWriter output;

	@BeforeEach
	void setUp() throws Exception {
		servlet = new OAuthEndpointServlet();
		providerService = mock(OAuthProviderService.class);
		var sessions = mock(SessionService.class);
		when(sessions.call(any())).thenAnswer(it -> ((Callable<?>) it.getArgument(0)).call());
		for (var name: Map.of("providerService", providerService, "sessionService", sessions).entrySet()) {
			var field = OAuthEndpointServlet.class.getDeclaredField(name.getKey());
			field.setAccessible(true); field.set(servlet, name.getValue());
		}
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		when(request.getServletPath()).thenReturn("/~oauth/token");
		when(request.getMethod()).thenReturn("POST");
		when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
		when(request.getRequestURL()).thenReturn(new StringBuffer("https://onedev.example/~oauth/token"));
		when(request.getHeaders("Authorization")).thenReturn(Collections.emptyEnumeration());
		output = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(output));
		when(providerService.exchange(any())).thenReturn(new TokenErrorResponse(OAuth2Error.INVALID_GRANT));
	}

	@Test
	void usesContainerParsedFormEvenWhenAFilterHasConsumedTheBody() throws Exception {
		when(request.getParameterMap()).thenReturn(Map.of("grant_type", new String[]{"authorization_code"},
				"client_id", new String[]{"client"}, "code", new String[]{"1.code"},
				"redirect_uri", new String[]{"https://client.example/callback"}, "code_verifier", new String[]{new CodeVerifier().getValue()}));
		servlet.service(request, response);
		var parsed = ArgumentCaptor.forClass(TokenRequest.class);
		verify(providerService).exchange(parsed.capture());
		assertEquals("client", parsed.getValue().getClientID().getValue());
		verify(request, never()).getInputStream();
		verify(response).setHeader("Cache-Control", "no-store");
	}

	@Test
	void rejectsRepeatedFormParameters() throws Exception {
		when(request.getParameterMap()).thenReturn(Map.of("client_id", new String[]{"a", "b"}));
		servlet.service(request, response);
		verify(providerService, never()).exchange(any());
		verify(providerService, never()).revoke(any());
		verify(response).setStatus(400);
		assertTrue(output.toString().contains("invalid_request"));
	}

	@Test
	void rejectsCredentialParametersInQueryString() throws Exception {
		when(request.getQueryString()).thenReturn("client_secret=secret");
		servlet.service(request, response);
		verify(providerService, never()).exchange(any());
		verify(providerService, never()).revoke(any());
		verify(response).setStatus(400);
	}

	@Test
	void userInfoSupportsBothGetAndPostWithBearerAuthentication() throws Exception {
		when(request.getServletPath()).thenReturn("/~oauth/userinfo");
		when(providerService.getUserInfo("access-token")).thenReturn(Map.of("sub", "10"));
		for (var method: new String[]{"GET", "POST"}) {
			when(request.getMethod()).thenReturn(method);
			when(request.getHeaders("Authorization")).thenReturn(Collections.enumeration(java.util.List.of("Bearer access-token")));
			servlet.service(request, response);
		}
		verify(providerService, times(2)).getUserInfo("access-token");
		verify(response, times(2)).setStatus(200);
	}

}
