package io.onedev.server.web.component.user.twofactorauthentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.onedev.server.model.User;
import io.onedev.server.model.support.TwoFactorAuthentication;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.service.UserService;
import io.onedev.server.service.impl.DefaultUserService;
import io.onedev.server.web.component.svg.SpriteImageResolver;
import io.onedev.server.web.translation.TranslationResolver;
import io.onedev.server.web.translation.TranslationStringResourceLoader;

class TwoFactorAuthenticationChallengePanelTest {

	private final User user = new User();
	private final UserService users = new DefaultUserService();
	private final Dao dao = mock(Dao.class);
	private final AtomicReference<User> verified = new AtomicReference<>();
	private WicketTester tester;

	@BeforeEach
	void setUp() throws IllegalAccessException {
		tester = new WicketTester();
		var app = tester.getApplication();
		app.getPageSettings().addComponentResolver(new TranslationResolver());
		app.getPageSettings().addComponentResolver(new SpriteImageResolver());
		app.getResourceSettings().getStringResourceLoaders().add(0, new TranslationStringResourceLoader());
		app.getComponentInstantiationListeners().add(component -> {
			if (component instanceof TwoFactorAuthenticationSetupPanel || component instanceof TwoFactorAuthenticationChallengePanel) {
				try {
					FieldUtils.writeField(component, "userService", users, true);
				} catch (IllegalAccessException e) {
					throw new AssertionError(e);
				}
			}
		});
		user.setId(42L);
		user.setName("alice");
		when(dao.load(User.class, user.getId())).thenReturn(user);
		when(dao.getSession()).thenReturn(mock(Session.class));
		FieldUtils.writeField(users, "dao", dao, true);
	}

	@AfterEach
	void tearDown() {
		tester.destroy();
	}

	private void startChallenge() {
		var panel = new TwoFactorAuthenticationChallengePanel("challenge", user.getId(), verified::set);
		tester.startComponentInPage(panel);
		assertNull(verified.get());
	}

	@Test
	void completesOnlyAfterMatchingTheAuthenticatorCode() {
		var factor = mock(TwoFactorAuthentication.class);
		when(factor.getTOTPCode()).thenReturn("123456");
		user.setTwoFactorAuthentication(factor);
		startChallenge();
		var form = tester.newFormTester("challenge:content:form");
		form.setValue("passcode", "000000");
		form.submit();
		assertNull(verified.get());
		form = tester.newFormTester("challenge:content:form");
		form.setValue("passcode", "123456");
		form.submit();
		assertSame(user, verified.get());
	}

	@Test
	void acceptsARecoveryCodeOnce() {
		user.setTwoFactorAuthentication(new TwoFactorAuthentication("secret", new ArrayList<>(List.of("recovery"))));
		startChallenge();
		tester.clickLink("challenge:content:verifyRecoveryCode");
		var form = tester.newFormTester("challenge:content:form");
		form.setValue("recoveryCode", "recovery");
		form.submit();
		assertSame(user, verified.getAndSet(null));
		verify(dao).persist(user);
		form = tester.newFormTester("challenge:content:form");
		form.setValue("recoveryCode", "recovery");
		form.submit();
		assertNull(verified.get());
	}

	@Test
	void completesEnrollmentAfterVerifyingTheNewAuthenticator() {
		startChallenge();
		var secret = submitEnrollmentCode();
		assertEquals(secret, user.getTwoFactorAuthentication().getSecretKey());
		assertNull(verified.get());
		tester.clickLink("challenge:content:content:ok");
		assertSame(user, verified.get());
	}

	@Test
	void pendingEnrollmentCannotReplaceAnAuthenticatorSetUpInAnotherRequest() {
		startChallenge();
		var otherFactor = new TwoFactorAuthentication("other-secret", List.of());
		user.setTwoFactorAuthentication(otherFactor);
		submitEnrollmentCode();
		assertSame(otherFactor, user.getTwoFactorAuthentication());
		assertNull(verified.get());
	}

	private String submitEnrollmentCode() {
		var path = "challenge:content:content:form";
		var secret = (Label) tester.getComponentFromLastRenderedPage(path + ":secretKey");
		var factor = new TwoFactorAuthentication(secret.getDefaultModelObjectAsString(), List.of());
		var form = tester.newFormTester(path);
		form.setValue("passcode", factor.getTOTPCode());
		form.submit("verify");
		return factor.getSecretKey();
	}
}
