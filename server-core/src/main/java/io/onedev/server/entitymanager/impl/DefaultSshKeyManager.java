package io.onedev.server.entitymanager.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.sshd.common.config.keys.KeyUtils;
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
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.CipherUtils;
import io.onedev.server.ssh.SshKeyUtils;

@Singleton
public class DefaultSshKeyManager extends BaseEntityManager<SshKey> implements SshKeyManager {

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
    public void syncSshKeys(User user, Collection<String> sshKeys) {
    	Map<String, SshKey> syncMap = new HashMap<>();
    	for (String content: sshKeys) {
    		try {
    			PublicKey pubEntry = SshKeyUtils.decodeSshPublicKey(content);
    	        String digest = KeyUtils.getFingerPrint(CipherUtils.DIGEST_FORMAT, pubEntry);
    			
    	        SshKey sshKey = new SshKey();
    	        sshKey.setDigest(digest);
    	        sshKey.setContent(content);
    	        sshKey.setOwner(user);
    	        sshKey.setDate(new Date());
    	        syncMap.put(content, sshKey);
    		} catch (IOException | GeneralSecurityException e) {
    			logger.error("Error parsing SSH key", e);
    		}
    	}

    	Map<String, SshKey> currentMap = new HashMap<>();
		user.getSshKeys().forEach(sshKey -> currentMap.put(sshKey.getContent(), sshKey));
		
		MapDifference<String, SshKey> diff = Maps.difference(currentMap, syncMap);
		
		diff.entriesOnlyOnLeft().values().forEach(sshKey -> delete(sshKey));
		
		diff.entriesOnlyOnRight().values().forEach(sshKey -> {
			if (findByDigest(sshKey.getDigest()) == null) 
				save(sshKey);	
			else 
				logger.warn("SSH key is already in use (digest: {})", sshKey.getDigest());
		});
		
    }

}
