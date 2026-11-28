package io.onedev.server.web.component.user.twofactorauthentication;

import static io.onedev.server.model.User.Type.ORDINARY;
import static io.onedev.server.web.translation.Translation._T;

import jakarta.inject.Inject;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.danekja.java.util.function.serializable.SerializableConsumer;

import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.UserService;
import io.onedev.server.web.page.security.LoginPage;

public class TwoFactorAuthenticationChallengePanel extends Panel {

	@Inject
	private UserService userService;

	private final Long userId;
	private final SerializableConsumer<User> onVerified;
	private String prompt;

	public TwoFactorAuthenticationChallengePanel(String id, Long userId, SerializableConsumer<User> onVerified) {
		super(id);
		this.userId = userId;
		this.onVerified = onVerified;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (loadUser().getTwoFactorAuthentication() == null) {
			prompt = _T("Set up two-factor authentication");
			add(new TwoFactorAuthenticationSetupPanel("content") {
				@Override
				protected User getUser() {
					return loadUser();
				}

				@Override
				protected void onConfigured(AjaxRequestTarget target) {
					onVerified.accept(getUser());
				}
			});
		} else {
			add(newVerificationForm(false));
		}
	}

	private User loadUser() {
		var user = userService.load(userId);
		if (user.isDisabled() || user.getType() != ORDINARY)
			throw new RestartResponseException(new LoginPage(_T(SecurityUtils.AUTHENTICATION_FAILED_MESSAGE)));
		return user;
	}

	private Fragment newVerificationForm(boolean recovery) {
		prompt = recovery
				? _T("Please input one of your recovery codes saved when enable two-factor authentication")
				: _T("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of ${productName} server and your device running TOTP authenticator is in sync");
		var fragment = new Fragment("content", recovery ? "recoveryCodeVerifyFrag" : "passcodeVerifyFrag", this);
		var code = new TextField<String>(recovery ? "recoveryCode" : "passcode", Model.of(""));
		code.setLabel(Model.of(recovery ? _T("Recovery code") : _T("Passcode"))).setRequired(true);
		var form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				var user = loadUser();
				var factor = user.getTwoFactorAuthentication();
				if (factor == null) {
					error(_T("Two-factor authentication has changed. Please sign in again."));
					return;
				}
				if (recovery) {
					if (!userService.consumeRecoveryCode(user, code.getModelObject())) {
						error(_T("Recovery code verification failed"));
						return;
					}
				} else if (!factor.getTOTPCode().equals(code.getModelObject())) {
					error(_T("Passcode verification failed"));
					return;
				}
				onVerified.accept(user);
			}
		};
		form.add(code, new FencedFeedbackPanel("feedback", form));
		fragment.add(form);
		if (!recovery) {
			fragment.add(new Link<Void>("verifyRecoveryCode") {
				@Override
				public void onClick() {
					TwoFactorAuthenticationChallengePanel.this.replace(newVerificationForm(true));
				}
			});
		}
		return fragment;
	}

	public String getPrompt() {
		return prompt;
	}
}
