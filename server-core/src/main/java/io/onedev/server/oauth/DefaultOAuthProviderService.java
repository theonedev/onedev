package io.onedev.server.oauth;

import static io.onedev.server.persistence.dao.Restrictions.eq;
import static io.onedev.server.oauth.OAuthProtocol.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.LockModeType;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authc.DisabledAccountException;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.*;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.model.OAuthApplication;
import io.onedev.server.model.OAuthGrant;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.Setting;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.SettingService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;

@Singleton
public class DefaultOAuthProviderService implements OAuthProviderService, SchedulableTask {

	@Inject
	private Dao dao;
	@Inject
	private AccessTokenService accessTokenService;
	@Inject
	private AuditService auditService;
	@Inject
	private ProjectService projectService;
	@Inject
	private RoleService roleService;
	@Inject
	private SettingService settingService;
	@Inject
	private TaskScheduler taskScheduler;
	@Inject
	private ClusterService clusterService;
	@Inject
	private SessionService sessionService;
	@Inject
	private TransactionService transactionService;

	private volatile String cleanupTaskId;

	@Override
	public RSAKey getPublicSigningKey() {
		return signingKey().toPublicJWK();
	}

	private void lockSigningKey() {
		dao.getSession().lock(settingService.findSetting(Setting.Key.OAUTH_SIGNING_KEY), LockModeType.PESSIMISTIC_WRITE);
	}

	private void revokeAllGrants() {
		for (var grant: dao.query(EntityCriteria.of(OAuthGrant.class)))
			revoke(grant);
	}

	@Transactional
	@Override
	public void generateSigningKey() {
		lockSigningKey();
		try {
			var key = new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyIDFromThumbprint(true).generate();
			revokeAllGrants();
			settingService.saveOAuthSigningKey(key.toJSONString());
			auditService.audit(null, "generated OAuth signing key", null, null);
		} catch (com.nimbusds.jose.JOSEException e) {
			throw new IllegalStateException("Unable to generate OAuth signing key", e);
		}
	}

	@Override
	public OAuthApplication getApplication(Long id) {
		return dao.load(OAuthApplication.class, id);
	}

	@Override
	public OAuthApplication findApplication(String clientId) {
		return dao.find(EntityCriteria.of(OAuthApplication.class).add(eq("clientId", clientId)));
	}

	@Override
	public List<OAuthApplication> getApplications(User owner) {
		return dao.query(EntityCriteria.of(OAuthApplication.class).add(eq("owner", owner)));
	}

	@Transactional
	@Override
	public String saveApplication(OAuthApplication application) {
		if (application.getOwner() == null)
			throw new ExplicitException("Application owner is required");
		if (application.getName() == null || application.getName().isBlank() || application.getName().length() > 255)
			throw new ExplicitException("Application name is required (up to 255 characters)");
		try {
			var homepage = URI.create(application.getHomepage());
			if (!List.of("http", "https").contains(homepage.getScheme()) || homepage.getHost() == null
					|| homepage.getUserInfo() != null || application.getHomepage().length() > 2048)
				throw new IllegalArgumentException();
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new ExplicitException("Specify a valid HTTP or HTTPS homepage");
		}
		if (application.getRedirectUris() == null || application.getRedirectUris().length() > 16000
				|| application.getRedirectUriList().isEmpty() || application.getRedirectUriList().size() > 20
				|| application.getRedirectUriList().stream().anyMatch(it -> it.length() > 2048
						|| !isValidRedirectUri(it, application.isConfidential())))
			throw new ExplicitException("Specify up to 20 exact redirect URIs, using HTTPS, loopback HTTP, or a native app scheme");
		boolean creating = application.isNew();
		String secret = null;
		if (creating) {
			application.setClientId(new ClientID().getValue());
			if (application.isConfidential()) {
				secret = new Secret(32).getValue();
				application.setSecretHash(hash(secret));
			}
		}
		dao.persist(application);
		auditService.audit(null, (creating ? "registered" : "updated") + " OAuth application \"" + application.getName() + "\"", null, null);
		return secret;
	}

	@Transactional
	@Override
	public String rotateSecret(Long id) {
		var application = getApplication(id);
		if (!application.isConfidential())
			throw new ExplicitException("Public applications do not have a client secret");
		String secret = new Secret(32).getValue();
		application.setSecretHash(hash(secret));
		dao.persist(application);
		revokeApplication(id);
		auditService.audit(null, "rotated secret of OAuth application \"" + application.getName() + "\"", null, null);
		return secret;
	}

