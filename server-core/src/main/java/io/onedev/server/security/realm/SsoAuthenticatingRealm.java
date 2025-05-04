package io.onedev.server.security.realm;

import static io.onedev.server.validation.validator.UserNameValidator.suggestUserName;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;

import com.google.common.collect.Sets;

import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;

@Singleton
public class SsoAuthenticatingRealm extends AuthenticatingRealm {

	private final UserManager userManager;
	
    private final MembershipManager membershipManager;
    
    private final TransactionManager transactionManager;
    
    private final SshKeyManager sshKeyManager;
    
    private final EmailAddressManager emailAddressManager;

	private final SettingManager settingManager;
    
	@Inject
    public SsoAuthenticatingRealm(UserManager userManager, MembershipManager membershipManager,
								  GroupManager groupManager, ProjectManager projectManager, SessionManager sessionManager,
								  TransactionManager transactionManager, SshKeyManager sshKeyManager,
								  SettingManager settingManager, EmailAddressManager emailAddressManager) {
		setCredentialsMatcher(new AllowAllCredentialsMatcher());
		this.userManager = userManager;
    	this.membershipManager = membershipManager;
    	this.transactionManager = transactionManager;
    	this.sshKeyManager = sshKeyManager;
    	this.emailAddressManager = emailAddressManager;
		this.settingManager = settingManager;
    }

	private User newUser(SsoAuthenticated authenticated) {
		User user = new User();
		user.setName(suggestUserName(authenticated.getUserName()));
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		userManager.create(user);

		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setOwner(user);
		emailAddress.setVerificationCode(null);
		emailAddress.setValue(authenticated.getEmail());
		emailAddress.setPrimary(true);
		emailAddress.setGit(true);
		emailAddressManager.create(emailAddress);
		
		user.getEmailAddresses().add(emailAddress);
		
		Collection<String> groupNames = authenticated.getGroupNames();
		if (groupNames == null && authenticated.getConnector().getDefaultGroup() != null)
			groupNames = Sets.newHashSet(authenticated.getConnector().getDefaultGroup());

		var defaultLoginGroupName = settingManager.getSecuritySetting().getDefaultGroupName();
		if (defaultLoginGroupName != null) {
			if (groupNames == null)
				groupNames = new HashSet<>();
			groupNames.add(defaultLoginGroupName);
		}
	
		if (groupNames != null) 
			membershipManager.syncMemberships(user, groupNames);
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
    	return user;
	}
	
	private void updateUser(EmailAddress emailAddress, SsoAuthenticated authenticated) {
		User user = emailAddress.getOwner();
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		userManager.update(user, null);

		emailAddressManager.setAsPrimary(emailAddress);

		var groupNames = authenticated.getGroupNames();
		if (groupNames != null) {
			var defaultLoginGroupName = settingManager.getSecuritySetting().getDefaultGroupName();
			if (defaultLoginGroupName != null)
				groupNames.add(defaultLoginGroupName);
			membershipManager.syncMemberships(user, groupNames);
		}
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
	}
	
	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof SsoAuthenticated;
	}
	
	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) 
			throws AuthenticationException {
		return transactionManager.call((Callable<AuthenticationInfo>) () -> {
			var authenticated = (SsoAuthenticated) token;
			var emailAddressValue = authenticated.getEmail();
			var emailAddress = emailAddressManager.findByValue(emailAddressValue);
			if (emailAddress != null) {
				if (emailAddress.isVerified()) {
					var user = emailAddress.getOwner();
					if (user.isDisabled())
						throw new AuthenticationException("User is disabled");
					updateUser(emailAddress, authenticated);
					return user;
				} else {
					emailAddressManager.delete(emailAddress);
					return newUser(authenticated);
				}
			} else {
				return newUser(authenticated);
			}
		});
	}
}
