package io.onedev.server.entitymanager.impl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.OneAuthorizingRealm;

@Singleton
public class DefaultSshKeyManager extends AbstractEntityManager<SshKey> implements SshKeyManager {

	private static final Logger logger = LoggerFactory.getLogger(OneAuthorizingRealm.class);
	
    @Inject
    public DefaultSshKeyManager(Dao dao) {
        super(dao);
    }
    
    @Sessional
    @Override
    public List<SshKey> loadUserKeys(User user) {
        SimpleExpression eq = Restrictions.eq("owner", user);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        
        return query(entityCriteria);
    }
    
    @Sessional
    @Override
    public SshKey loadKeyByDigest(String digest) {
        SimpleExpression eq = Restrictions.eq("digest", digest);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        
        return find(entityCriteria);
    }
    
    @Sessional
    @Override
    public boolean isKeyAlreadyInUse(String keyDigest) {
        SimpleExpression eq = Restrictions.eq("digest", keyDigest);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        
        List<SshKey> keys = query(entityCriteria);
        
        return keys.size() > 0;
    }
    
    @Override
    public void syncUserKeys(User user, Collection<SshKey> keys) {
    	SshKeyManager sshKeyManager = OneDev.getInstance(SshKeyManager.class);
		List<SshKey> currentKeys = sshKeyManager.loadUserKeys(user);

		Map<String, SshKey> currentKeysMap = sshPublicKeysToMap(currentKeys);
		Map<String, SshKey> authKeysMap = sshPublicKeysToMap(keys);
		MapDifference<String, SshKey> diff = Maps.difference(currentKeysMap, authKeysMap);
		
		// remove keys not delivered by authentication
		diff.entriesOnlyOnLeft().values().forEach((key) -> sshKeyManager.delete(key));
		
		// add keys from authorization
		diff.entriesOnlyOnRight().values().forEach((key) -> {
			
			if (!sshKeyManager.isKeyAlreadyInUse(key.getDigest())) {
				key.setTimestamp(LocalDateTime.now());
				key.setOwner(user);
				sshKeyManager.save(key);	
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
