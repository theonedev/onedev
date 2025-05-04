package io.onedev.server.web.page.simple.security;

import static io.onedev.server.model.User.PROP_NOTIFY_OWN_EVENTS;
import static io.onedev.server.model.User.PROP_SERVICE_ACCOUNT;
import static io.onedev.server.web.page.simple.security.SignUpBean.PROP_EMAIL_ADDRESS;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Group;
import io.onedev.server.model.Membership;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.HomePage;
import io.onedev.server.web.page.simple.SimplePage;

public class SignUpPage extends SimplePage {
	
	public SignUpPage(PageParameters params) {
		super(params);
		
		if (!getSecuritySetting().isEnableSelfRegister())
			throw new UnauthenticatedException("User sign-up is disabled");
		if (getLoginUser() != null)
			throw new IllegalStateException("Can not sign up a user while signed in");
	}
	
	private SecuritySetting getSecuritySetting() {
		return OneDev.getInstance(SettingManager.class).getSecuritySetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		SignUpBean bean = new SignUpBean();
		BeanEditor editor = BeanContext.edit("editor", bean, Sets.newHashSet(PROP_SERVICE_ACCOUNT, PROP_NOTIFY_OWN_EVENTS), true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User userWithSameName = getUserManager().findByName(bean.getName());
				if (userWithSameName != null) {
					editor.error(new Path(new PathNode.Named(User.PROP_NAME)),
							"Login name already used by another account");
				} 

				var invalidEmailAddress = false;
				if (getSecuritySetting().getAllowedSelfRegisterEmailDomain() != null) {
					var emailDomain = StringUtils.substringAfter(bean.getEmailAddress(), "@").toLowerCase();
					var patternSet = PatternSet.parse(getSecuritySetting().getAllowedSelfRegisterEmailDomain().toLowerCase());
					if (!patternSet.matches(new StringMatcher(), emailDomain)) {
						editor.error(new Path(new PathNode.Named(PROP_EMAIL_ADDRESS)),
								"This email domain is not accepted for self sign-up");
						invalidEmailAddress = true;
					}
				}
				if (!invalidEmailAddress && getEmailAddressManager().findByValue(bean.getEmailAddress()) != null) {
					editor.error(new Path(new PathNode.Named(PROP_EMAIL_ADDRESS)),
							"Email address already used by another user");
				} 
				if (editor.isValid()) {
					User user = new User();
					user.setName(bean.getName());
					user.setFullName(bean.getFullName());
					user.setPassword(getPasswordService().encryptPassword(bean.getPassword()));
					
					EmailAddress emailAddress = new EmailAddress();
					emailAddress.setValue(bean.getEmailAddress());
					emailAddress.setOwner(user);

					var defaultLoginGroup = getSettingManager().getSecuritySetting().getDefaultGroup();
					
					getTransactionManager().run(() -> {
						getUserManager().create(user);
						getEmailAddressManager().create(emailAddress);
						if (defaultLoginGroup != null) 
							createMembership(user, defaultLoginGroup);
					});
					
					Session.get().success("Account sign up successfully");
					SecurityUtils.getSubject().runAs(user.getPrincipals());
					setResponsePage(HomePage.class);
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				throw new RestartResponseException(HomePage.class);
			}
			
		});
		add(form);
	}

	private PasswordService getPasswordService() {
		return OneDev.getInstance(PasswordService.class);
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

	private TransactionManager getTransactionManager() {
		return OneDev.getInstance(TransactionManager.class);
	}

	private MembershipManager getMembershipManager() {
		return OneDev.getInstance(MembershipManager.class);
	}
	
	private void createMembership(User user, Group group) {
		var membership = new Membership();
		membership.setUser(user);
		membership.setGroup(group);
		user.getMemberships().add(membership);
		getMembershipManager().create(membership);
	}

	@Override
	protected String getTitle() {
		return "Sign Up";
	}

	@Override
	protected String getSubTitle() {
		return "Enter your details to create your account";
	}

}
