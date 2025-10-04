package io.onedev.server.web.page.security;

import static io.onedev.server.web.page.security.SsoProcessPage.MOUNT_PATH;
import static io.onedev.server.web.page.security.SsoProcessPage.STAGE_INITIATE;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.SsoProviderService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.realm.PasswordAuthenticatingRealm;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.twofactorauthentication.TwoFactorAuthenticationSetupPanel;
import io.onedev.server.web.page.simple.SimpleCssResourceReference;
import io.onedev.server.web.page.simple.SimplePage;

public class LoginPage extends SimplePage {

	private String userName;
	
	private String password;
	
	private String passcode;
	
	private String recoveryCode;

	private boolean rememberMe;
	
	private String errorMessage;
	
	private String subTitle = _T("Enter your details to login to your account");
	
	public LoginPage(PageParameters params) {
		super(params);
		
		if (SecurityUtils.getAuthUser() != null)
			throw new RestartResponseException(getApplication().getHomePage());
	}
	
	public LoginPage(String errorMessage) {
		super(new PageParameters());
		this.errorMessage = errorMessage;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Fragment fragment = new Fragment("content", "passwordCheckFrag", this);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				try {
					var token = new UsernamePasswordToken(userName, password, rememberMe);
					var user = (User) OneDev.getInstance(PasswordAuthenticatingRealm.class).getAuthenticationInfo(token);
					if (user.isEnforce2FA()) {
						if (user.getTwoFactorAuthentication() != null) {
							subTitle = _T("Two-factor authentication is enabled. Please input passcode displayed on your TOTP authenticator. If you encounter problems, make sure time of OneDev server and your device running TOTP authenticator is in sync");
							newPasscodeVerifyFrag(user.getId());
						} else {
							subTitle = _T("Set up two-factor authentication");
							newTwoFactorAuthenticationSetup(user.getId());
						}
					} else {
						afterLogin(user);
					}
				} catch (IncorrectCredentialsException|UnknownAccountException e) {
					error(_T("Invalid credentials"));
				} catch (AuthenticationException ae) {
					error(ae.getMessage());
				}
			}
			
		};
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		if (errorMessage != null) 
			form.error(errorMessage);
		
		form.add(new TextField<>("userName", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return userName;
			}

			@Override
			public void setObject(String object) {
				userName = object;
			}

		}).setLabel(Model.of(_T("User name"))).setRequired(true));
		
		form.add(new PasswordTextField("password", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return password;
			}

			@Override
			public void setObject(String object) {
				password = object;
			}
			
		}).setLabel(Model.of(_T("Password"))).setRequired(true));
		
		form.add(new CheckBox("rememberMe", new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return rememberMe;
			}

			@Override
			public void setObject(Boolean object) {
				rememberMe = object;
			}
			
		}));
		
		form.add(new ViewStateAwarePageLink<Void>("forgetPassword", PasswordResetPage.class));
		fragment.add(form);
		
		SettingService settingService = OneDev.getInstance(SettingService.class);
		
		boolean enableSelfRegister = settingService.getSecuritySetting().isEnableSelfRegister();
		fragment.add(new ViewStateAwarePageLink<Void>("registerUser", SignUpPage.class).setVisible(enableSelfRegister));

		String serverUrl = settingService.getSystemSetting().getServerUrl();
		
		var ssoProviderService = OneDev.getInstance(SsoProviderService.class);
		RepeatingView ssoButtonsView = new RepeatingView("ssoButtons");
		var ssoProviders = ssoProviderService.query();
		for (SsoProvider provider: ssoProviders) {
			ExternalLink ssoButton = new ExternalLink(ssoButtonsView.newChildId(), 
					Model.of(serverUrl + "/" + MOUNT_PATH + "/" + STAGE_INITIATE + "/" + provider.getName()));
			ssoButton.add(new ExternalImage("image", provider.getConnector().getButtonImageUrl()));
			ssoButton.add(new Label("label", MessageFormat.format(_T("Login with {0}"), provider.getName())));
			ssoButtonsView.add(ssoButton);
		}
		fragment.add(ssoButtonsView.setVisible(!ssoProviders.isEmpty()));
		
		add(fragment);
	}
	
	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}
	
	private void afterLogin(User user) {
		RememberMeManager rememberMeManager = OneDev.getInstance(RememberMeManager.class);
		if (rememberMe) {
			AuthenticationToken token = new UsernamePasswordToken(userName, password, rememberMe);
			rememberMeManager.onSuccessfulLogin(SecurityUtils.getSubject(), token, user);
		} else {
			SecurityUtils.getSubject().runAs(user.getPrincipals());
		}
		
		continueToOriginalDestination();
		throw new RestartResponseException(getApplication().getHomePage());
	}
	
	private void newTwoFactorAuthenticationSetup(Long userId) {
		replace(new TwoFactorAuthenticationSetupPanel("content") {
			
			@Override
			protected void onConfigured(AjaxRequestTarget target) {
				afterLogin(getUser());
			}
			
			@Override
			protected User getUser() {
				return getUserService().load(userId);
			}
			
		});
	}

	private void newPasscodeVerifyFrag(Long userId) {
		Fragment fragment = new Fragment("content", "passcodeVerifyFrag", this);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				User user = getUserService().load(userId);
				if (user.getTwoFactorAuthentication().getTOTPCode().equals(passcode)) 
					afterLogin(user);
				else 
					error("Passcode verification failed");
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(new TextField<String>("passcode", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return passcode;
			}

			@Override
			public void setObject(String object) {
				LoginPage.this.passcode = object;
			}
			
		}).setLabel(Model.of(_T("Passcode"))).setRequired(true));
		
		fragment.add(form);

		fragment.add(new Link<Void>("verifyRecoveryCode") {

			@Override
			public void onClick() {
				subTitle = _T("Please input one of your recovery codes saved when enable two-factor authentication");
				newRecoveryCodeVerifyFrag(userId);
			}
			
		});
		replace(fragment);
	}

	private void newRecoveryCodeVerifyFrag(Long userId) {
		Fragment fragment = new Fragment("content", "recoveryCodeVerifyFrag", this);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				User user = getUserService().load(userId);
				if (user.getTwoFactorAuthentication().getScratchCodes().remove(recoveryCode)) {
					getUserService().update(user, null);
					afterLogin(user);
				} else {
					error("Recovery code verification failed");
				}
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(new TextField<String>("recoveryCode", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return recoveryCode;
			}

			@Override
			public void setObject(String object) {
				LoginPage.this.recoveryCode = object;
			}
			
		}).setLabel(Model.of(_T("Recovery code"))).setRequired(true));
		
		fragment.add(form);
		replace(fragment);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SimpleCssResourceReference()));
	}

	@Override
	protected String getTitle() {
		return _T("Sign In To") + " " + OneDev.getInstance(SettingService.class).getBrandingSetting().getName();
	}

	@Override
	protected String getSubTitle() {
		return subTitle;
	}
	
}