	@Transactional
	@Override
	public void deleteApplication(Long id) {
		var application = getApplication(id);
		revokeApplication(id);
		auditService.audit(null, "deleted OAuth application \"" + application.getName() + "\"", null, null);
		dao.remove(application);
	}

	@Transactional
	@Override
	public void revokeApplication(Long id) {
		var application = getApplication(id);
		for (var grant: new ArrayList<>(application.getGrants()))
			revoke(grant);
	}

	@Override
	public List<OAuthGrant> getGrants(User owner) {
		return dao.query(EntityCriteria.of(OAuthGrant.class).add(eq("owner", owner))).stream()
				.filter(it -> !it.isExpired()).toList();
	}

	@Transactional
	@Override
	public void revokeGrant(Long id) {
		var grant = dao.getSession().find(OAuthGrant.class, id, LockModeType.PESSIMISTIC_WRITE);
		if (grant == null)
			return;
		revoke(grant);
	}

	@Transactional
	@Override
	public void revokeGrants(User owner, Long applicationId) {
		var grants = dao.query(EntityCriteria.of(OAuthGrant.class)
				.add(eq("owner", owner)).add(eq("application.id", applicationId)));
		for (var grant: grants)
			revoke(grant);
	}

	@Transactional
	@Override
	public void revokeGrants(User owner) {
		for (var grant: dao.query(EntityCriteria.of(OAuthGrant.class).add(eq("owner", owner))))
			revoke(grant);
	}

	private void removeGrant(OAuthGrant grant) {
		// Delete tokens explicitly so their removal also invalidates the access-token cache.
		for (var token: new ArrayList<>(grant.getAccessTokens()))
			accessTokenService.delete(token);
		grant.getAccessTokens().clear();
		dao.remove(grant);
	}

	private void revoke(OAuthGrant grant) {
		grant = dao.getSession().find(OAuthGrant.class, grant.getId(), LockModeType.PESSIMISTIC_WRITE);
		if (grant != null) {
			auditService.audit(null, "revoked OAuth grant " + grant.getId() + " for application \""
					+ grant.getApplication().getName() + "\" and user \"" + grant.getOwner().getName() + "\"", null, null);
			removeGrant(grant);
		}
	}

	@Listen
	public void on(SystemStarted event) {
		cleanupTaskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		if (cleanupTaskId != null)
			taskScheduler.unschedule(cleanupTaskId);
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}

	@Override
	public void execute() {
		if (!clusterService.isLeaderServer())
			return;
		while (true) {
			var ids = sessionService.call(() -> dao.getSession().createQuery("select id from OAuthGrant "
					+ "where expireDate <= :now "
					+ "or (lastUsedDate is null and codeExpireDate <= :now) order by id", Long.class)
					.setParameter("now", new Date()).setMaxResults(100).getResultList());
			if (ids.isEmpty())
				break;
			for (var id: ids) {
				transactionService.run(() -> {
					var grant = dao.getSession().find(OAuthGrant.class, id, LockModeType.PESSIMISTIC_WRITE);
					if (grant != null && grant.isExpired())
						removeGrant(grant);
				});
			}
		}
	}

	@Override
	public Map<String, String> getScopes() {
		Map<String, String> scopes = new LinkedHashMap<>();
		scopes.put("openid", "Sign in with your OneDev identity");
		scopes.put("profile", "Read your name and username");
		scopes.put("email", "Read your primary email address and verification status");
		scopes.put("offline_access", "Keep access while you are away (up to 30 days)");
		roleService.query().stream().sorted(java.util.Comparator.comparing(Role::getName))
				.forEach(it -> scopes.put(getRoleScope(it), "Role: " + it.getName()));
		return scopes;
	}

	private String getRoleScope(Role role) {
		var name = role.getName().toLowerCase(Locale.ROOT).replace(' ', '_');
		// Keep special and non-ASCII characters in a single scope token.
		return "role:" + URLEncoder.encode(name, StandardCharsets.UTF_8);
	}

