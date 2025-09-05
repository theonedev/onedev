package io.onedev.server.web.page.security;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.oauth2.sdk.ParseException;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.SsoAccountManager;
import io.onedev.server.entitymanager.SsoProviderManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.SsoAccount;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.realm.PasswordAuthenticatingRealm;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.component.tabbable.ActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.HomePage;
import io.onedev.server.web.page.simple.SimplePage;

public class SsoProcessPage extends SimplePage {

	public static final String MOUNT_PATH = "~sso";
		
	public static final String STAGE_INITIATE = "initiate";
	
	public static final String STAGE_CALLBACK = "callback";
	
	private static final String PARAM_PROVIDER = "provider";
	
	private static final String PARAM_STAGE = "stage";
	
	private static final String SESSION_ATTR_REDIRECT_URL = "redirectUrl";

	private static final Logger logger = LoggerFactory.getLogger(SsoProcessPage.class);

	private final IModel<SsoProvider> providerModel;
	
	private final String stage;

	private final SsoAuthenticated authenticated;
	
	public SsoProcessPage(PageParameters params) {
		super(params);
		
		stage = params.get(PARAM_STAGE).toString();
		
		try {
			String providerName = params.get(PARAM_PROVIDER).toString();

			providerModel = new LoadableDetachableModel<SsoProvider>() {
				@Override
				protected SsoProvider load() {
					return OneDev.getInstance(SsoProviderManager.class).find(providerName);
				}
			};
			
			if (getProvider() == null) 
				throw new AuthenticationException(_T("Unable to find SSO provider: ") + providerName);
			
			if (stage.equals(STAGE_INITIATE)) {
				String redirectUrlAfterLogin;
				Url url = RestartResponseAtInterceptPageException.getOriginalUrl();
				if (url != null && url.toString().length() != 0) 
					redirectUrlAfterLogin = url.toString();
				else 
					redirectUrlAfterLogin = RequestCycle.get().urlFor(HomePage.class, new PageParameters()).toString(); 
				Session.get().bind();
				Session.get().setAttribute(SESSION_ATTR_REDIRECT_URL, redirectUrlAfterLogin);
				throw new RedirectToUrlException(getProvider().getConnector().buildAuthUrl(providerName));
			} else {
				authenticated = getProvider().getConnector().handleAuthResponse(providerName);
				var aUser = getTransactionManager().call(() -> {					
					var ssoAccount = getSsoAccountManager().find(getProvider(), authenticated.getSubject());
					if (ssoAccount != null) {
						var user = ssoAccount.getUser();
						if (user.isServiceAccount() || user.isDisabled()) {
							getSsoAccountManager().delete(ssoAccount);
						} else {
							if (authenticated.getEmail() != null) {
								var emailAddress = getEmailAddressManager().findByValue(authenticated.getEmail());
								if (emailAddress == null) {
									emailAddress = new EmailAddress();
									emailAddress.setValue(authenticated.getEmail());
									emailAddress.setVerificationCode(null);
									user.addEmailAddress(emailAddress);
									getEmailAddressManager().create(emailAddress);
								} else if (emailAddress.getOwner().equals(user)) {
									emailAddress.setVerificationCode(null);
									getEmailAddressManager().update(emailAddress);
								} else if (!emailAddress.isVerified()) {
									emailAddress.setVerificationCode(null);
									user.addEmailAddress(emailAddress);
									getEmailAddressManager().update(emailAddress);
								} else {
									throw new AuthenticationException(MessageFormat.format(_T("Email address \"{0}\" used by account \"{1}\""), authenticated.getEmail(), user.getName()));
								}
							}
							syncGroupsAndSshKeys(user, false);
							return user;
						}
					} 
					if (authenticated.getEmail() != null) {
						var emailAddress = getEmailAddressManager().findByValue(authenticated.getEmail());
						if (emailAddress != null) {
							var user = emailAddress.getOwner();
							if (emailAddress.isVerified()) {
								if (user.isServiceAccount()) {
									getEmailAddressManager().delete(emailAddress);
								} else if (user.isDisabled()) {
									throw new AuthenticationException(MessageFormat.format(_T("Email address \"{0}\" used by disabled account \"{1}\""), authenticated.getEmail(), user.getName()));
								} else {
									ssoAccount = new SsoAccount();
									ssoAccount.setUser(user);
									ssoAccount.setProvider(getProvider());
									ssoAccount.setSubject(authenticated.getSubject());
									getSsoAccountManager().create(ssoAccount);

									syncGroupsAndSshKeys(user, false);
									return user;
								}
							} else {
								getEmailAddressManager().delete(emailAddress);
							}
						} 
					}
					return null;
				});
				if (aUser != null) 
					afterLogin(aUser);
			}
		} catch (AuthenticationException e) {
			throw new RestartResponseException(new LoginPage(e.getMessage()));
		} catch (Exception e) {
			var parseException = ExceptionUtils.find(e, ParseException.class);
			if (parseException != null) {
				// This will happen if user refreshes the page and some Idp return a response
				// with 200 status code but indicate errors in response body, which will 
				// result in ParseException. In this case, we logger the error and simply 
				// restart the login process
				logger.error("Error parsing OIDC response", e);
				throw new RestartResponseException(LoginPage.class);
			} else {
				throw e;
			}
		}
	}	

