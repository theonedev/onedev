package io.onedev.server.web.page.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LoginPage extends BasePage {

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
				try {
					WebSession.get().login(userName, password, rememberMe);
					continueToOriginalDestination();
					setResponsePage(getApplication().getHomePage());
				} catch (IncorrectCredentialsException e) {
					error("Incorrect credentials");
				} catch (UnknownAccountException e) {
					error("Unknown user name");
				} catch (AuthenticationException ae) {
					error(ae.getMessage());
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