	@Override
	public List<Role> getRoles(Scope scope) throws ParseException {
		if (scope == null || scope.isEmpty() || scope.toString().length() > 4096)
			throw new ParseException("Specify at least one supported scope", OAuth2Error.INVALID_SCOPE);
		Map<String, Role> rolesByScope = new LinkedHashMap<>();
		roleService.query().forEach(it -> rolesByScope.put(getRoleScope(it), it));
		List<Role> roles = new ArrayList<>();
		for (var value: scope) {
			String name = value.getValue();
			if (List.of("openid", "profile", "email", "offline_access").contains(name))
				continue;
			var role = rolesByScope.get(name);
			if (role == null)
				throw new ParseException("Unknown scope", OAuth2Error.INVALID_SCOPE);
			roles.add(role);
		}
		return roles;
	}

	@Transactional
	@Override
	public String authorize(Subject subject, AuthorizationRequest request, Collection<Long> projectIds) throws ParseException {
		var owner = SecurityUtils.getAuthUser(subject);
		var application = findApplication(request.getClientID().getValue());
		if (application == null || !hasRegisteredRedirect(application, request)
				|| application.getOwner().isDisabled())
			throw new ParseException("Invalid application or redirect URI", OAuth2Error.INVALID_REQUEST);
		validateAuthorization(request);
		var roles = getRoles(request.getScope());
		if (roles.isEmpty() != projectIds.isEmpty())
			throw new ExplicitException("Select at least one project for the requested roles");
		List<Project> projects = new ArrayList<>();
		for (Long projectId: new java.util.HashSet<>(projectIds)) {
			var project = projectService.load(projectId);
			if (Project.DEFAULT_ID.equals(projectId) || !SecurityUtils.canManageProject(subject, project))
				throw new UnauthorizedException("OAuth authorization owner should have permission to manage authorized project");
			projects.add(project);
		}
		var grant = new OAuthGrant();
		grant.setOwner(owner);
		grant.setApplication(application);
		grant.setScopes(request.getScope().toString());
		grant.setRedirectUri(request.getRedirectionURI().toString());
		grant.setCodeChallenge(request.getCodeChallenge().getValue());
		grant.setCodeExpireDate(new Date(System.currentTimeMillis() + 120_000));
		grant.setExpireDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_SECONDS * 1000));
		var nonce = request.getCustomParameter("nonce");
		if (nonce != null)
			grant.setNonce(nonce.get(0));
		dao.persist(grant);
		String code = grant.getId() + "." + new AuthorizationCode(32).getValue();
		grant.setCodeHash(hash(code));
		var token = new AccessToken();
		token.setOwner(owner);
		token.setName("OAuth grant " + grant.getId() + " (" + new ClientID().getValue() + ")");
		token.setOauthGrant(grant);
		// Not usable until the code is exchanged.
		token.setExpireDate(new Date(0));
		accessTokenService.createOrUpdate(token);
		grant.getAccessTokens().add(token);
		for (var project: projects) {
			for (var role: roles) {
				var authorization = new AccessTokenAuthorization();
				authorization.setProject(project);
				authorization.setRole(role);
				authorization.setToken(token);
				dao.persist(authorization);
				token.getAuthorizations().add(authorization);
			}
		}
		auditService.audit(null, "authorized OAuth application \"" + application.getName() + "\" (grant "
				+ grant.getId() + ", scopes: " + grant.getScopes() + ")", null, null);
		return code;
	}

	private OAuthApplication authenticate(ClientID clientId, ClientAuthentication authentication) {
		if (authentication != null)
			clientId = authentication.getClientID();
		if (clientId == null)
			return null;
		var application = findApplication(clientId.getValue());
		if (application == null || application.getOwner().isDisabled())
			return null;
		if (application.isConfidential()) {
			if (!(authentication instanceof ClientSecretBasic || authentication instanceof ClientSecretPost)
					|| !matches(((PlainClientSecret) authentication).getClientSecret().getValue(), application.getSecretHash()))
				return null;
		} else if (authentication != null) {
			return null;
		}
		return application;
	}

	private OAuthGrant lockGrant(String credential) {
		var id = grantId(credential);
		if (id == null)
			return null;
		return dao.getSession().find(OAuthGrant.class, id, LockModeType.PESSIMISTIC_WRITE);
	}

	@Transactional
	@Override
	public TokenResponse exchange(TokenRequest request) {
		var application = authenticate(request.getClientID(), request.getClientAuthentication());
		if (application == null)
			return new TokenErrorResponse(OAuth2Error.INVALID_CLIENT);
		if (request.getResources() != null || request.getAuthorizationDetails() != null)
			return new TokenErrorResponse(OAuth2Error.INVALID_REQUEST);
		OAuthGrant grant;
		if (request.getAuthorizationGrant() instanceof AuthorizationCodeGrant code) {
			grant = lockGrant(code.getAuthorizationCode().getValue());
			if (grant == null || !application.equals(grant.getApplication()) || !grant.isActive()
					|| !matches(code.getAuthorizationCode().getValue(), grant.getCodeHash())
					|| code.getRedirectionURI() == null || !grant.getRedirectUri().equals(code.getRedirectionURI().toString())
					|| !verifyPkce(grant.getCodeChallenge(), code.getCodeVerifier()))
				return new TokenErrorResponse(OAuth2Error.INVALID_GRANT);
			if (grant.getLastUsedDate() != null) {
				revoke(grant);
				return new TokenErrorResponse(OAuth2Error.INVALID_GRANT);
			}
			if (grant.getCodeExpireDate().before(new Date()))
				return new TokenErrorResponse(OAuth2Error.INVALID_GRANT);
		} else if (request.getAuthorizationGrant() instanceof RefreshTokenGrant refresh) {
			String value = refresh.getRefreshToken().getValue();
			grant = lockGrant(value);
			if (grant == null || !application.equals(grant.getApplication()) || !grant.isActive())
				return new TokenErrorResponse(OAuth2Error.INVALID_GRANT);
			if (grant.getUsedRefreshTokenHashes().contains(hash(value))) {
				revoke(grant);
				return new TokenErrorResponse(OAuth2Error.INVALID_GRANT);
			}
			if (!matches(value, grant.getRefreshTokenHash()))
				return new TokenErrorResponse(OAuth2Error.INVALID_GRANT);
			if (request.getScope() != null && !Scope.parse(grant.getScopes()).containsAll(request.getScope()))
				return new TokenErrorResponse(OAuth2Error.INVALID_SCOPE);
			if (request.getScope() != null)
				grant.setScopes(request.getScope().toString());
			grant.getUsedRefreshTokenHashes().add(grant.getRefreshTokenHash());
		} else {
			return new TokenErrorResponse(OAuth2Error.UNSUPPORTED_GRANT_TYPE);
		}
		var scope = Scope.parse(grant.getScopes());
		var token = grant.getAccessTokens().stream().findFirst().orElse(null);
		if (token == null)
			return new TokenErrorResponse(OAuth2Error.INVALID_GRANT);
		// Narrowing a refresh scope also removes the corresponding project authorizations.
		for (var authorization: new ArrayList<>(token.getAuthorizations())) {
			if (!scope.contains(getRoleScope(authorization.getRole()))) {
				dao.remove(authorization);
				token.getAuthorizations().remove(authorization);
			}
		}
		long lifetime = Math.min(ACCESS_TOKEN_SECONDS, (grant.getExpireDate().getTime() - System.currentTimeMillis()) / 1000);
		token.setValue(new BearerAccessToken(32).getValue());
		token.setExpireDate(new Date(System.currentTimeMillis() + lifetime * 1000));
		accessTokenService.createOrUpdate(token);
		RefreshToken refresh = new RefreshToken(grant.getId() + "." + new RefreshToken(32).getValue());
		grant.setRefreshTokenHash(hash(refresh.getValue()));
		grant.setLastUsedDate(new Date());
		dao.persist(grant);
		var bearer = new BearerAccessToken(token.getValue(), lifetime, scope);
		if (scope.contains("openid"))
			return new OIDCTokenResponse(new OIDCTokens(idToken(grant), bearer, refresh));
		return new AccessTokenResponse(new Tokens(bearer, refresh));
	}

	@Transactional
	@Override
	public HTTPResponse revoke(TokenRevocationRequest request) {
		var application = authenticate(request.getClientID(), request.getClientAuthentication());
		if (application == null)
			return new TokenErrorResponse(OAuth2Error.INVALID_CLIENT).toHTTPResponse();
		String value = request.getToken().getValue();
		var grant = lockGrant(value);
		if (grant != null && application.equals(grant.getApplication())
				&& (matches(value, grant.getRefreshTokenHash()) || grant.getUsedRefreshTokenHashes().contains(hash(value)))) {
			revoke(grant);
		} else {
			var token = findAccessToken(value);
			if (token != null && token.getOauthGrant() != null && application.equals(token.getOauthGrant().getApplication()))
				revoke(token.getOauthGrant());
		}
		return new HTTPResponse(200);
	}

	private String issuer() {
		return settingService.getSystemSetting().getServerUrl().replaceAll("/+$", "");
	}

	@Transactional
	@Override
	public Map<String, Object> getMetadata() {
		var metadata = new OIDCProviderMetadata(new Issuer(issuer()), List.of(SubjectType.PUBLIC), URI.create(issuer() + "/~oauth/jwks"));
		metadata.setAuthorizationEndpointURI(URI.create(issuer() + "/~oauth/authorize"));
		metadata.setTokenEndpointURI(URI.create(issuer() + "/~oauth/token"));
		metadata.setRevocationEndpointURI(URI.create(issuer() + "/~oauth/revoke"));
		metadata.setUserInfoEndpointURI(URI.create(issuer() + "/~oauth/userinfo"));
		metadata.setResponseTypes(List.of(new ResponseType(ResponseType.Value.CODE)));
		metadata.setResponseModes(List.of(ResponseMode.QUERY));
		metadata.setGrantTypes(List.of(GrantType.AUTHORIZATION_CODE, GrantType.REFRESH_TOKEN));
		metadata.setCodeChallengeMethods(List.of(CodeChallengeMethod.S256));
		var methods = List.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, ClientAuthenticationMethod.CLIENT_SECRET_POST, ClientAuthenticationMethod.NONE);
		metadata.setTokenEndpointAuthMethods(methods);
		metadata.setRevocationEndpointAuthMethods(methods);
		metadata.setIDTokenJWSAlgs(List.of(JWSAlgorithm.RS256));
		metadata.setScopes(new Scope(getScopes().keySet().toArray(String[]::new)));
		metadata.setClaims(List.of("sub", "name", "preferred_username", "email", "email_verified"));
		metadata.setSupportsRequestParam(false);
		metadata.setSupportsRequestURIParam(false);
		metadata.setCustomParameter("onedev_scope_descriptions", getScopes());
		return metadata.toJSONObject();
	}

	private RSAKey signingKey() {
		try {
			return RSAKey.parse(settingService.getOAuthSigningKey());
		} catch (java.text.ParseException e) {
			throw new IllegalStateException("Unable to load OAuth signing key", e);
		}
	}

	@Transactional
	@Override
	public Map<String, Object> getJwks() {
		return new JWKSet(signingKey().toPublicJWK()).toJSONObject();
	}

	private SignedJWT idToken(OAuthGrant grant) {
		try {
			var key = signingKey();
			var claims = new JWTClaimsSet.Builder().issuer(issuer()).subject(grant.getOwner().getId().toString())
					.audience(grant.getApplication().getClientId()).issueTime(new Date())
					.expirationTime(new Date(System.currentTimeMillis() + ACCESS_TOKEN_SECONDS * 1000));
			if (grant.getNonce() != null)
				claims.claim("nonce", grant.getNonce());
			var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims.build());
			jwt.sign(new RSASSASigner(key));
			return jwt;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to sign ID token", e);
		}
	}

	@Sessional
	@Override
	public Map<String, Object> getUserInfo(String value) {
		var token = findAccessToken(value);
		if (token == null || token.getOauthGrant() == null)
			return null;
		var scope = Scope.parse(token.getOauthGrant().getScopes());
		var owner = token.getOwner();
		Map<String, Object> claims = new LinkedHashMap<>();
		claims.put("sub", owner.getId().toString());
		if (scope.contains("profile")) {
			claims.put("name", owner.getDisplayName());
			claims.put("preferred_username", owner.getName());
		}
		if (scope.contains("email") && owner.getPrimaryEmailAddress() != null) {
			claims.put("email", owner.getPrimaryEmailAddress().getValue());
			claims.put("email_verified", owner.getPrimaryEmailAddress().isVerified());
		}
		return claims;
	}

	private AccessToken findAccessToken(String value) {
		try {
			return accessTokenService.findByValue(value);
		} catch (DisabledAccountException e) {
			return null;
		}
	}
}