	private void syncGroupsAndSshKeys(User user, boolean forNewUser) {
		var groupNames = authenticated.getGroupNames();
		if (forNewUser && groupNames == null) 
			groupNames = new HashSet<String>();
		if (groupNames != null) {
			if (getProvider().getDefaultGroup() != null)
				groupNames.add(getProvider().getDefaultGroup().getName());
			if (getSettingManager().getSecuritySetting().getDefaultGroupName() != null)
				groupNames.add(getSettingManager().getSecuritySetting().getDefaultGroupName());
			getMembershipManager().syncMemberships(user, groupNames);
		}
		
		if (authenticated.getSshKeys() != null)
			getSshKeyManager().syncSshKeys(user, authenticated.getSshKeys());									
	}

	private void afterLogin(User user) {		
		String redirectUrlAfterLogin = (String) Session.get().getAttribute(SESSION_ATTR_REDIRECT_URL);
		if (StringUtils.isBlank(redirectUrlAfterLogin))
			throw new AuthenticationException(_T("Unsolicited OIDC authentication response"));

		SecurityUtils.getSubject().runAs(user.getPrincipals());

		throw new RedirectToUrlException(redirectUrlAfterLogin);	
	}

	private SsoProvider getProvider() {
		return providerModel.getObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Tab> tabs = new ArrayList<>();
					
		var createNewUserTab = new ActionTab(Model.of(_T("Create New User"))) {
			@Override
			protected void onSelect(Component tabLink) {
				var createNewUserPanel = newCreateNewUserPanel("tabContent");
				getPage().replace(createNewUserPanel);
			}
		};
		tabs.add(createNewUserTab);

		var linkExistingUserTab = new ActionTab(Model.of(_T("Link Existing User"))) {
			@Override
			protected void onSelect(Component tabLink) {
				var linkExistingUserPanel = newLinkExistingUserPanel("tabContent");
				getPage().replace(linkExistingUserPanel);
			}
		};
		tabs.add(linkExistingUserTab);

		add(new Tabbable("tabs", tabs));
		add(newCreateNewUserPanel("tabContent"));
	}

