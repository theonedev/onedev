package io.onedev.server.oauth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.LockModeType;

import org.hibernate.Session;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.jose.jwk.RSAKey;

import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.model.OAuthApplication;
import io.onedev.server.model.OAuthGrant;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.Setting;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.SettingService;

class OAuthProviderServiceTest {

	private DefaultOAuthProviderService provider;
	private Subject subject;
	private OAuthApplication application;
	private OAuthGrant grant;
	private AccessToken token;
	private Dao dao;
	private AccessTokenService accessTokens;
	private SettingService settings;
	private AtomicReference<String> signingKey;
	private final CodeVerifier verifier = new CodeVerifier();
	private static final URI ENDPOINT = URI.create("https://onedev.example/~oauth/token");
	private static final URI REDIRECT = URI.create("https://client.example/callback");

	@BeforeEach
	void setUp() throws Exception {
		provider = new DefaultOAuthProviderService();
		subject = mock(Subject.class);
		dao = mock(Dao.class);
		var session = mock(Session.class);
		when(dao.getSession()).thenReturn(session);
		inject("dao", dao);
		accessTokens = mock(AccessTokenService.class);
		inject("accessTokenService", accessTokens);
		inject("auditService", mock(AuditService.class));
		settings = mock(SettingService.class);
		signingKey = new AtomicReference<>("configured");
		when(settings.getOAuthSigningKey()).thenAnswer(it -> signingKey.get());
		doAnswer(it -> { signingKey.set(it.getArgument(0)); return null; })
				.when(settings).saveOAuthSigningKey(any());
		var signingKeySetting = new Setting();
		signingKeySetting.setKey(Setting.Key.OAUTH_SIGNING_KEY);
		when(settings.findSetting(Setting.Key.OAUTH_SIGNING_KEY)).thenReturn(signingKeySetting);
		inject("settingService", settings);
		application = new OAuthApplication();
		application.setId(1L);
		application.setName("Test application");
		var applicationOwner = new User();
		applicationOwner.setId(11L);
		applicationOwner.setName("application-owner");
		application.setOwner(applicationOwner);
		application.setClientId("client");
		application.setConfidential(true);
		application.setSecretHash(OAuthProtocol.hash("client-secret"));
		when(dao.find(any())).thenAnswer(it -> application);
		var owner = new User();
		owner.setId(10L);
		owner.setName("owner");
		grant = new OAuthGrant();
		grant.setId(20L);
		grant.setOwner(owner);
		grant.setApplication(application);
		grant.setScopes("profile role:code_reader");
		grant.setRedirectUri(REDIRECT.toString());
		grant.setCodeChallenge(CodeChallenge.compute(CodeChallengeMethod.S256, verifier).getValue());
		grant.setCodeHash(OAuthProtocol.hash("20.code-secret"));
		grant.setCodeExpireDate(new Date(System.currentTimeMillis() + 120_000));
		grant.setExpireDate(new Date(System.currentTimeMillis() + 86400_000));
		token = new AccessToken();
		token.setId(30L);
		token.setOauthGrant(grant);
		token.setOwner(owner);
		token.setExpireDate(new Date(0));
		var role = new Role();
		role.setId(1L);
		role.setName("Code Reader");
		var roles = mock(RoleService.class);
		when(roles.query()).thenReturn(List.of(role));
		inject("roleService", roles);
		var authorization = new AccessTokenAuthorization();
		authorization.setToken(token);
		authorization.setRole(role);
		token.getAuthorizations().add(authorization);
		grant.getAccessTokens().add(token);
		when(session.find(OAuthGrant.class, 20L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(grant);
	}

	private void inject(String name, Object value) throws Exception {
		var field = DefaultOAuthProviderService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(provider, value);
	}

	private ClientSecretBasic auth() { return new ClientSecretBasic(new ClientID("client"), new Secret("client-secret")); }

	private TokenRequest code(CodeVerifier value, URI redirect) {
		return new TokenRequest(ENDPOINT, auth(), new AuthorizationCodeGrant(new AuthorizationCode("20.code-secret"), redirect, value));
	}

	private TokenRequest refresh(RefreshToken refresh, Scope scope) {
		return new TokenRequest(ENDPOINT, auth(), new RefreshTokenGrant(refresh), scope);
	}

	private void assertError(String expected, TokenResponse response) {
		assertFalse(response.indicatesSuccess());
		assertEquals(expected, response.toErrorResponse().getErrorObject().getCode());
	}

	@Test
	void regeneratingSigningKeyRevokesGrantsAndPreservesApplications() throws Exception {
		provider.generateSigningKey();
		var first = RSAKey.parse(signingKey.get());
		assertTrue(first.isPrivate());
		assertFalse(provider.getPublicSigningKey().isPrivate());
		assertEquals(first.toPublicJWK().toJSONObject(),
				((java.util.List<?>) provider.getJwks().get("keys")).get(0));
		when(dao.query(org.mockito.ArgumentMatchers.<EntityCriteria<OAuthGrant>>any())).thenReturn(List.of(grant));
		provider.generateSigningKey();
		assertNotEquals(first.getKeyID(), provider.getPublicSigningKey().getKeyID());
		verify(accessTokens).delete(token);
		verify(dao).remove(grant);
		verify(dao, never()).remove(application);
	}

	@Test
	void consentRequiresTheAccountToManageTheSelectedProject() throws Exception {
		var owner = grant.getOwner();
		var project = new Project();
		project.setId(100L);
		var projects = mock(ProjectService.class);
		when(projects.load(project.getId())).thenReturn(project);
		inject("projectService", projects);
		application.setRedirectUris(REDIRECT.toString());
		var request = AuthorizationRequest.parse("client_id=client&response_type=code"
				+ "&redirect_uri=https%3A%2F%2Fclient.example%2Fcallback&scope=profile+role%3Acode_reader"
				+ "&code_challenge_method=S256&code_challenge=" + grant.getCodeChallenge());
		when(subject.isPermitted(any(Permission.class))).thenAnswer(it ->
				new ProjectPermission(project, new AccessProject()).implies(it.getArgument(0)));
		try (var security = mockStatic(SecurityUtils.class)) {
			security.when(() -> SecurityUtils.getAuthUser(subject)).thenReturn(owner);
			security.when(() -> SecurityUtils.canAccessProject(subject, project)).thenCallRealMethod();
			security.when(() -> SecurityUtils.canManageProject(subject, project)).thenCallRealMethod();
			assertTrue(SecurityUtils.canAccessProject(subject, project));
			assertThrows(UnauthorizedException.class, () -> provider.authorize(subject, request, List.of(project.getId())));
			verify(dao, never()).persist(any());
			verify(accessTokens, never()).createOrUpdate(any());

			when(subject.isPermitted(any(Permission.class))).thenAnswer(it ->
					new ProjectPermission(project, new ManageProject()).implies(it.getArgument(0)));
			assertNotNull(provider.authorize(subject, request, List.of(project.getId())));
			verify(dao).persist(argThat(entity -> entity instanceof AccessTokenAuthorization authorization
					&& authorization.getProject().equals(project) && authorization.getToken().getOwner().equals(owner)));
		}
	}

	@Test
	void exchangesCodeAndRotatesRefreshToken() {
		var first = provider.exchange(code(verifier, REDIRECT)).toSuccessResponse().getTokens();
		assertEquals(3600, first.getAccessToken().getLifetime());
		assertFalse(token.isExpired());
		assertNotEquals(first.getRefreshToken().getValue(), grant.getRefreshTokenHash());
		var second = provider.exchange(refresh(first.getRefreshToken(), null)).toSuccessResponse().getTokens();
		assertNotEquals(first.getAccessToken(), second.getAccessToken());
		assertNotEquals(first.getRefreshToken(), second.getRefreshToken());
		assertEquals(1, grant.getUsedRefreshTokenHashes().size());
		assertEquals(new Scope("profile", "role:code_reader"), second.getAccessToken().getScope());
		assertEquals(1, token.getAuthorizations().size());
	}

	@Test
	void roleScopesUseNormalizedNamesInsteadOfIds() throws Exception {
		var role = token.getAuthorizations().iterator().next().getRole();
		assertEquals("Role: Code Reader", provider.getScopes().get("role:code_reader"));
		assertEquals(List.of(role), provider.getRoles(Scope.parse("profile role:code_reader")));
		assertEquals(OAuth2Error.INVALID_SCOPE, assertThrows(ParseException.class,
				() -> provider.getRoles(new Scope("role:1"))).getErrorObject());
		role.setName("Reader + % / \"é\"");
		var scope = "role:reader_%2B_%25_%2F_%22%C3%A9%22";
		assertTrue(provider.getScopes().containsKey(scope));
		assertEquals(List.of(role), provider.getRoles(Scope.parse(scope)));
		assertThrows(ParseException.class, () -> provider.getRoles(new Scope("role:code_reader")));
	}

	@Test
	void renamedRoleIsRemovedWhenRefreshingAnExistingGrant() {
		var first = provider.exchange(code(verifier, REDIRECT)).toSuccessResponse().getTokens();
		token.getAuthorizations().iterator().next().getRole().setName("Renamed Reader");
		assertTrue(provider.exchange(refresh(first.getRefreshToken(), null)).indicatesSuccess());
		assertTrue(token.getAuthorizations().isEmpty());
	}

	@Test
	void rejectsWrongPkceAndRedirectWithoutConsumingCode() {
		assertError("invalid_grant", provider.exchange(code(new CodeVerifier(), REDIRECT)));
		assertError("invalid_grant", provider.exchange(code(verifier, URI.create("https://client.example/other"))));
		assertNull(grant.getLastUsedDate());
		assertTrue(provider.exchange(code(verifier, REDIRECT)).indicatesSuccess());
	}

	@Test
	void codeExchangeRequiresTheLoopbackPortUsedDuringAuthorization() {
		var redirect = URI.create("http://127.0.0.1:49152");
		grant.setRedirectUri(redirect.toString());
		assertError("invalid_grant", provider.exchange(code(verifier, URI.create("http://127.0.0.1:49153"))));
		assertNull(grant.getLastUsedDate());
		assertTrue(provider.exchange(code(verifier, redirect)).indicatesSuccess());
	}

	@Test
	void replayingAUsedCodeRevokesItsGrant() {
		assertTrue(provider.exchange(code(verifier, REDIRECT)).indicatesSuccess());
		assertError("invalid_grant", provider.exchange(code(verifier, REDIRECT)));
		verify(dao).remove(grant);
		verify(accessTokens).delete(token);
	}

	@Test
	void replayingRotatedRefreshTokenRevokesItsEntireGrant() {
		var first = provider.exchange(code(verifier, REDIRECT)).toSuccessResponse().getTokens();
		var second = provider.exchange(refresh(first.getRefreshToken(), null)).toSuccessResponse().getTokens();
		assertError("invalid_grant", provider.exchange(refresh(first.getRefreshToken(), null)));
		verify(dao).remove(grant);
		verify(accessTokens).delete(token);
		assertError("invalid_grant", provider.exchange(refresh(second.getRefreshToken(), null)));
	}

	@Test
	void refreshScopeCanNarrowButCannotExpand() {
		var first = provider.exchange(code(verifier, REDIRECT)).toSuccessResponse().getTokens();
		assertError("invalid_scope", provider.exchange(refresh(first.getRefreshToken(), new Scope("profile", "email"))));
		var second = provider.exchange(refresh(first.getRefreshToken(), new Scope("profile"))).toSuccessResponse().getTokens();
		assertEquals(new Scope("profile"), second.getAccessToken().getScope());
		assertTrue(token.getAuthorizations().isEmpty());
		assertError("invalid_scope", provider.exchange(refresh(second.getRefreshToken(), new Scope("profile", "role:code_reader"))));
	}

	@Test
	void expiredAndDisabledGrantsCannotIssueTokens() {
		grant.setCodeExpireDate(new Date(0));
		assertError("invalid_grant", provider.exchange(code(verifier, REDIRECT)));
		grant.setCodeExpireDate(new Date(System.currentTimeMillis() + 120_000));
		grant.getOwner().setDisabled(true);
		assertError("invalid_grant", provider.exchange(code(verifier, REDIRECT)));
	}

	@Test
	void publicClientsMustUsePkceAndCannotAuthenticateWithASecret() {
		application.setConfidential(false);
		assertError("invalid_client", provider.exchange(code(verifier, REDIRECT)));
		var request = new TokenRequest(ENDPOINT, new ClientID("client"),
				new AuthorizationCodeGrant(new AuthorizationCode("20.code-secret"), REDIRECT, verifier));
		assertTrue(provider.exchange(request).indicatesSuccess());
	}

	@Test
	void anotherClientCannotRedeemOrRevokeGrant() {
		var refresh = provider.exchange(code(verifier, REDIRECT)).toSuccessResponse().getTokens().getRefreshToken();
		var other = new OAuthApplication();
		other.setId(2L);
		grant.setApplication(other);
		assertError("invalid_grant", provider.exchange(code(verifier, REDIRECT)));
		assertEquals(200, provider.revoke(new TokenRevocationRequest(ENDPOINT, auth(), refresh)).getStatusCode());
		verify(dao, never()).remove(grant);
		verify(accessTokens, never()).delete(token);
	}

	@Test
	void unsupportedRefreshRequestDoesNotConsumeTokenOrNarrowScopes() throws Exception {
		var refresh = provider.exchange(code(verifier, REDIRECT)).toSuccessResponse().getTokens().getRefreshToken();
		var http = refresh(refresh, new Scope("profile")).toHTTPRequest();
		http.setBody(http.getBody() + "&resource=https%3A%2F%2Fapi.example");
		var request = TokenRequest.parse(http);
		assertError("invalid_request", provider.exchange(request));
		assertEquals("profile role:code_reader", grant.getScopes());
		assertTrue(provider.exchange(refresh(refresh, null)).indicatesSuccess());
	}

	@Test
	void disabledAccountIsAnInvalidBearerToken() {
		when(accessTokens.findByValue("disabled")).thenThrow(new DisabledAccountException());
		assertNull(provider.getUserInfo("disabled"));
	}

	@Test
	void revokingOneTokenLeavesAnotherAuthorizationUsable() {
		var otherGrant = new OAuthGrant();
		otherGrant.setId(21L);
		otherGrant.setOwner(grant.getOwner());
		otherGrant.setApplication(application);
		otherGrant.setScopes(grant.getScopes());
		otherGrant.setRedirectUri(grant.getRedirectUri());
		otherGrant.setCodeChallenge(grant.getCodeChallenge());
		otherGrant.setCodeHash(OAuthProtocol.hash("21.other-code"));
		otherGrant.setCodeExpireDate(grant.getCodeExpireDate());
		otherGrant.setExpireDate(grant.getExpireDate());
		var otherToken = new AccessToken();
		otherToken.setId(31L);
		otherToken.setOwner(grant.getOwner());
		otherToken.setOauthGrant(otherGrant);
		otherGrant.getAccessTokens().add(otherToken);
		when(dao.getSession().find(OAuthGrant.class, 21L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(otherGrant);
		provider.revokeGrant(grant.getId());
		verify(accessTokens).delete(token);
		verify(dao).remove(grant);
		assertTrue(grant.getAccessTokens().isEmpty());
		assertError("invalid_grant", provider.exchange(code(verifier, REDIRECT)));
		assertTrue(provider.exchange(new TokenRequest(ENDPOINT, auth(),
				new AuthorizationCodeGrant(new AuthorizationCode("21.other-code"), REDIRECT, verifier))).indicatesSuccess());
		verify(accessTokens, never()).delete(otherToken);
	}

	@Test
	void revokingAllTokensDeletesGrantsAndPreservesApplication() {
		var second = new OAuthGrant();
		second.setId(21L);
		second.setOwner(grant.getOwner());
		second.setApplication(application);
		second.setCodeExpireDate(grant.getCodeExpireDate());
		second.setExpireDate(grant.getExpireDate());
		var secondToken = new AccessToken();
		secondToken.setId(31L);
		second.getAccessTokens().add(secondToken);
		when(dao.getSession().find(OAuthGrant.class, 21L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(second);
		when(dao.query(org.mockito.ArgumentMatchers.<EntityCriteria<OAuthGrant>>any())).thenReturn(List.of(grant, second));
		assertEquals(2, provider.getGrants(grant.getOwner()).size());
		provider.revokeGrants(grant.getOwner(), application.getId());
		verify(accessTokens).delete(token);
		verify(accessTokens).delete(secondToken);
		verify(dao).remove(grant);
		verify(dao).remove(second);
		verify(dao, never()).remove(application);
	}

	@Test
	void revokingAllApplicationsRemovesTokensAcrossApplications() {
		var otherApplication = new OAuthApplication();
		otherApplication.setId(2L);
		otherApplication.setName("Another application");
		var otherGrant = new OAuthGrant();
		otherGrant.setId(21L);
		otherGrant.setOwner(grant.getOwner());
		otherGrant.setApplication(otherApplication);
		var otherToken = new AccessToken();
		otherToken.setId(31L);
		otherGrant.getAccessTokens().add(otherToken);
		when(dao.getSession().find(OAuthGrant.class, 21L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(otherGrant);
		when(dao.query(org.mockito.ArgumentMatchers.<EntityCriteria<OAuthGrant>>any())).thenReturn(List.of(grant, otherGrant));
		provider.revokeGrants(grant.getOwner());
		verify(accessTokens).delete(token);
		verify(accessTokens).delete(otherToken);
		verify(dao).remove(grant);
		verify(dao).remove(otherGrant);
	}

	@Test
	void grantListExcludesObsoleteEntriesButKeepsRefreshableTokens() {
		var expired = new OAuthGrant();
		expired.setExpireDate(new Date(0));
		when(dao.query(org.mockito.ArgumentMatchers.<EntityCriteria<OAuthGrant>>any())).thenReturn(List.of(grant, expired));
		assertEquals(List.of(grant), provider.getGrants(grant.getOwner()));
		grant.setCodeExpireDate(new Date(0));
		assertTrue(provider.getGrants(grant.getOwner()).isEmpty());
		grant.setLastUsedDate(new Date());
		assertTrue(token.isExpired());
		assertEquals(List.of(grant), provider.getGrants(grant.getOwner()));
	}

	@Test
	void cleanupRemovesExpiredEntriesAndRechecksPendingCodesUnderLock() throws Exception {
		var cluster = mock(ClusterService.class);
		when(cluster.isLeaderServer()).thenReturn(true);
		inject("clusterService", cluster);
		var sessions = mock(SessionService.class);
		when(sessions.call(any())).thenAnswer(it -> ((Callable<?>) it.getArgument(0)).call());
		inject("sessionService", sessions);
		var transactions = mock(TransactionService.class);
		doAnswer(it -> { ((Runnable) it.getArgument(0)).run(); return null; }).when(transactions).run(any());
		inject("transactionService", transactions);
		@SuppressWarnings("unchecked")
		var query = (org.hibernate.query.Query<Long>) mock(org.hibernate.query.Query.class);
		when(dao.getSession().createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(eq("now"), any(Date.class))).thenReturn(query);
		when(query.setMaxResults(100)).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(20L, 21L), List.of());
		grant.setCodeExpireDate(new Date(0));
		var redeemed = new OAuthGrant();
		redeemed.setId(21L);
		redeemed.setLastUsedDate(new Date());
		redeemed.setCodeExpireDate(new Date(0));
		redeemed.setExpireDate(new Date(System.currentTimeMillis() + 86400_000));
		when(dao.getSession().find(OAuthGrant.class, 21L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(redeemed);
		provider.execute();
		verify(dao).remove(grant);
		verify(accessTokens).delete(token);
		verify(dao, never()).remove(redeemed);
		assertTrue(grant.getAccessTokens().isEmpty());
	}
}
