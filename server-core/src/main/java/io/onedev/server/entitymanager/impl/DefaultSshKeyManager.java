package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.Date;
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
    public void syncSshKeys(User user, Collection<SshKey> sshKeys) {
		Collection<SshKey> currentSshKeys = user.getSshKeys();

		Map<String, SshKey> currentKeysMap = keysToMap(currentSshKeys);
		Map<String, SshKey> keysMap = keysToMap(sshKeys);
		MapDifference<String, SshKey> diff = Maps.difference(currentKeysMap, keysMap);
		
		diff.entriesOnlyOnLeft().values().forEach((key) -> delete(key));
		
		diff.entriesOnlyOnRight().values().forEach((key) -> {
			if (findByDigest(key.getDigest()) == null) {
				key.setDate(new Date());
				key.setOwner(user);
				save(key);	
			} else {
				logger.warn("SSH key is already in use (digest: {})", key.getDigest());
			}
		});
		
    }

	private Map<String, SshKey> keysToMap(Collection<SshKey> sshKeys) {
		Map<String, SshKey> keysMap = new HashMap<String, SshKey>();
		// use content as map key as we want to update key if key comment is changed
		sshKeys.forEach((key) -> keysMap.put(key.getContent(), key));
		return keysMap;
	}
	
}
