package io.onedev.server.web.page.security;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.mail.MailService;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.taskbutton.TaskResult.PlainMessage;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.simple.SimplePage;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.jspecify.annotations.Nullable;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PasswordResetPage extends SimplePage {

	private static final String PARAM_PASSWORD_RESET_CODE = "passwordResetCode";
	
	private final String passwordResetCode;
	
	private String loginNameOrEmail;
	
	public PasswordResetPage(PageParameters params) {
		super(params);
		passwordResetCode = params.get(PARAM_PASSWORD_RESET_CODE).toOptionalString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (passwordResetCode == null) {
			var fragment = new Fragment("content", "requestToResetFrag", this);
			add(fragment);
			
			Form<?> form = new Form<Void>("form");
			form.add(new FencedFeedbackPanel("feedback", form));
			form.add(new TextField<>("loginNameOrEmail", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return loginNameOrEmail;
				}

				@Override
				public void setObject(String object) {
					loginNameOrEmail = object;
				}

			}).setLabel(Model.of(_T("Login name or email"))).setRequired(true));
			
			form.add(new TaskButton("resettingPassword") {

				@Override
				protected void onCompleted(AjaxRequestTarget target, boolean successful) {
					super.onCompleted(target, successful);
					if (successful)
						setResponsePage(LoginPage.class);
				}

				@Override
				protected TaskResult runTask(TaskLogger logger) {
					return OneDev.getInstance(SessionService.class).call(() -> {
						User user = getUserService().findByName(loginNameOrEmail);
						if (user == null) 
							user = getUserService().findByVerifiedEmailAddress(loginNameOrEmail);
						if (user == null) {
							throw new ExplicitException(_T("No user found with login name or email: ") + loginNameOrEmail);
						} else if (user.isServiceAccount() || user.isDisabled()) {
							throw new ExplicitException(_T("Can not reset password for service account or disabled user"));
						} else if (user.getPassword() == null) {
							throw new ExplicitException(_T("Can not reset password for user authenticating via external system"));
						} else {
							SettingService settingService = OneDev.getInstance(SettingService.class);
							if (settingService.getMailConnector() != null) {
								String passwordResetCode = CryptoUtils.generateSecret();
								user.setPasswordResetCode(passwordResetCode);
								getUserService().update(user, null);

								MailService mailService = OneDev.getInstance(MailService.class);

								Map<String, Object> bindings = new HashMap<>();
								bindings.put("passwordResetUrl", settingService.getSystemSetting().getServerUrl() + "/~reset-password/" + passwordResetCode);
								bindings.put("user", user);

								var template = settingService.getEmailTemplates().getPasswordReset();
								var htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
								var textBody = EmailTemplates.evalTemplate(false, template, bindings);

								String emailAddressValue;
								if (loginNameOrEmail.contains("@")) {
									emailAddressValue = loginNameOrEmail;
								} else {
									EmailAddress emailAddress = user.getPrimaryEmailAddress();
									if (emailAddress == null)
										throw new ExplicitException(_T("Primary email address not specified"));
									else if (!emailAddress.isVerified())
										throw new ExplicitException(_T("Your primary email address is not verified"));
									else
										emailAddressValue = emailAddress.getValue();
								}

								mailService.sendMail(Arrays.asList(emailAddressValue),
										Lists.newArrayList(), Lists.newArrayList(),
										"[Password Reset] You are Requesting to Reset Your OneDev Password",
										htmlBody, textBody, null, null, null);

								return new TaskResult(true, new PlainMessage(_T("Please check your email for password reset instructions")));
							} else {
								return new TaskResult(false, new PlainMessage(_T("Unable to send password reset email as mail service is not configured")));
							}
						}
					});
				}

			});

			form.add(new Link<Void>("cancel") {

				@Override
				public void onClick() {
					setResponsePage(LoginPage.class);
				}

			});

			fragment.add(form);
		} else {
			var userId = User.idOf(getUserService().findByPasswordResetCode(passwordResetCode));
			if (userId != null) {
				var fragment = new Fragment("content", "resetFrag", this);
				add(fragment);
				var bean = new PasswordResetBean();
				Form<?> form = new Form<Void>("form") {
					@Override
					protected void onSubmit() {
						super.onSubmit();
						var user = getUserService().load(userId);
						user.setPasswordResetCode(null);
						user.setPassword(OneDev.getInstance(PasswordService.class).encryptPassword(bean.getNewPassword()));
						getUserService().update(user, null);
						Session.get().success(_T("Password changed. Please login with your new password"));
						setResponsePage(LoginPage.class);
					}
				};
				form.add(BeanContext.edit("editor", bean));
				form.add(new Link<Void>("cancel") {

					@Override
					public void onClick() {
						setResponsePage(LoginPage.class);
					}

				});
				fragment.add(form);
			} else {
				throw new ExplicitException(_T("Password reset url is invalid or obsolete"));
			}
		}  
	}
	
	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}
	
	@Override
	protected String getTitle() {
		if (passwordResetCode == null)
			return _T("Forgotten Password?");
		else 
			return _T("Enter New Password");
	}

	@Override
	protected String getSubTitle() {
		if (passwordResetCode == null)
			return _T("Enter your user name or email to reset password");
		else 
			return null;
	}

	public static PageParameters paramsOf(@Nullable String passwordResetCode) {
		var params = new PageParameters();
		if (passwordResetCode != null)
			params.add(PARAM_PASSWORD_RESET_CODE, passwordResetCode);
		return params;
	}
	
}
