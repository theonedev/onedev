package io.onedev.server.web.page.simple.security;

import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.page.simple.SimplePage;

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
			protected String runTask(TaskLogger logger) {
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
						if (settingManager.getMailSetting() != null) {
							String password = RandomStringUtils.random(10, true, true);								
							user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(password));
							userManager.save(user);
							
							MailManager mailManager = OneDev.getInstance(MailManager.class);
							
							String serverUrl = settingManager.getSystemSetting().getServerUrl();
							
							String htmlBody = String.format("Dear %s, "
								+ "<p style='margin: 16px 0;'>"
								+ "Per your request, password of your login \"%s\" at <a href=\"%s\">%s</a> has been reset to:<br>"
								+ "%s<br><br>"
								+ "Please login and change the password in your earliest convenience.",
								user.getDisplayName(), user.getName(), serverUrl, serverUrl, password);
	
							String textBody = String.format("Dear %s,\n\n"
									+ "Per your request, password of account \"%s\" at %s has been reset to:\n"
									+ "%s",
									user.getDisplayName(), user.getName(), serverUrl, password);
							
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
							
							mailManager.sendMail(
									settingManager.getMailSetting().getSendSetting(), 
									Arrays.asList(emailAddressValue),
									Lists.newArrayList(), Lists.newArrayList(), 
									"[Password Reset] Your OneDev Password Has Been Reset", 
									htmlBody, textBody, null, null);
							
							return "Please check your email " + emailAddressValue + " for the reset password";
						} else {
							throw new ExplicitException("Unable to send password reset email as smtp setting is not defined");
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
