package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.git.signatureverification.gpg.GpgSigningKey;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.GpgKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.GpgUtils;
import org.bouncycastle.openpgp.PGPPublicKey;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class DefaultGpgKeyManager extends BaseEntityManager<GpgKey> implements GpgKeyManager {

	private final TransactionManager transactionManager;
	
	private final EmailAddressManager emailAddressManager;
	
	private final ClusterManager clusterManager;
	
	private volatile Map<Long, Long> entityIds;
	
    @Inject
    public DefaultGpgKeyManager(Dao dao, TransactionManager transactionManager, EmailAddressManager emailAddressManager, 
    		ClusterManager clusterManager) {
        super(dao);
        this.transactionManager = transactionManager;
        this.emailAddressManager = emailAddressManager;
        this.clusterManager = clusterManager;
    }
    
    @Listen
    @Sessional
    public void on(SystemStarting event) {
		var hazelcastInstance = clusterManager.getHazelcastInstance();
    	entityIds = hazelcastInstance.getMap("gpgKeyEntityIds");
    	var cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("gpgKeyCacheInited");
		clusterManager.init(cacheInited, () -> {
			for (GpgKey key: query()) {
				for (Long keyId: key.getKeyIds())
					entityIds.put(keyId, key.getId());
			}
			return 1L;			
		});
    }
    
    @Transactional
    @Listen
    public void on(EntityRemoved event) {
    	if (event.getEntity() instanceof GpgKey) {
    		List<Long> keyIds = ((GpgKey)event.getEntity()).getKeyIds();
    		transactionManager.runAfterCommit(() -> {
				for (var id: keyIds)
					entityIds.remove(id);
			});
    	} else if (event.getEntity() instanceof User) {
    		User user = (User) event.getEntity();
    		Collection<Long> keyIds = new ArrayList<>();
    		for (GpgKey key: user.getGpgKeys()) 
    			keyIds.addAll(key.getKeyIds());
    		transactionManager.runAfterCommit(() -> {
				for (var id: keyIds)
					entityIds.remove(id);
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
    		transactionManager.runAfterCommit(() -> {
				for (Long keyId: keyIds)
					entityIds.put(keyId, entityId);
			});
    	}
    }

    @Sessional
	@Override
	public GpgSigningKey findSigningKey(long keyId) {
    	Long entityId = entityIds.get(keyId);
    	if (entityId != null) {
    		return new GpgSigningKey() {
				
    			private transient Collection<String> emailAddresses;
				
				@Override
				public PGPPublicKey getPublicKey() {
					for (PGPPublicKey publicKey: load(entityId).getPublicKeys()) {
						if (keyId == publicKey.getKeyID())
							return publicKey;
					}
					throw new IllegalStateException();
				}

				@Override
				public Collection<String> getEmailAddresses() {
					if (emailAddresses == null) {
						emailAddresses = new LinkedHashSet<>();
						GpgKey gpgKey = load(entityId);
						for (String value: GpgUtils.getEmailAddresses(gpgKey.getPublicKeys().get(0))) {
							EmailAddress emailAddress = emailAddressManager.findByValue(value);
							if (emailAddress != null 
									&& emailAddress.isVerified() 
									&& emailAddress.getOwner().equals(gpgKey.getOwner())) {
								emailAddresses.add(value);
							}
						}
					}
					return emailAddresses;
				}
				
			};
    	} else {
    		return null;
    	}
	}

	@Transactional
	@Override
	public void create(GpgKey gpgKey) {
		Preconditions.checkState(gpgKey.isNew());
		dao.persist(gpgKey);
	}

}
