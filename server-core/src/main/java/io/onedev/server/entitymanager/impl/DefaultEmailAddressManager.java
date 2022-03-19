package io.onedev.server.entitymanager.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.notification.MailManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultEmailAddressManager extends BaseEntityManager<EmailAddress> implements EmailAddressManager {

	private final SettingManager settingManager;
	
	private final MailManager mailManager;
	
	private final TransactionManager transactionManager;
	
	private final SessionManager sessionManager;
	
	private final Map<String, Long> idCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultEmailAddressManager(Dao dao, SettingManager settingManager, MailManager mailManager, 
    		TransactionManager transactionManager, SessionManager sessionManager) {
        super(dao);
        this.settingManager = settingManager;
        this.mailManager = mailManager;
        this.transactionManager = transactionManager;
        this.sessionManager = sessionManager;
    }

    @Listen
    @Sessional
    public void on(SystemStarted event) {
    	for (EmailAddress address: query())
    		idCache.put(address.getValue(), address.getId());
    }
    
    @Sessional
    @Override
    public EmailAddress findByValue(String value) {
    	Long id = idCache.get(value.toLowerCase());
    	return id != null? load(id): null;
    }

    @Sessional
    @Override
    public EmailAddress findByPersonIdent(PersonIdent personIdent) {
    	if (StringUtils.isNotBlank(personIdent.getEmailAddress()))
    		return findByValue(personIdent.getEmailAddress());
    	else
    		return null;
    }
    
    @Transactional
	@Override
	public void setAsPrimary(EmailAddress emailAddress) {
    	for (EmailAddress each: emailAddress.getOwner().getEmailAddresses())
    		each.setPrimary(false);
    	emailAddress.setPrimary(true);
	}

    @Transactional
	@Override
	public void useForGitOperations(EmailAddress emailAddress) {
    	for (EmailAddress each: emailAddress.getOwner().getEmailAddresses())
    		each.setGit(false);
    	emailAddress.setGit(true);
	}

    @Transactional
    @Override
	public void delete(EmailAddress emailAddress) {
		super.delete(emailAddress);
		
		User user = emailAddress.getOwner();
		
		user.getEmailAddresses().remove(emailAddress);
		if (!user.getSortedEmailAddresses().isEmpty()) {
			if (user.getPrimaryEmailAddress() == null)
				user.getSortedEmailAddresses().iterator().next().setPrimary(true);
			if (user.getGitEmailAddress() == null)
				user.getSortedEmailAddresses().iterator().next().setGit(true);
		}
	}

	@Transactional
	@Override
	public void save(EmailAddress emailAddress) {
		boolean isNew = emailAddress.isNew();
		emailAddress.setValue(emailAddress.getValue().toLowerCase());
		
		User user = emailAddress.getOwner();
		if (user.getEmailAddresses().isEmpty()) {
			emailAddress.setPrimary(true);
			emailAddress.setGit(true);
		}
		super.save(emailAddress);
		
		user.getEmailAddresses().add(emailAddress);
		
		if (isNew && settingManager.getMailSetting() != null && !emailAddress.isVerified()) {
			Long addressId = emailAddress.getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					sessionManager.runAsync(new Runnable() {

						@Override
						public void run() {
							sendVerificationEmail(load(addressId));
						}
						
					});
				}
				
			});
		}
	}

    @Transactional
    @Listen
    public void on(EntityRemoved event) {
    	if (event.getEntity() instanceof EmailAddress) {
    		String value = ((EmailAddress)event.getEntity()).getValue();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					idCache.remove(value);
				}
    			
    		});
    	} else if (event.getEntity() instanceof User) {
    		User user = (User) event.getEntity();
    		Collection<String> values = user.getEmailAddresses().stream()
    				.map(it->it.getValue()).collect(Collectors.toList());
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					idCache.keySet().removeAll(values);
				}
    			
    		});
    	}
    }
    
    @Transactional
    @Listen
    public void on(EntityPersisted event) {
    	if (event.getEntity() instanceof EmailAddress) {
    		EmailAddress emailAddress = (EmailAddress) event.getEntity();
    		String value = emailAddress.getValue();
    		Long id = emailAddress.getId();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					idCache.put(value, id);
				}
    			
    		});
    	}
    }
    
	@Override
	public void sendVerificationEmail(EmailAddress emailAddress) {
		Preconditions.checkState(settingManager.getMailSetting() != null 
				&& !emailAddress.isVerified());

		User user = emailAddress.getOwner();
		
		String serverUrl = settingManager.getSystemSetting().getServerUrl();
		
		String verificationUrl = String.format("%s/verify-email-address/%d/%s", 
				serverUrl, emailAddress.getId(), emailAddress.getVerficationCode());
		String htmlBody = String.format("Hello,"
			+ "<p style='margin: 16px 0;'>"
			+ "The account \"%s\" at \"%s\" tries to use email address '%s', please visit below link to verify if this is you:<br><br>"
			+ "<a href='%s'>%s</a>",
			user.getName(), serverUrl, emailAddress.getValue(), verificationUrl, verificationUrl);

		String textBody = String.format("Hello,\n\n"
				+ "The account \"%s\" at \"%s\" tries to use email address \"%s\", please visit below link to verify if this is you:\n\n"
				+ "%s",
				user.getName(), serverUrl, emailAddress.getValue(), verificationUrl);
		
		mailManager.sendMail(
				settingManager.getMailSetting(), 
				Arrays.asList(emailAddress.getValue()),
				Lists.newArrayList(), Lists.newArrayList(), 
				"[Verification] Please Verify Your Email Address", 
				htmlBody, textBody, null, null);
	}
    
}
