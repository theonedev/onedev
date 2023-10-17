package io.onedev.server.security.realm;

import com.google.common.collect.Sets;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.*;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.authenticator.Authenticated;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.validation.validator.UserNameValidator;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class PasswordAuthorizingRealm extends AbstractAuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(PasswordAuthorizingRealm.class);
	
    private final TransactionManager transactionManager;
    
    private final MembershipManager membershipManager;
    
    private final SshKeyManager sshKeyManager;
    
    private final EmailAddressManager emailAddressManager;
    
	@Inject
    public PasswordAuthorizingRealm(UserManager userManager, SettingManager settingManager, 
    		MembershipManager membershipManager, GroupManager groupManager, 
    		ProjectManager projectManager, SessionManager sessionManager, 
    		TransactionManager transactionManager, SshKeyManager sshKeyManager, 
    		PasswordService passwordService, EmailAddressManager emailAddressManager) {
		super(userManager, groupManager, projectManager, sessionManager, settingManager);
		
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(passwordService);
		setCredentialsMatcher(passwordMatcher);
		
    	this.transactionManager = transactionManager;
    	this.membershipManager = membershipManager;
    	this.sshKeyManager = sshKeyManager;
    	this.emailAddressManager = emailAddressManager;
    }

	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}

	private User newUser(String userNameOrEmailAddress, Authenticated authenticated, 
						 boolean createAsGuest, @Nullable String defaultGroup) {
		User user = new User();
		var userName = userNameOrEmailAddress;
		if (userName.contains("@"))
			userName = StringUtils.substringBefore(userName, "@");
		user.setName(UserNameValidator.suggestUserName(userName));
		user.setPassword(User.EXTERNAL_MANAGED);
		user.setGuest(createAsGuest);
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		userManager.create(user);
		
		if (authenticated.getEmail() != null) {
			EmailAddress emailAddress = new EmailAddress();
			emailAddress.setValue(authenticated.getEmail());
			emailAddress.setVerificationCode(null);
			emailAddress.setOwner(user);
			emailAddress.setPrimary(true);
			emailAddress.setGit(true);
			emailAddressManager.create(emailAddress);

			user.getEmailAddresses().add(emailAddress);
		}

		Collection<String> groupNames = authenticated.getGroupNames();
		if (groupNames == null && defaultGroup != null)
			groupNames = Sets.newHashSet(defaultGroup);
		if (groupNames != null) 
			membershipManager.syncMemberships(user, groupNames);
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
    	return user;
	}
	
	@Transactional
	protected void updateUser(User user, Authenticated authenticated, @Nullable EmailAddress emailAddress) {
		if (emailAddress != null) {
			emailAddress.setVerificationCode(null);
			emailAddressManager.setAsPrimary(emailAddress);
		} else if (authenticated.getEmail() != null) {
			for (var eachEmailAddress: user.getEmailAddresses()) {
				eachEmailAddress.setPrimary(false);
				emailAddressManager.update(eachEmailAddress);
			}
			emailAddress = new EmailAddress();
			emailAddress.setValue(authenticated.getEmail());
			emailAddress.setVerificationCode(null);
			emailAddress.setOwner(user);
			emailAddress.setPrimary(true);
			emailAddress.setGit(true);
			emailAddressManager.create(emailAddress);
		}
		user.setSsoConnector(null);
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		userManager.update(user, null);
		
		if (authenticated.getGroupNames() != null)
			membershipManager.syncMemberships(user, authenticated.getGroupNames());
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
	}
	
	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) 
			throws AuthenticationException {
		return transactionManager.call(() -> {
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
							if (emailAddressValue != null) {
								emailAddress = emailAddressManager.findByValue(emailAddressValue);
								if (emailAddress != null && !emailAddress.getOwner().equals(user)) {
									throw new AuthenticationException("Email address '" + emailAddressValue
											+ "' has already been used by another user");
								} else {
									updateUser(user, authenticated, emailAddress);
									return user;
								}
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
				} else {
					Authenticator authenticator = settingManager.getAuthenticator();
					if (authenticator != null) {
						Authenticated authenticated = authenticator.authenticate((UsernamePasswordToken) token);
						String emailAddressValue = authenticated.getEmail();
						if (emailAddressValue != null) {
							if (emailAddressManager.findByValue(emailAddressValue) != null) {
								throw new AuthenticationException("Email address '" + emailAddressValue
										+ "' has already been used by another user");
							} else {
								return newUser(userNameOrEmailAddressValue, authenticated, 
										authenticator.isCreateUserAsGuest(), authenticator.getDefaultGroup());
							}
						} else {
							return newUser(userNameOrEmailAddressValue, authenticated, 
									authenticator.isCreateUserAsGuest(), authenticator.getDefaultGroup());
						}
					} else {
						throw new UnknownAccountException("Invalid credentials");
					}
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
		});
	}
}
