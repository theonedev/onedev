package io.onedev.server.security.realm;

import java.util.Collection;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;

import com.google.common.collect.Sets;

import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.model.support.SsoInfo;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;

@Singleton
public class SsoAuthorizingRealm extends AbstractAuthorizingRealm {

    private final MembershipManager membershipManager;
    
    private final TransactionManager transactionManager;
    
    private final SshKeyManager sshKeyManager;
    
	@Inject
    public SsoAuthorizingRealm(UserManager userManager, MembershipManager membershipManager, 
    		GroupManager groupManager, ProjectManager projectManager, SessionManager sessionManager, 
    		TransactionManager transactionManager, SshKeyManager sshKeyManager) {
		super(userManager, groupManager, projectManager, sessionManager);
		setCredentialsMatcher(new AllowAllCredentialsMatcher());
		
    	this.membershipManager = membershipManager;
    	this.transactionManager = transactionManager;
    	this.sshKeyManager = sshKeyManager;
    }

	private User newUser(SsoAuthenticated authenticated) {
		User user = new User();
		user.setName(authenticated.getUserName());
		user.setPassword(User.EXTERNAL_MANAGED);
		user.setEmail(authenticated.getEmail());
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		user.setSsoInfo(authenticated.getSsoInfo());
		
		userManager.save(user);

		Collection<String> groupNames = authenticated.getGroupNames();
		if (groupNames == null && authenticated.getConnector().getDefaultGroup() != null)
			groupNames = Sets.newHashSet(authenticated.getConnector().getDefaultGroup());
		if (groupNames != null) 
			membershipManager.syncMemberships(user, groupNames);
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
    	return user;
	}
	
	private void updateUser(User user, SsoAuthenticated authenticated) {
		user.setName(authenticated.getUserName());
		user.setEmail(authenticated.getEmail());
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		userManager.save(user);
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
		return transactionManager.call(new Callable<AuthenticationInfo>() {

			@Override
			public AuthenticationInfo call() {
				User user;
				SsoAuthenticated authenticated = (SsoAuthenticated) token;
				String userName = authenticated.getUserName();
				String email = authenticated.getEmail();
				SsoInfo ssoInfo = authenticated.getSsoInfo();
		    	user = userManager.findBySsoInfo(ssoInfo);
		    	if (user == null) {
		    		if (userManager.findByName(userName) != null)
		    			throw new AuthenticationException("Account '" + userName + "' already exists");
		    		if (userManager.findByEmail(email) != null)
		    			throw new AuthenticationException("Email '" + email + "' has already been used by another account");
	    			user = newUser(authenticated);
		    	} else {
		    		if (!userName.equals(user.getName()) && userManager.findByName(userName) != null)
		    			throw new AuthenticationException("Account '" + userName + "' already exists");
		    		if (!email.equals(user.getEmail()) && userManager.findByEmail(email) != null)
		    			throw new AuthenticationException("Email '" + email + "' has already been used by another account");
	    			updateUser(user, authenticated);
		    	}				
		    	return user;
			}
			
		});
	}
}
