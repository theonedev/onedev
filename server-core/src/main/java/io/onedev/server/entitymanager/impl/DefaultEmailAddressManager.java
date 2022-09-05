package io.onedev.server.entitymanager.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.mail.MailManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.facade.EmailAddressFacade;
import io.onedev.server.util.facade.EmailAddressFacades;

@Singleton
public class DefaultEmailAddressManager extends BaseEntityManager<EmailAddress> implements EmailAddressManager {

	private final SettingManager settingManager;
	
	private final MailManager mailManager;
	
	private final TransactionManager transactionManager;
	
	private final SessionManager sessionManager;
	
	private final EmailAddressFacades cache = new EmailAddressFacades();
	
	private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
	
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
    	cacheLock.writeLock().lock();
    	try {
	    	for (EmailAddress address: query())
	    		cache.put(address.getId(), address.getFacade());
    	} finally {
    		cacheLock.writeLock().unlock();
    	}
    }
    
    @Sessional
    @Override
    public EmailAddress findByValue(String value) {
    	cacheLock.readLock().lock();
    	try {
    		EmailAddressFacade facade = cache.findByValue(value);
    		if (facade != null)
    			return load(facade.getId());
    		else
    			return null;
    	} finally {
    		cacheLock.readLock().unlock();
    	}
    }

    @Sessional
    @Override
    public EmailAddress findByPersonIdent(PersonIdent personIdent) {
    	cacheLock.readLock().lock();
    	try {
    		EmailAddressFacade facade = cache.findByPersonIdent(personIdent);
    		if (facade != null)
    			return load(facade.getId());
    		else
    			return null;
    	} finally {
    		cacheLock.readLock().unlock();
    	}
    }
    
    @Transactional
	@Override
	public void setAsPrimary(EmailAddress emailAddress) {
    	for (EmailAddress each: emailAddress.getOwner().getEmailAddresses()) {
    		each.setPrimary(false);
    		dao.persist(each);
    	}
    	emailAddress.setPrimary(true);
    	dao.persist(emailAddress);
	}

    @Transactional
	@Override
	public void useForGitOperations(EmailAddress emailAddress) {
    	for (EmailAddress each: emailAddress.getOwner().getEmailAddresses()) {
    		each.setGit(false);
    		dao.persist(each);
    	}
    	emailAddress.setGit(true);
    	dao.persist(emailAddress);
	}

    @Transactional
    @Override
	public void delete(EmailAddress emailAddress) {
		super.delete(emailAddress);

		if (emailAddress.isPrimary() || emailAddress.isGit()) {
			User user = emailAddress.getOwner();
			user.getEmailAddresses().remove(emailAddress);
			if (!user.getSortedEmailAddresses().isEmpty()) {
				EmailAddress firstEmailAddress = user.getSortedEmailAddresses().iterator().next();
				if (emailAddress.isPrimary()) 
					firstEmailAddress.setPrimary(true);
				if (emailAddress.isGit())
					firstEmailAddress.setGit(true);
				dao.persist(firstEmailAddress);
			}
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
		dao.persist(emailAddress);
		
		user.getEmailAddresses().add(emailAddress);
		
		if (isNew && settingManager.getMailSetting() != null && !emailAddress.isVerified()) {
			Long addressId = emailAddress.getId();
			sessionManager.runAsyncAfterCommit(new Runnable() {

				@Override
				public void run() {
					sendVerificationEmail(load(addressId));
				}
				
			});
		}
	}

    @Transactional
    @Listen
    public void on(EntityRemoved event) {
    	if (event.getEntity() instanceof EmailAddress) {
    		Long id = event.getEntity().getId();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
			    	cacheLock.writeLock().lock();
			    	try {
			    		cache.remove(id);
			    	} finally {
			    		cacheLock.writeLock().unlock();
			    	}
				}
    			
    		});
    	} else if (event.getEntity() instanceof User) {
    		Long ownerId = event.getEntity().getId();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
			    	cacheLock.writeLock().lock();
			    	try {
			    		for (Iterator<Map.Entry<Long, EmailAddressFacade>> it = cache.entrySet().iterator(); it.hasNext();) {
			    			if (it.next().getValue().getOwnerId().equals(ownerId))
			    				it.remove();
			    		}
			    	} finally {
			    		cacheLock.writeLock().unlock();
			    	}
				}
    			
    		});
    	}
    }
    
    @Transactional
    @Listen
    public void on(EntityPersisted event) {
    	if (event.getEntity() instanceof EmailAddress) {
    		EmailAddressFacade facade = ((EmailAddress) event.getEntity()).getFacade();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
			    	cacheLock.writeLock().lock();
			    	try {
			    		cache.put(facade.getId(), facade);
			    	} finally {
			    		cacheLock.writeLock().unlock();
			    	}
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
				serverUrl, emailAddress.getId(), emailAddress.getVerificationCode());
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
				settingManager.getMailSetting().getSendSetting(), 
				Arrays.asList(emailAddress.getValue()),
				Lists.newArrayList(), Lists.newArrayList(), 
				"[Verification] Please Verify Your Email Address", 
				htmlBody, textBody, null, null);
	}

	@Override
	public EmailAddress findPrimary(User user) {
    	cacheLock.readLock().lock();
    	try {
    		EmailAddressFacade facade = cache.findPrimary(user);
    		if (facade != null)
    			return load(facade.getId());
    		else
    			return null;
    	} finally {
    		cacheLock.readLock().unlock();
    	}
	}

	@Override
	public EmailAddress findGit(User user) {
    	cacheLock.readLock().lock();
    	try {
    		EmailAddressFacade facade = cache.findGit(user);
    		if (facade != null)
    			return load(facade.getId());
    		else
    			return null;
    	} finally {
    		cacheLock.readLock().unlock();
    	}
	}

	@Override
	public EmailAddressFacades cloneCache() {
		return cache.clone();
	}

}
