package com.pmease.gitplex.web.page.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.web.WebSession;
import com.pmease.gitplex.web.page.base.BasePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class LoginPage extends BasePage {

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
				
				try {
					WebSession.get().login(userName, password, rememberMe);
					continueToOriginalDestination();
					setResponsePage(getApplication().getHomePage());
				} catch (AuthenticationException ae) {
					error("Invalid user name and/or password.");
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
		
		add(new BookmarkablePageLink<Void>("forgetPassword", ForgetPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(GitPlex.getInstance(ConfigManager.class).getMailSetting() != null);
			}
			
		});

		boolean enableSelfRegister = GitPlex.getInstance(ConfigManager.class).getSecuritySetting().isEnableSelfRegister();
		add(new BookmarkablePageLink<Void>("registerAccount", RegisterPage.class).setVisible(enableSelfRegister));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new LoginResourceReference()));
	}

}
