package io.onedev.server.oauth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;

import io.onedev.server.model.OAuthApplication;

class OAuthProtocolTest {

	private static final String CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
	private static final String REQUEST = "client_id=client&response_type=code&redirect_uri=https%3A%2F%2Fclient.example%2Fcallback"
			+ "&scope=profile&state=return-state&code_challenge_method=S256&code_challenge=" + CHALLENGE;

	@Test
	void validatesRfc7636Example() throws Exception {
		OAuthProtocol.validateAuthorization(AuthorizationRequest.parse(REQUEST));
		assertTrue(OAuthProtocol.verifyPkce(CHALLENGE, new CodeVerifier("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")));
		assertFalse(OAuthProtocol.verifyPkce(CHALLENGE, new CodeVerifier()));
		assertFalse(OAuthProtocol.verifyPkce(CHALLENGE, null));
	}

	@Test
	void refusesImplicitAndPlainPkce() throws Exception {
		assertThrows(ParseException.class, () -> OAuthProtocol.validateAuthorization(AuthorizationRequest.parse(REQUEST.replace("response_type=code", "response_type=token"))));
		assertThrows(ParseException.class, () -> OAuthProtocol.validateAuthorization(AuthorizationRequest.parse(REQUEST.replace("S256", "plain"))));
		assertThrows(ParseException.class, () -> OAuthProtocol.validateAuthorization(AuthorizationRequest.parse("client_id=client&response_type=code")));
	}

	@Test
	void rejectsUnsupportedExtensionsInsteadOfSilentlyIgnoringThem() {
		for (String parameter: List.of("request_uri=https://client.example/request", "max_age=0", "claims=%7B%7D", "resource=https://api.example"))
			assertThrows(ParseException.class, () -> OAuthProtocol.validateAuthorization(AuthorizationRequest.parse(REQUEST + "&" + parameter)));
	}

	@Test
	void requiresExactRedirectMatch() throws Exception {
		var application = new OAuthApplication();
		application.setRedirectUris("https://client.example/callback");
		assertTrue(OAuthProtocol.hasRegisteredRedirect(application, AuthorizationRequest.parse(REQUEST)));
		for (String value: List.of("https://client.example/callback/", "https://client.example/callback?x=1", "https://client.example.evil/callback"))
			assertFalse(OAuthProtocol.hasRegisteredRedirect(application, AuthorizationRequest.parse(REQUEST.replace("https%3A%2F%2Fclient.example%2Fcallback", value))));
	}

	@Test
	void publicLoopbackClientsCanChooseAnAvailablePort() throws Exception {
		var application = new OAuthApplication();
		for (String host: List.of("127.0.0.1", "[::1]")) {
			for (String path: List.of("", "/callback?client=git")) {
				application.setRedirectUris("http://" + host + path);
				var request = AuthorizationRequest.parse(REQUEST.replace("https%3A%2F%2Fclient.example%2Fcallback",
						java.net.URLEncoder.encode("http://" + host + ":49152" + path, java.nio.charset.StandardCharsets.UTF_8)));
				assertTrue(OAuthProtocol.hasRegisteredRedirect(application, request));
				application.setRedirectUris("http://" + host + ":12345" + path);
				assertTrue(OAuthProtocol.hasRegisteredRedirect(application, request));
				application.setConfidential(true);
				assertFalse(OAuthProtocol.hasRegisteredRedirect(application, request));
				application.setConfidential(false);
			}
		}
	}

	@Test
	void loopbackPortExceptionDoesNotRelaxOtherRedirectComponents() throws Exception {
		var application = new OAuthApplication();
		application.setRedirectUris("http://127.0.0.1/callback?client=git");
		for (String uri: List.of("http://127.0.0.1:49152/other?client=git", "http://127.0.0.1:49152/%63allback?client=git",
				"http://127.0.0.1:49152/callback?client=other", "http://localhost:49152/callback?client=git",
				"http://[::1]:49152/callback?client=git", "http://127.0.0.1.example:49152/callback?client=git",
				"http://user@127.0.0.1:49152/callback?client=git", "https://127.0.0.1:49152/callback?client=git",
				"http://127.0.0.1:65536/callback?client=git", "http://127.0.0.1:0/callback?client=git")) {
			var request = AuthorizationRequest.parse(REQUEST.replace("https%3A%2F%2Fclient.example%2Fcallback",
					java.net.URLEncoder.encode(uri, java.nio.charset.StandardCharsets.UTF_8)));
			assertFalse(OAuthProtocol.hasRegisteredRedirect(application, request), uri);
		}
		application.setRedirectUris("http://localhost/callback");
		assertFalse(OAuthProtocol.hasRegisteredRedirect(application,
				AuthorizationRequest.parse(REQUEST.replace("https%3A%2F%2Fclient.example%2Fcallback", "http://localhost:49152/callback"))));
	}

	@Test
	void validatesRegistrationUris() {
		for (String uri: List.of("https://client.example/callback", "http://127.0.0.1:3456/callback", "http://[::1]:3456/callback"))
			assertTrue(OAuthProtocol.isValidRedirectUri(uri, true), uri);
		assertTrue(OAuthProtocol.isValidRedirectUri("com.example.app:/callback", false));
		assertFalse(OAuthProtocol.isValidRedirectUri("com.example.app:/callback", true));
		for (String uri: List.of("http://remote.example/callback", "https://client.example/#fragment", "//client.example/callback",
				"https://user:password@client.example/callback", "https://*.example/callback", "javascript:alert(1)", "http:/callback"))
			assertFalse(OAuthProtocol.isValidRedirectUri(uri, false), uri);
	}

	@Test
	void rejectsDuplicateParameters() {
		assertThrows(ParseException.class, () -> OAuthProtocol.checkSingleParameters(Map.of("client_id", List.of("first", "second"))));
	}

	@Test
	void comparesCredentialHashesAndRejectsMalformedGrantIdentifiers() {
		assertTrue(OAuthProtocol.matches("secret", OAuthProtocol.hash("secret")));
		assertFalse(OAuthProtocol.matches("other", OAuthProtocol.hash("secret")));
		assertFalse(OAuthProtocol.matches("secret", null));
		assertNull(OAuthProtocol.grantId("abc"));
		assertNull(OAuthProtocol.grantId("9223372036854775808.value"));
	}
}
