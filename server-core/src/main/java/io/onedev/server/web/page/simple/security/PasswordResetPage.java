package io.onedev.server.web.page.simple.security;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.persistence.SessionManager;
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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
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
			form.add(new TextField<String>("loginNameOrEmail", new IModel<String>() {

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

			}).setLabel(Model.of("Login name or email")).setRequired(true));

			form.add(new TaskButton("resettingPassword") {

				@Override
				protected void onCompleted(AjaxRequestTarget target, boolean successful) {
					super.onCompleted(target, successful);
					if (successful)
						setResponsePage(LoginPage.class);
				}

				@Override
				protected TaskResult runTask(TaskLogger logger) {
					OneDev.getInstance(SessionManager.class).openSession();
					try {
						User user = getUserManager().findByName(loginNameOrEmail);
						if (user == null)
							user = getUserManager().findByVerifiedEmailAddress(loginNameOrEmail);
						if (user == null) {
							throw new ExplicitException("No user found with login name or verified email: " + loginNameOrEmail);
						} else {
							SettingManager settingManager = OneDev.getInstance(SettingManager.class);
							if (settingManager.getMailService() != null) {
								String passwordResetCode = CryptoUtils.generateSecret();
								user.setPasswordResetCode(passwordResetCode);
								getUserManager().update(user, null);

								MailManager mailManager = OneDev.getInstance(MailManager.class);

								Map<String, Object> bindings = new HashMap<>();
								bindings.put("passwordResetUrl", settingManager.getSystemSetting().getServerUrl() + "/~reset-password/" + passwordResetCode);
								bindings.put("user", user);

								var template = settingManager.getEmailTemplates().getPasswordReset();
								var htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
								var textBody = EmailTemplates.evalTemplate(false, template, bindings);

								String emailAddressValue;
								if (loginNameOrEmail.contains("@")) {
									emailAddressValue = loginNameOrEmail;
								} else {
									EmailAddress emailAddress = user.getPrimaryEmailAddress();
									if (emailAddress == null)
										throw new ExplicitException("Primary email address not specified");
									else if (!emailAddress.isVerified())
										throw new ExplicitException("Your primary email address is not verified");
									else
										emailAddressValue = emailAddress.getValue();
								}

								mailManager.sendMail(Arrays.asList(emailAddressValue),
										Lists.newArrayList(), Lists.newArrayList(),
										"[Password Reset] You are Requesting to Reset Your OneDev Password",
										htmlBody, textBody, null, null, null);

								return new TaskResult(true, new PlainMessage("Please check your email for password reset instructions"));
							} else {
								return new TaskResult(false, new PlainMessage("Unable to send password reset email as smtp settings are not defined"));
							}
						}
					} finally {
						OneDev.getInstance(SessionManager.class).closeSession();
					}
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
			var userId = User.idOf(getUserManager().findByPasswordResetCode(passwordResetCode));
			if (userId != null) {
				var fragment = new Fragment("content", "resetFrag", this);
				add(fragment);
				var bean = new PasswordResetBean();
				Form<?> form = new Form<Void>("form") {
					@Override
					protected void onSubmit() {
						super.onSubmit();
						var user = getUserManager().load(userId);
						user.setPasswordResetCode(null);
						user.setSsoConnector(null);
						user.setPassword(OneDev.getInstance(PasswordService.class).encryptPassword(bean.getNewPassword()));
						getUserManager().update(user, null);
						Session.get().success("Password changed. Please login with your new password");
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
				throw new ExplicitException("Password reset url is invalid or obsolete");
			}
		} 
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	@Override
	protected String getTitle() {
		if (passwordResetCode == null)
			return "Forgotten Password?";
		else 
			return "Enter New Password";
	}

	@Override
	protected String getSubTitle() {
		if (passwordResetCode == null)
			return "Enter your user name or email to reset password";
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
