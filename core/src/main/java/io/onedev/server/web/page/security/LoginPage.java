package io.onedev.server.web.page.security;

import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.manager.MailManager;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LoginPage extends BasePage {

	private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
	
	private String userName;
	
	private String password;

	private boolean rememberMe;
	
	public LoginPage(PageParameters params) {
		super(params);
		if (SecurityUtils.getSubject().isAuthenticated())
			throw new RestartResponseException(getApplication().getHomePage());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new NotificationPanel("feedback"));
		
		StatelessForm<?> form = new StatelessForm<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				String feedback = null;
				
				UserManager userManager = OneDev.getInstance(UserManager.class);
				if (userName != null) {
					User user = userManager.findByName(userName);
					if (user != null && user.getPassword() != null && user.getPassword().startsWith("@hash^prefix@")) {
						String hashAlgorithmChangeMessage = "OneDev password hash algorithm has been changed for security reason.";
						if (user.isRoot()) {
							feedback = hashAlgorithmChangeMessage + " Please reset administrator password by running "
									+ "reset_admin_password command from OneDev bin directory"; 
						} else {
							String password = RandomStringUtils.random(10, true, true);								
							user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(password));
							userManager.save(user);
							
							SettingManager configManager = OneDev.getInstance(SettingManager.class);
							if (configManager.getMailSetting() != null) {
								
								MailManager mailManager = OneDev.getInstance(MailManager.class);
								try {
									String mailBody = String.format("Dear %s, "
										+ "<p style='margin: 16px 0;'>"
										+ hashAlgorithmChangeMessage
										+ " As a result of this, password of your user \"%s\" has been reset to:<br>"
										+ "%s<br><br>"
										+ "-- Sent by OneDev", 
										user.getDisplayName(), user.getName(), password);

									mailManager.sendMail(configManager.getMailSetting(), Arrays.asList(user.getEmail()), 
											"Your OneDev password has been reset", mailBody);
									feedback = hashAlgorithmChangeMessage  
										+ " As a result of this, password of your user has been reset and sent to "
										+ "address " + user.getEmail();
								} catch (Exception e) {
									logger.error("Error sending password reset email", e);
									feedback = hashAlgorithmChangeMessage 
											+ " Since the reset password can not be been sent to your mail box "
											+ "due to mail error (check server log for details). Please "
											+ "contact OneDev administrator to reset your password manually";
								}
							} else {
								feedback = hashAlgorithmChangeMessage 
										+ " The reset password can not be sent as mail setting is not defined. "
										+ "Please contact OneDev administrator to reset your password manually";
							}
						}
					}
				}
				if (feedback != null) {
					error(feedback);
				} else {
					try {
						WebSession.get().login(userName, password, rememberMe);
						continueToOriginalDestination();
						setResponsePage(getApplication().getHomePage());
					} catch (AuthenticationException ae) {
						error("Authentication not passed");
					}
				}
			}
			
		};
		
		form.add(new TextField<String>("userName", new IModel<String>() {

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
			
		}));
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
			
		}));
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
		
		add(form);
		
		add(new ViewStateAwarePageLink<Void>("forgetPassword", ForgetPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(OneDev.getInstance(SettingManager.class).getMailSetting() != null);
			}
			
		});

		boolean enableSelfRegister = OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableSelfRegister();
		add(new ViewStateAwarePageLink<Void>("registerUser", RegisterPage.class).setVisible(enableSelfRegister));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new LoginResourceReference()));
	}

}
