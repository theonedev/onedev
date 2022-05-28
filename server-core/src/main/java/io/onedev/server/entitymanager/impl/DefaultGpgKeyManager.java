package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bouncycastle.openpgp.PGPPublicKey;

import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.git.signature.SignatureVerificationKey;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.GpgKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.GpgUtils;

@Singleton
public class DefaultGpgKeyManager extends BaseEntityManager<GpgKey> implements GpgKeyManager {

	private final TransactionManager transactionManager;
	
	private final Map<Long, Long> entityIdCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultGpgKeyManager(Dao dao, TransactionManager transactionManager) {
        super(dao);
        this.transactionManager = transactionManager;
    }
    
    @Listen
    @Sessional
    public void on(SystemStarted event) {
    	for (GpgKey key: query()) {
    		for (Long keyId: key.getKeyIds())
    			entityIdCache.put(keyId, key.getId());
    	}
    }
    
    @Transactional
    @Listen
    public void on(EntityRemoved event) {
    	if (event.getEntity() instanceof GpgKey) {
    		List<Long> keyIds = ((GpgKey)event.getEntity()).getKeyIds();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					entityIdCache.keySet().removeAll(keyIds);
				}
    			
    		});
    	} else if (event.getEntity() instanceof EmailAddress) {
    		EmailAddress emailAddress = (EmailAddress) event.getEntity();
    		Collection<Long> keyIds = new ArrayList<>();
    		for (GpgKey key: emailAddress.getGpgKeys()) 
    			keyIds.addAll(key.getKeyIds());
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					entityIdCache.keySet().removeAll(keyIds);
				}
    			
    		});
    	} else if (event.getEntity() instanceof User) {
    		User user = (User) event.getEntity();
    		Collection<Long> keyIds = new ArrayList<>();
    		for (EmailAddress emailAddress: user.getEmailAddresses()) {
        		for (GpgKey key: emailAddress.getGpgKeys()) 
        			keyIds.addAll(key.getKeyIds());
    		}
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					entityIdCache.keySet().removeAll(keyIds);
				}
    			
    		});
    	}
    }
    
    @Transactional
    @Listen
    public void on(EntityPersisted event) {
    	if (event.getEntity() instanceof GpgKey) {
    		GpgKey gpgKey = (GpgKey) event.getEntity();
    		List<Long> keyIds = gpgKey.getKeyIds();
    		Long entityId = gpgKey.getId();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					for (Long keyId: keyIds)
						entityIdCache.put(keyId, entityId);
				}
    			
    		});
    	}
    }

    @Sessional
	@Override
	public SignatureVerificationKey findSignatureVerificationKey(long keyId) {
    	Long entityId = entityIdCache.get(keyId);
    	if (entityId != null) {
    		return new SignatureVerificationKey() {
				
				@Override
				public boolean shouldVerifyDataWriter() {
					return true;
				}
				
				@Override
				public PGPPublicKey getPublicKey() {
					for (PGPPublicKey publicKey: load(entityId).getPublicKeys()) {
						if (keyId == publicKey.getKeyID())
							return publicKey;
					}
					throw new IllegalStateException();
				}

				@Override
				public String getEmailAddress() {
					return GpgUtils.getEmailAddress(load(entityId).getPublicKeys().get(0));
				}
				
			};
    	} else {
    		return null;
    	}
	}
    
}