	private Component newCreateNewUserPanel(String componentId) {
		var bean = new SignUpBean();
		bean.setName(authenticated.getUserName());
		bean.setFullName(authenticated.getFullName());
		var excludedProperties = new HashSet<String>();
		excludedProperties.add(User.PROP_SERVICE_ACCOUNT);
		excludedProperties.add(User.PROP_NOTIFY_OWN_EVENTS);
		excludedProperties.add(User.PROP_PASSWORD);
		if (authenticated.getEmail() != null)
			excludedProperties.add(SignUpBean.PROP_EMAIL_ADDRESS);
		var editor = BeanContext.edit("editor", bean, excludedProperties, true);
		var form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				User userWithSameName = getUserManager().findByName(bean.getName());
				if (userWithSameName != null) {
					editor.error(new Path(new PathNode.Named(User.PROP_NAME)),
							_T("Login name already used by another account"));
				} 

				if (authenticated.getEmail() == null && getEmailAddressManager().findByValue(bean.getEmailAddress()) != null) {
					editor.error(new Path(new PathNode.Named(SignUpBean.PROP_EMAIL_ADDRESS)),
							_T("Email address already used by another user"));
				}
				if (editor.isValid()) {					
					var aUser = getTransactionManager().call(() -> {
						User user = new User();
						user.setName(bean.getName());
						user.setFullName(bean.getFullName());
						
						EmailAddress emailAddress = new EmailAddress();
						if (authenticated.getEmail() != null) {
							emailAddress.setValue(authenticated.getEmail());
							emailAddress.setVerificationCode(null);
						} else {
							emailAddress.setValue(bean.getEmailAddress());
						}
						emailAddress.setPrimary(true);
						emailAddress.setGit(true);
						emailAddress.setOwner(user);

						var ssoAccount = new SsoAccount();
						ssoAccount.setUser(user);
						ssoAccount.setProvider(getProvider());
						ssoAccount.setSubject(authenticated.getSubject());
	
						getUserManager().create(user);
						getEmailAddressManager().create(emailAddress);
						getSsoAccountManager().create(ssoAccount);
						
						syncGroupsAndSshKeys(user, true);
						return user;
					});
					afterLogin(aUser);
				}

			}
		};
		form.add(editor);
		form.add(new Link<Void>("cancel") {
			@Override
			public void onClick() {
				setResponsePage(LoginPage.class);
			}
		});

		var fragment = new Fragment(componentId, "tabContentFrag", this);
		fragment.add(form);
		return fragment;
	}

	private Component newLinkExistingUserPanel(String componentId) {
		var bean = new LinkUserBean();
		var editor = BeanContext.edit("editor", bean);
		var form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();

				try {
					var aUser = getTransactionManager().call(() -> {
						var token = new UsernamePasswordToken(bean.getUserName(), bean.getPassword(), false);
						var user = (User) getPasswordAuthenticatingRealm().getAuthenticationInfo(token);
						if (authenticated.getEmail() != null) {
							var emailAddress = new EmailAddress();
							emailAddress.setValue(authenticated.getEmail());
							emailAddress.setVerificationCode(null);
							user.addEmailAddress(emailAddress);
							getEmailAddressManager().create(emailAddress);							
						}
						var ssoAccount = new SsoAccount();
						ssoAccount.setUser(user);
						ssoAccount.setProvider(getProvider());
						ssoAccount.setSubject(authenticated.getSubject());
						getSsoAccountManager().create(ssoAccount);

						syncGroupsAndSshKeys(user, true);
						return user;
					});
					afterLogin(aUser);
				} catch (AuthenticationException e) {
					error(e.getMessage());
				}
			}
		};
		form.add(editor);

		form.add(new Link<Void>("cancel") {
			@Override
			public void onClick() {
				setResponsePage(LoginPage.class);
			}
		});

		var fragment = new Fragment(componentId, "tabContentFrag", this);
		fragment.add(form);
		return fragment;
	}

	@Override
	protected void onDetach() {
		providerModel.detach();
		super.onDetach();
	}
			
	public static PageParameters paramsOf(String stage, String providerName) {
		PageParameters params = new PageParameters();
		params.add(PARAM_STAGE, stage);
		params.add(PARAM_PROVIDER, providerName);
		return params;
	}

	@Override
	protected String getTitle() {
		return _T("Single Sign-On");
	}

	@Override
	protected String getSubTitle() {
		return _T("Connect with your SSO account");
	}
	
	private TransactionManager getTransactionManager() {
		return OneDev.getInstance(TransactionManager.class);
	}

	private SsoAccountManager getSsoAccountManager() {
		return OneDev.getInstance(SsoAccountManager.class);
	}

	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}

	private EmailAddressManager getEmailAddressManager() {
		return OneDev.getInstance(EmailAddressManager.class);
	}

	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}

	private MembershipManager getMembershipManager() {
		return OneDev.getInstance(MembershipManager.class);
	}

	private SshKeyManager getSshKeyManager() {
		return OneDev.getInstance(SshKeyManager.class);
	}

	private PasswordAuthenticatingRealm getPasswordAuthenticatingRealm() {
		return OneDev.getInstance(PasswordAuthenticatingRealm.class);
	}
}
