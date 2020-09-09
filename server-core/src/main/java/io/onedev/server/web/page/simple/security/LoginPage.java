package io.onedev.server.web.page.simple.security;

import static io.onedev.server.web.page.admin.sso.SsoProcessPage.MOUNT_PATH;
import static io.onedev.server.web.page.admin.sso.SsoProcessPage.STAGE_INITIATE;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.simple.SimpleCssResourceReference;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class LoginPage extends SimplePage {

	private String userName;
	
	private String password;

	private boolean rememberMe;
	
	private String errorMessage;
	
	public LoginPage(PageParameters params) {
		super(params);
		
		if (SecurityUtils.getSubject().isAuthenticated())
			throw new RestartResponseException(getApplication().getHomePage());
	}
	
	public LoginPage(String errorMessage) {
		super(new PageParameters());
		this.errorMessage = errorMessage;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		StatelessForm<?> form = new StatelessForm<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				try {
					WebSession.get().login(new UsernamePasswordToken(userName, password, rememberMe));
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
		
		form.add(new FencedFeedbackPanel("feedback"));
		
		if (errorMessage != null) 
			form.error(errorMessage);
		
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
			
		}).setLabel(Model.of("User name")).setRequired(true));
		
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
			
		}).setLabel(Model.of("Password")).setRequired(true));
		
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
		
		form.add(new ViewStateAwarePageLink<Void>("forgetPassword", PasswordResetPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(OneDev.getInstance(SettingManager.class).getMailSetting() != null);
			}
			
		});

		add(form);
		
		SettingManager settingManager = OneDev.getInstance(SettingManager.class);
		
		boolean enableSelfRegister = settingManager.getSecuritySetting().isEnableSelfRegister();
		add(new ViewStateAwarePageLink<Void>("registerUser", SignUpPage.class).setVisible(enableSelfRegister));

		String serverUrl = settingManager.getSystemSetting().getServerUrl();
		
		List<SsoConnector> ssoConnectors = settingManager.getSsoConnectors();
		RepeatingView ssoButtonsView = new RepeatingView("ssoButtons");
		for (SsoConnector connector: ssoConnectors) {
			ExternalLink ssoButton = new ExternalLink(ssoButtonsView.newChildId(), 
					Model.of(serverUrl + "/" + MOUNT_PATH + "/" + STAGE_INITIATE + "/" + connector.getName()));
			ssoButton.add(new ExternalImage("image", connector.getButtonImageUrl()));
			ssoButton.add(new Label("label", "Login with " + connector.getName()));
			ssoButtonsView.add(ssoButton);
		}
		add(ssoButtonsView.setVisible(!ssoConnectors.isEmpty()));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SimpleCssResourceReference()));
	}

	@Override
	protected String getTitle() {
		return "Sign In To OneDev";
	}

	@Override
	protected String getSubTitle() {
		return "Enter your details to login to your account";
	}
	
}
