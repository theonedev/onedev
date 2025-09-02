package io.onedev.server.security.realm;

import static io.onedev.server.validation.validator.UserNameValidator.normalizeUserName;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.HashSet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class PasswordAuthenticatingRealm extends AuthenticatingRealm {

	private static final Logger logger = LoggerFactory.getLogger(PasswordAuthenticatingRealm.class);
	
	private final UserManager userManager;
	
    private final TransactionManager transactionManager;
    
    private final MembershipManager membershipManager;
    
    private final SshKeyManager sshKeyManager;
    
    private final EmailAddressManager emailAddressManager;
	
	private final SettingManager settingManager;
    
	@Inject
    public PasswordAuthenticatingRealm(UserManager userManager, SettingManager settingManager,
									   MembershipManager membershipManager, GroupManager groupManager,
									   ProjectManager projectManager, SessionManager sessionManager,
									   TransactionManager transactionManager, SshKeyManager sshKeyManager,
									   PasswordService passwordService, EmailAddressManager emailAddressManager) {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(passwordService);
		setCredentialsMatcher(passwordMatcher);
		this.userManager = userManager;
    	this.transactionManager = transactionManager;
    	this.membershipManager = membershipManager;
    	this.sshKeyManager = sshKeyManager;
    	this.emailAddressManager = emailAddressManager;
		this.settingManager = settingManager;
    }

	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}

	private User newUser(String userName, Authenticated authenticated, @Nullable String defaultGroupName) {
		User user = new User();
		user.setName(userName);
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

		syncGroupsAndSshKeys(user, true, authenticated, defaultGroupName);
    	return user;
	}

	private void syncGroupsAndSshKeys(User user, boolean forNewUser, 
			Authenticated authenticated, @Nullable String defaultGroupName) {
		var groupNames = authenticated.getGroupNames();
		if (forNewUser && groupNames == null) 
			groupNames = new HashSet<String>();
		if (groupNames != null) {
			if (defaultGroupName != null)
				groupNames.add(defaultGroupName);
			if (settingManager.getSecuritySetting().getDefaultGroupName() != null)
				groupNames.add(settingManager.getSecuritySetting().getDefaultGroupName());
			membershipManager.syncMemberships(user, groupNames);
		}
		
		if (authenticated.getSshKeys() != null)
			sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());									
	}
	
	@Transactional
	protected void updateUser(User user, Authenticated authenticated, 
			@Nullable EmailAddress emailAddress, @Nullable String defaultGroupName) {
		if (emailAddress != null) {
			emailAddress.setVerificationCode(null);
			if (!user.equals(emailAddress.getOwner())) 
				user.addEmailAddress(emailAddress);
			emailAddressManager.update(emailAddress);
		} else if (authenticated.getEmail() != null) {
			emailAddress = new EmailAddress();
			emailAddress.setValue(authenticated.getEmail());
			emailAddress.setVerificationCode(null);
			user.addEmailAddress(emailAddress);
			emailAddressManager.create(emailAddress);
		}
		syncGroupsAndSshKeys(user, false, authenticated, defaultGroupName);
	}
	
	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) 
			throws AuthenticationException {
		return transactionManager.call(() -> {
			try {
				var userName = normalizeUserName((String) token.getPrincipal());
				var user = userManager.findByVerifiedEmailAddress((String) token.getPrincipal());
				if (user == null) 
					user = userManager.findByName(userName);
				if (user != null) {
					if (user.isDisabled())
						throw new DisabledAccountException(_T("Account is disabled"));
					else if (user.isServiceAccount())
						throw new DisabledAccountException(_T("Service account not allowed to login"));
					if (user.getPassword() == null) {
						var authenticator = settingManager.getAuthenticator();
						if (authenticator != null) {
							var authenticated = authenticator.authenticate((UsernamePasswordToken) token);
							var emailAddressValue = authenticated.getEmail();
							if (emailAddressValue != null) {
								var emailAddress = emailAddressManager.findByValue(emailAddressValue);
								if (emailAddress != null) {
									if (emailAddress.getOwner().equals(user) || !emailAddress.isVerified()) {
										updateUser(user, authenticated, emailAddress, authenticator.getDefaultGroup());
										return user;
									} else {
										throw new AuthenticationException(MessageFormat.format(_T("Email address \"{0}\" already used by another account"), emailAddressValue));
									}
								} else {
									updateUser(user, authenticated, null, authenticator.getDefaultGroup());
									return user;
								}
							} else {
								updateUser(user, authenticated, null, authenticator.getDefaultGroup());
								return user;																
							}
						} else {
							throw new AuthenticationException(MessageFormat.format(_T("No external password authenticator to authenticate user \"{0}\""), userName));
						}
					} else {
						return user;
					}
				} else {
					var authenticator = settingManager.getAuthenticator();
					if (authenticator != null) {
						var authenticated = authenticator.authenticate((UsernamePasswordToken) token);
						var emailAddressValue = authenticated.getEmail();
						if (emailAddressValue != null) {
							var emailAddress = emailAddressManager.findByValue(emailAddressValue);
							if (emailAddress != null) {								
								if (!emailAddress.isVerified()) {
									emailAddressManager.delete(emailAddress);
									return newUser(userName, authenticated, authenticator.getDefaultGroup());
								} else {
									throw new AuthenticationException(MessageFormat.format(_T("Email address \"{0}\" already used by another account"), emailAddressValue));
								}
							} else {
								return newUser(userName, authenticated, authenticator.getDefaultGroup());
							}
						} else {
							return newUser(userName, authenticated, authenticator.getDefaultGroup());
						}
					} else {
						throw new UnknownAccountException(_T("Invalid credentials"));
					}
				}
			} catch (Exception e) {
				if (e instanceof AuthenticationException) {
					logger.debug("Authentication not passed", e);
					throw ExceptionUtils.unchecked(e);
				} else {
					logger.error("Error authenticating user", e);
					throw new AuthenticationException(_T("Error authenticating user"), e);
				}
			}
		});
	}
}
