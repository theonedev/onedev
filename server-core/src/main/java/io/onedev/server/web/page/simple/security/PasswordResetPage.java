package io.onedev.server.web.page.simple.security;

import com.google.common.collect.Lists;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.taskbutton.TaskResult.PlainMessage;
import io.onedev.server.web.page.simple.SimplePage;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class PasswordResetPage extends SimplePage {

	private String loginNameOrEmail;
	
	public PasswordResetPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
					UserManager userManager = OneDev.getInstance(UserManager.class);
					User user = userManager.findByName(loginNameOrEmail);
					if (user == null) 
						user = userManager.findByVerifiedEmailAddress(loginNameOrEmail);
					if (user == null) {
						throw new ExplicitException("No user found with login name or verified email: " + loginNameOrEmail);
					} else {
						SettingManager settingManager = OneDev.getInstance(SettingManager.class);
						if (settingManager.getMailService() != null) {
							String password = CryptoUtils.generateSecret();								
							user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(password));
							user.setSsoConnector(null);
							userManager.update(user, null);
							
							MailManager mailManager = OneDev.getInstance(MailManager.class);

							Map<String, Object> bindings = new HashMap<>();
							bindings.put("serverUrl", settingManager.getSystemSetting().getServerUrl());
							bindings.put("user", user);
							bindings.put("newPassword", password);

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
									"[Password Reset] Your OneDev Password Has Been Reset", 
									htmlBody, textBody, null, null, null);
							
							return new TaskResult(true, new PlainMessage("Please check your email " + emailAddressValue + " for the reset password"));
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
		
		add(form);
	}
	
	@Override
	protected String getTitle() {
		return "Forgotten Password?";
	}

	@Override
	protected String getSubTitle() {
		return "Enter your user name or email to reset password";
	}

}
