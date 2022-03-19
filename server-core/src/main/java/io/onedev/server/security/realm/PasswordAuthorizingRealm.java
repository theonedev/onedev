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
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.authenticator.Authenticated;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;

@Singleton
public class PasswordAuthorizingRealm extends AbstractAuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(PasswordAuthorizingRealm.class);
	
    private final TransactionManager transactionManager;
    
    private final MembershipManager membershipManager;
    
    private final SshKeyManager sshKeyManager;
    
	@Inject
    public PasswordAuthorizingRealm(UserManager userManager, SettingManager settingManager, 
    		MembershipManager membershipManager, GroupManager groupManager, 
    		ProjectManager projectManager, SessionManager sessionManager, 
    		TransactionManager transactionManager, SshKeyManager sshKeyManager, 
    		PasswordService passwordService, EmailAddressManager emailAddressManager) {
		super(userManager, groupManager, projectManager, sessionManager, 
				settingManager, emailAddressManager);
		
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(passwordService);
		setCredentialsMatcher(passwordMatcher);
		
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
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		userManager.save(user);
		
		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setValue(authenticated.getEmail());
		emailAddress.setVerificationCode(null);
		emailAddress.setOwner(user);
		emailAddress.setPrimary(true);
		emailAddress.setGit(true);
		emailAddressManager.save(emailAddress);
		
		user.getEmailAddresses().add(emailAddress);

		Collection<String> groupNames = authenticated.getGroupNames();
		if (groupNames == null && defaultGroup != null)
			groupNames = Sets.newHashSet(defaultGroup);
		if (groupNames != null) 
			membershipManager.syncMemberships(user, groupNames);
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
    	return user;
	}
	
	private void updateUser(User user, Authenticated authenticated, @Nullable EmailAddress emailAddress) {
		if (emailAddress != null) {
			emailAddress.setVerificationCode(null);
			emailAddressManager.setAsPrimary(emailAddress);
		} else {
			emailAddress = new EmailAddress();
			emailAddress.setValue(authenticated.getEmail());
			emailAddress.setVerificationCode(null);
			emailAddress.setOwner(user);
			emailAddress.setPrimary(true);
			emailAddress.setGit(true);
			emailAddressManager.save(emailAddress);
		}
		user.setSsoConnector(null);
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
					String userNameOrEmailAddressValue = (String) token.getPrincipal();
					User user;
					EmailAddress emailAddress = emailAddressManager.findByValue(userNameOrEmailAddressValue);
					if (emailAddress != null)
						user = emailAddress.getOwner();
					else 
						user = userManager.findByName(userNameOrEmailAddressValue);
					if (user != null) {
						if (user.isExternalManaged()) {
					    	Authenticator authenticator = settingManager.getAuthenticator();
			    			if (authenticator != null) {
			    				UsernamePasswordToken authToken = (UsernamePasswordToken) token;
			    				authToken = new UsernamePasswordToken(user.getName(), authToken.getPassword(), 
			    						authToken.isRememberMe(), authToken.getHost());
			    				Authenticated authenticated = authenticator.authenticate(authToken);
			    				String emailAddressValue = authenticated.getEmail();
			    				emailAddress = emailAddressManager.findByValue(emailAddressValue);
			    				if (emailAddress != null && !emailAddress.getOwner().equals(user)) {
			    					throw new AuthenticationException("Email address '" + emailAddressValue 
			    							+ "' has already been used by another user");
			    				} else {
				        			updateUser(user, authenticated, emailAddress);
				        			return user;
			    				}
			    			} else {
			    				throw new AuthenticationException("No external authenticator to authenticate user '" 
			    						+ userNameOrEmailAddressValue + "'");
			    			}
						} else {
							return user;
						}
					} else if (emailAddress == null) {
				    	Authenticator authenticator = settingManager.getAuthenticator();
		    			if (authenticator != null) {
		    				Authenticated authenticated = authenticator.authenticate((UsernamePasswordToken) token);
		    				String emailAddressValue = authenticated.getEmail();
		    				if (emailAddressManager.findByValue(emailAddressValue) != null) {
		    					throw new AuthenticationException("Email address '" + emailAddressValue
		    							+ "' has already been used by another user");
		    				} else {
		    					return newUser(userNameOrEmailAddressValue, authenticated, authenticator.getDefaultGroup());
		    				}
		    			} else {
		    	            throw new UnknownAccountException("Unknown user");
		    			}
			    	} else {
	    	            throw new UnknownAccountException("Unknown user");
			    	}
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
