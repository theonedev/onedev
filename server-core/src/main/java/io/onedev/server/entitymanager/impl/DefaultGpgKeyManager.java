package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.GpgKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultGpgKeyManager extends BaseEntityManager<GpgKey> implements GpgKeyManager {

	private final TransactionManager transactionManager;
	
	private final Map<Long, Long> idCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultGpgKeyManager(Dao dao, TransactionManager transactionManager) {
        super(dao);
        this.transactionManager = transactionManager;
    }
    
    @Sessional
    @Override
    public GpgKey findByKeyId(long keyId) {
    	Long id = idCache.get(keyId);
    	return id != null? load(id): null;
    }

    @Listen
    @Sessional
    public void on(SystemStarted event) {
    	for (GpgKey key: query())
    		idCache.put(key.getKeyId(), key.getId());
    }

    @Transactional
    @Listen
    public void on(EntityRemoved event) {
    	if (event.getEntity() instanceof GpgKey) {
    		long keyId = ((GpgKey)event.getEntity()).getKeyId();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					idCache.remove(keyId);
				}
    			
    		});
    	} else if (event.getEntity() instanceof EmailAddress) {
    		EmailAddress emailAddress = (EmailAddress) event.getEntity();
    		Collection<Long> keyIds = emailAddress.getGpgKeys().stream()
					.map(it->it.getKeyId())
					.collect(Collectors.toList());
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					idCache.keySet().removeAll(keyIds);
				}
    			
    		});
    	} else if (event.getEntity() instanceof User) {
    		User user = (User) event.getEntity();
    		Collection<Long> keyIds = new ArrayList<>();
    		for (EmailAddress emailAddress: user.getEmailAddresses()) {
    			keyIds.addAll(emailAddress.getGpgKeys().stream()
    					.map(it->it.getKeyId())
    					.collect(Collectors.toList()));
    		}
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					idCache.keySet().removeAll(keyIds);
				}
    			
    		});
    	}
    }
    
    @Transactional
    @Listen
    public void on(EntityPersisted event) {
    	if (event.getEntity() instanceof GpgKey) {
    		GpgKey gpgKey = (GpgKey) event.getEntity();
    		long keyId = gpgKey.getKeyId();
    		Long id = gpgKey.getId();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					idCache.put(keyId, id);
				}
    			
    		});
    	}
    }
    
}
