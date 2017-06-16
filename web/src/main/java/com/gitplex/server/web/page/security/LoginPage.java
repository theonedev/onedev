package com.gitplex.server.web.page.security;

import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.MailManager;
import com.gitplex.server.model.User;
import com.gitplex.server.web.WebSession;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.base.BasePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class LoginPage extends BasePage {

	private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
	
	private String userName;
	
	private String password;

	private boolean rememberMe;
	
	public LoginPage() {
		if (SecurityUtils.getSubject().isAuthenticated())
			throw new RestartResponseException(getApplication().getHomePage());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new NotificationPanel("feedback"));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				String feedback = null;
				
				UserManager userManager = GitPlex.getInstance(UserManager.class);
				if (userName != null) {
					User user = userManager.findByName(userName);
					if (user != null && user.getPassword() != null && user.getPassword().startsWith("@hash^prefix@")) {
						String hashAlgorithmChangeMessage = "GitPlex password hash algorithm has been changed for security reason.";
						if (user.isRoot()) {
							feedback = hashAlgorithmChangeMessage + " Please reset administrator password by running "
									+ "reset_admin_password command from GitPlex bin directory"; 
						} else {
							String password = RandomStringUtils.random(10, true, true);								
							user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(password));
							userManager.save(user);
							
							ConfigManager configManager = GitPlex.getInstance(ConfigManager.class);
							if (configManager.getMailSetting() != null) {
								
								MailManager mailManager = GitPlex.getInstance(MailManager.class);
								try {
									String mailBody = String.format("Dear %s, "
										+ "<p style='margin: 16px 0;'>"
										+ hashAlgorithmChangeMessage
										+ " As a result of this, password of your user \"%s\" has been reset to:<br>"
										+ "%s<br><br>"
										+ "-- Sent by GitPlex", 
										user.getDisplayName(), user.getName(), password);

									mailManager.sendMail(configManager.getMailSetting(), Arrays.asList(user.getEmail()), 
											"Your GitPlex password has been reset", mailBody);
									feedback = hashAlgorithmChangeMessage  
										+ " As a result of this, password of your user has been reset and sent to "
										+ "address " + user.getEmail();
								} catch (Exception e) {
									logger.error("Error sending password reset email", e);
									feedback = hashAlgorithmChangeMessage 
											+ " Since the reset password can not be been sent to your mail box "
											+ "due to mail error (check server log for details). Please "
											+ "contact GitPlex administrator to reset your password manually";
								}
							} else {
								feedback = hashAlgorithmChangeMessage 
										+ " The reset password can not be sent as mail setting is not defined. "
										+ "Please contact GitPlex administrator to reset your password manually";
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
				setVisible(GitPlex.getInstance(ConfigManager.class).getMailSetting() != null);
			}
			
		});

		boolean enableSelfRegister = GitPlex.getInstance(ConfigManager.class).getSecuritySetting().isEnableSelfRegister();
		add(new ViewStateAwarePageLink<Void>("registerUser", RegisterPage.class).setVisible(enableSelfRegister));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new LoginResourceReference()));
	}

}
