package io.onedev.server.entitymanager.impl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultSshKeyManager extends AbstractEntityManager<SshKey> implements SshKeyManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultSshKeyManager.class);
	
    @Inject
    public DefaultSshKeyManager(Dao dao) {
        super(dao);
    }
    
    @Sessional
    @Override
    public SshKey findByDigest(String digest) {
        SimpleExpression eq = Restrictions.eq("digest", digest);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        entityCriteria.setCacheable(true);
        return find(entityCriteria);
    }
    
    @Transactional
    @Override
    public void syncUserKeys(User user, Collection<SshKey> keys) {
		Collection<SshKey> currentKeys = user.getSshKeys();

		Map<String, SshKey> currentKeysMap = sshPublicKeysToMap(currentKeys);
		Map<String, SshKey> authKeysMap = sshPublicKeysToMap(keys);
		MapDifference<String, SshKey> diff = Maps.difference(currentKeysMap, authKeysMap);
		
		// remove keys not delivered by authentication
		diff.entriesOnlyOnLeft().values().forEach((key) -> delete(key));
		
		// add keys from authorization
		diff.entriesOnlyOnRight().values().forEach((key) -> {
			
			if (findByDigest(key.getDigest()) != null) {
				key.setTimestamp(LocalDateTime.now());
				key.setOwner(user);
				save(key);	
			} else {
				logger.warn("SSH public key provided by auth is already in use", key.getDigest());
			}
			
		});
    }

	private Map<String, SshKey> sshPublicKeysToMap(Collection<SshKey> keys) {
		Map<String, SshKey> keysMap = new HashMap<String, SshKey>();
		keys.forEach((key) -> keysMap.put(key.getDigest(), key));
		return keysMap;
	}
}
