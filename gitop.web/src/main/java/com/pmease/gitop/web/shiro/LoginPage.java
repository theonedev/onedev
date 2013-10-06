package com.pmease.gitop.web.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.gitop.web.GitopSession;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.common.form.checkbox.CheckBoxElement;
import com.pmease.gitop.web.common.form.flatcheckbox.FlatCheckBoxElement;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.AccountHomePage;
import com.pmease.gitop.web.util.WicketUtils;

@SuppressWarnings("serial")
public class LoginPage extends AbstractLayoutPage {
	private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
	
	public LoginPage() {
		if (SecurityUtils.getSubject().isAuthenticated()) {
			throw new RestartResponseException(getApplication().getHomePage());
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new LoginForm("login"));
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
	}
	
	private class LoginForm extends StatelessForm<Void> {

		public LoginForm(String id) {
			super(id);
		}
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			add(new TextField<String>("username", new Model<String>()).setRequired(true));
			add(new PasswordTextField("password", new Model<String>()).setResetPassword(false));
			add(new FlatCheckBoxElement("rememberme", Model.of(false),
					Model.of("Remember me on this computer")));
		}
		
		private Component getUsernameField() {
			return get("username");
		}
		
		private Component getPasswordField() {
			return get("password");
		}
		
		/**
		 * Delegate to {@link #loginShiro loginShiro()} to peform the
		 * authentication; if it succeeds, redirect to the user's original intended
		 * destination or to the application home page.
		 */
		@Override
		protected void onSubmit() {
			String username = getUsernameField().getDefaultModelObjectAsString();
			String password = getPasswordField().getDefaultModelObjectAsString();

			// Convert username to lowercase just in case the backend authentication
			// system is case sensitive and the user accidentally typed in uppercase.
			if (username != null) {
				username = username.toLowerCase();
			}

			if (loginShiro(username, password, remember())) {
				continueToOriginalDestination();
				setResponsePage(AccountHomePage.class, WicketUtils.newPageParams(PageSpec.USER, username));
			}
		}

		
		protected boolean loginShiro(String loginName, String password, boolean remember) {
			try {
				GitopSession.get().login(loginName, password, remember);
				return true;
			} catch (AuthenticationException ae) {
				onAuthenticationException(ae);
			}
			return false;
		}

		/**
		 * Handle any exceptions that are thrown upon login failure by setting an
		 * appropriate feedback message. The default implemention adds an error
		 * feedback message with the key {@code loginFailed} to the email field.
		 */
		protected void onAuthenticationException(AuthenticationException ae) {
			logger.debug("Shiro Subject.login() failed", ae);
			getUsernameField().error(getString("loginFailed", null, "Invalid user name and/or password."));
		}

		/**
		 * Override this method to return {@code true} if you want to enable Shiro's
		 * "remember me" feature. By default this returns {@code false}.
		 */
		protected boolean remember() {
			CheckBoxElement c = (CheckBoxElement) get("rememberme");
			Boolean b = (Boolean) c.getFormComponent().getDefaultModelObject();
			System.out.println(b);
			return b;
		}
	}


	@Override
	protected String getPageTitle() {
		return "Gitop - Log in";
	}
}
