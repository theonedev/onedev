package io.onedev.server.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.model.support.TwoFactorAuthentication;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.authenticator.Authenticated;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.service.impl.DefaultMembershipService;

class PasswordOnlyAuthenticationTest extends AppLoaderMocker {

	private final SecuritySetting security = new SecuritySetting();
	private final User user = new User();
	private final UsernamePasswordToken password = new UsernamePasswordToken("alice", "password");
	private final DefaultPasswordService passwords = new DefaultPasswordService();
	private final HttpServletRequest request = mock(HttpServletRequest.class);
	private DefaultWebSecurityManager manager;
	private Subject subject;
	private SubjectThreadState threadState;

	@Mock
	private TransactionService transactions;

	@Mock
	private UserService users;

	@Mock
	private SettingService settings;

	@InjectMocks
	private DefaultAuthenticatingService realm = new DefaultAuthenticatingService(passwords);

	@Mock
	private GroupService groups;

	@Mock
	private Dao dao;

	@InjectMocks
	private DefaultMembershipService memberships;

	@Mock
	private AccessTokenService tokens;

	@Mock
	private ClusterService cluster;

	@InjectMocks
	private BearerAuthenticationFilter bearerFilter;

	@Override
	protected void setup() {
		when(AppLoader.getInstance(SettingService.class)).thenReturn(settings);
		when(settings.getSecuritySetting()).thenReturn(security);
		user.setName(password.getUsername());
		user.setPassword(passwords.encryptPassword(password.getPassword()));
		when(users.findByName(user.getName())).thenReturn(user);
		when(transactions.call(any())).thenAnswer(it -> it.<Callable<?>>getArgument(0).call());
		manager = new DefaultWebSecurityManager(Set.of(realm, new DefaultAuthorizingService()), null);
		when(AppLoader.getInstance(WebSecurityManager.class)).thenReturn(manager);
		subject = new Subject.Builder(manager).buildSubject();
		threadState = new SubjectThreadState(subject);
		threadState.bind();
	}

	@Override
	protected void teardown() {
		threadState.restore();
	}

	private void authenticateBasic(String credentials) {
		var encoded = Base64.getEncoder().encodeToString(credentials.getBytes(UTF_8));
		when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + encoded);
		new BasicAuthenticationFilter(tokens).onPreHandle(request, null, null);
	}

	@Test
	void globalEnforcementAppliesBeforeAndAfterEnrollment() {
		security.setEnforce2FA(true);
		assertSame(user, realm.getAuthenticationInfo(password));
		assertThrows(IncorrectCredentialsException.class,
				() -> realm.getAuthenticationInfo(new UsernamePasswordToken(user.getName(), "incorrect")));
		assertThrows(IncorrectCredentialsException.class, () -> authenticateBasic("alice:password"));
		user.setTwoFactorAuthentication(new TwoFactorAuthentication("secret", List.of()));
		assertThrows(IncorrectCredentialsException.class, () -> authenticateBasic("alice:password"));
		assertFalse(subject.isAuthenticated());

		security.setEnforce2FA(false);
		assertSame(user, manager.authenticate(password));
	}

	@Test
	void externalPasswordLoginEnforcesNewlyAssignedGroup() throws IllegalAccessException {
		var group = new Group();
		group.setName("developers");
		group.setEnforce2FA(true);
		when(groups.find(group.getName())).thenReturn(group);

		user.setPassword(null);
		var authenticator = mock(Authenticator.class);
		when(settings.getAuthenticator()).thenReturn(authenticator);
		when(authenticator.authenticate(password))
				.thenReturn(new Authenticated(null, null, Set.of(group.getName()), null));
		FieldUtils.writeField(realm, "membershipService", memberships, true);

		assertThrows(IncorrectCredentialsException.class, () -> subject.login(password));
		assertTrue(user.getGroups().contains(group));
		assertFalse(subject.isAuthenticated());
	}

	private AccessToken createAccessToken() {
		security.setEnforce2FA(true);
		var token = new AccessToken();
		token.setId(1L);
		token.setOwner(user);
		when(tokens.findByValue("access-token")).thenReturn(token);
		return token;
	}

	@ParameterizedTest
	@ValueSource(strings = {"alice:access-token", "access-token:"})
	void acceptsBasicAccessToken(String credentials) {
		var token = createAccessToken();
		authenticateBasic(credentials);
		assertEquals(token.getPrincipals(), SecurityUtils.getSubject().getPrincipals());
	}

	@Test
	void acceptsBearerAccessToken() {
		var token = createAccessToken();
		when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer access-token");
		when(cluster.getCredential()).thenReturn("cluster-credential");
		bearerFilter.onPreHandle(request, null, null);
		assertEquals(token.getPrincipals(), SecurityUtils.getSubject().getPrincipals());
	}
}
