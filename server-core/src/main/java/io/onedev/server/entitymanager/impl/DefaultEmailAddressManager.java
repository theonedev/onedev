package io.onedev.server.entitymanager.impl;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.hazelcast.core.HazelcastInstance;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
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
import io.onedev.server.util.facade.EmailAddressCache;
import io.onedev.server.util.facade.EmailAddressFacade;

@Singleton
public class DefaultEmailAddressManager extends BaseEntityManager<EmailAddress> implements EmailAddressManager {

	private final SettingManager settingManager;
	
	private final MailManager mailManager;
	
	private final TransactionManager transactionManager;
	
	private final SessionManager sessionManager;
	
	private final ClusterManager clusterManager;
	
	private volatile EmailAddressCache cache;
	
    @Inject
    public DefaultEmailAddressManager(Dao dao, SettingManager settingManager, MailManager mailManager, 
    		TransactionManager transactionManager, SessionManager sessionManager, 
    		ClusterManager clusterManager) {
        super(dao);
        this.settingManager = settingManager;
        this.mailManager = mailManager;
        this.transactionManager = transactionManager;
        this.sessionManager = sessionManager;
        this.clusterManager = clusterManager;
    }

    @Listen
    @Sessional
    public void on(SystemStarted event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
        cache = new EmailAddressCache(hazelcastInstance.getReplicatedMap("emailAddressCache"));
        
    	for (EmailAddress address: query())
    		cache.put(address.getId(), address.getFacade());
    }
    
    @Sessional
    @Override
    public EmailAddress findByValue(String value) {
		EmailAddressFacade facade = cache.findByValue(value);
		if (facade != null)
			return load(facade.getId());
		else
			return null;
    }

    @Sessional
    @Override
    public EmailAddressFacade findFacadeByValue(String value) {
    	return cache.findByValue(value);
    }
    
    @Sessional
    @Override
    public EmailAddress findByPersonIdent(PersonIdent personIdent) {
		EmailAddressFacade facade = cache.findByPersonIdent(personIdent);
		if (facade != null)
			return load(facade.getId());
		else
			return null;
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
		
		if (isNew && !emailAddress.isVerified() && settingManager.getMailSetting() != null) {
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
			    	cache.remove(id);
				}
    			
    		});
    	} else if (event.getEntity() instanceof User) {
    		Long ownerId = event.getEntity().getId();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					for (var id: cache.entrySet().stream()
							.filter(it->it.getValue().getOwnerId().equals(ownerId))
							.map(it->it.getKey())
							.collect(Collectors.toSet())) {
						cache.remove(id);
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
					if (cache != null)
						cache.put(facade.getId(), facade);
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
		
		String verificationUrl = String.format("%s/~verify-email-address/%d/%s", 
				serverUrl, emailAddress.getId(), emailAddress.getVerificationCode());
		String htmlBody = String.format("Hello,"
			+ "<p style='margin: 16px 0;'>"
			+ "The account \"%s\" at \"%s\" tries to use email address \"%s\", please visit below link to verify if this is you:<br><br>"
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
		EmailAddressFacade facade = cache.findPrimary(user.getId());
		if (facade != null)
			return load(facade.getId());
		else
			return null;
	}

	@Override
	public EmailAddressFacade findPrimaryFacade(Long userId) {
   		return cache.findPrimary(userId);
	}
	
	@Override
	public EmailAddress findGit(User user) {
		EmailAddressFacade facade = cache.findGit(user);
		if (facade != null)
			return load(facade.getId());
		else
			return null;
	}

	@Override
	public EmailAddressCache cloneCache() {
		return cache.clone();
	}

}
