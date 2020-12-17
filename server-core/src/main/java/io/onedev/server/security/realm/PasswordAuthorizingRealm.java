package io.onedev.server.security.realm;

import java.util.Collection;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.authenticator.Authenticated;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;

@Singleton
public class PasswordAuthorizingRealm extends AbstractAuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(PasswordAuthorizingRealm.class);
	
    private final SettingManager settingManager;
    
    private final TransactionManager transactionManager;
    
    private final MembershipManager membershipManager;
    
    private final SshKeyManager sshKeyManager;
    
	@Inject
    public PasswordAuthorizingRealm(UserManager userManager, SettingManager settingManager, 
    		MembershipManager membershipManager, GroupManager groupManager, 
    		ProjectManager projectManager, SessionManager sessionManager, 
    		TransactionManager transactionManager, SshKeyManager sshKeyManager, 
    		PasswordService passwordService) {
		super(userManager, groupManager, projectManager, sessionManager);
		
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(passwordService);
		setCredentialsMatcher(passwordMatcher);
		
    	this.settingManager = settingManager;
    	this.transactionManager = transactionManager;
    	this.membershipManager = membershipManager;
    	this.sshKeyManager = sshKeyManager;
    }

	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}

	private User newUser(String userName, Authenticated authenticated, @Nullable String defaultGroup) {
		User user = new User();
		user.setName(userName);
		user.setPassword(User.EXTERNAL_MANAGED);
		user.setEmail(authenticated.getEmail());
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		
		userManager.save(user);

		Collection<String> groupNames = authenticated.getGroupNames();
		if (groupNames == null && defaultGroup != null)
			groupNames = Sets.newHashSet(defaultGroup);
		if (groupNames != null) 
			membershipManager.syncMemberships(user, groupNames);
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
    	return user;
	}
	
	private void updateUser(User user, Authenticated authenticated) {
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
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) 
			throws AuthenticationException {
		return transactionManager.call(new Callable<AuthenticationInfo>() {

			@Override
			public AuthenticationInfo call() {
				try {
					String userName = (String) token.getPrincipal();
					User user = userManager.findByName(userName);
			    	if (user == null) {
				    	Authenticator authenticator = settingManager.getAuthenticator();
		    			if (authenticator != null) {
		    				Authenticated authenticated = authenticator.authenticate((UsernamePasswordToken) token);
		    				String email = authenticated.getEmail();
		    				if (userManager.findByEmail(email) != null) {
		    					throw new AuthenticationException("Email '" + email 
		    							+ "' has already been used by another account");
		    				}
		        			user = newUser(userName, authenticated, authenticator.getDefaultGroup());
		    			} else {
		    	            throw new UnknownAccountException("Unable to find account data for token [" + token + "] in realm [" + this + "]");
		    			}
			    	} else if (user.getPassword().equals(User.EXTERNAL_MANAGED)) {
		    			if (user.getSsoInfo().getConnector() != null) {
		    				throw new AuthenticationException("Account '" + userName 
		    						+ "' is set to authenticate via " + User.AUTH_SOURCE_SSO_PROVIDER 
		    						+ user.getSsoInfo().getConnector());
		    			}
				    	Authenticator authenticator = settingManager.getAuthenticator();
		    			if (authenticator != null) {
		    				Authenticated authenticated = authenticator.authenticate((UsernamePasswordToken) token);
		    				String email = authenticated.getEmail();
		    				if (!email.equals(user.getEmail()) && userManager.findByEmail(email) != null) { 
		    					throw new AuthenticationException("Email '" + email 
		    							+ "' has already been used by another account");
		    				}
		        			updateUser(user, authenticated);
		    			} else {
		    				throw new AuthenticationException("Account '" + userName + "' is set to authenticate "
		    						+ "externally but " + User.AUTH_SOURCE_EXTERNAL_AUTHENTICATOR + " is not defined");
		    			}
			    	} 				
			    	return user;
				} catch (Exception e) {
	    			if (e instanceof AuthenticationException) {
	    				logger.debug("Authentication not passed", e);
	        			throw ExceptionUtils.unchecked(e);
	    			} else {
	    				logger.error("Error authenticating user", e);
	        			throw new AuthenticationException("Error authenticating user", e);
	    			}
				}
			}
		});
	}
}
