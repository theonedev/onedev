package io.onedev.server.security.realm;

import com.google.common.collect.Sets;
import io.onedev.server.entitymanager.*;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.concurrent.Callable;

@Singleton
public class SsoAuthorizingRealm extends AbstractAuthorizingRealm {

    private final MembershipManager membershipManager;
    
    private final TransactionManager transactionManager;
    
    private final SshKeyManager sshKeyManager;
    
    private final EmailAddressManager emailAddressManager;
    
	@Inject
    public SsoAuthorizingRealm(UserManager userManager, MembershipManager membershipManager, 
    		GroupManager groupManager, ProjectManager projectManager, SessionManager sessionManager, 
    		TransactionManager transactionManager, SshKeyManager sshKeyManager, 
    		SettingManager settingManager, EmailAddressManager emailAddressManager) {
		super(userManager, groupManager, projectManager, sessionManager, settingManager);
		setCredentialsMatcher(new AllowAllCredentialsMatcher());
		
    	this.membershipManager = membershipManager;
    	this.transactionManager = transactionManager;
    	this.sshKeyManager = sshKeyManager;
    	this.emailAddressManager = emailAddressManager;
    }

	private User newUser(SsoAuthenticated authenticated) {
		User user = new User();
		user.setName(authenticated.getUserName());
		user.setGuest(authenticated.getConnector().isCreateUserAsGuest());
		user.setSsoConnector(authenticated.getConnector().getName());
		user.setPassword(User.EXTERNAL_MANAGED);
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
		if (groupNames != null) 
			membershipManager.syncMemberships(user, groupNames);
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
    	return user;
	}
	
	private void updateUser(EmailAddress emailAddress, SsoAuthenticated authenticated) {
		User user = emailAddress.getOwner();
		user.setName(authenticated.getUserName());
		user.setSsoConnector(authenticated.getConnector().getName());
		user.setPassword(User.EXTERNAL_MANAGED);
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		userManager.update(user, null);
		
		emailAddress.setVerificationCode(null);
		emailAddressManager.setAsPrimary(emailAddress);
		
		if (authenticated.getGroupNames() != null)
			membershipManager.syncMemberships(user, authenticated.getGroupNames());
		
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
			EmailAddress emailAddress;
			SsoAuthenticated authenticated = (SsoAuthenticated) token;
			String userName = authenticated.getUserName();
			String emailAddressValue = authenticated.getEmail();
			emailAddress = emailAddressManager.findByValue(emailAddressValue);
			if (emailAddress == null) {
				if (userManager.findByName(userName) != null)
					throw new AuthenticationException("Login name '" + userName + "' already used by another user");
				else
					return newUser(authenticated);
			} else if (!userName.equalsIgnoreCase(emailAddress.getOwner().getName())
					&& userManager.findByName(userName) != null) {
				throw new AuthenticationException("Login name '" + userName + "' already used by another user");
			} else {
				updateUser(emailAddress, authenticated);
				return emailAddress.getOwner();
			}
		});
	}
}
