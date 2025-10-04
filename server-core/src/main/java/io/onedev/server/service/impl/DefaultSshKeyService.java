package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.SshKeyService;

@Singleton
public class DefaultSshKeyService extends BaseEntityService<SshKey> implements SshKeyService {

	private static final Logger logger = LoggerFactory.getLogger(DefaultSshKeyService.class);

    @Sessional
    @Override
    public SshKey findByFingerprint(String fingerprint) {
        SimpleExpression eq = Restrictions.eq("fingerprint", fingerprint);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        entityCriteria.setCacheable(true);
        return find(entityCriteria);
    }
    
    @Transactional
    @Override
    public void syncSshKeys(User user, Collection<String> sshKeys) {
    	Map<String, SshKey> syncMap = new HashMap<>();
    	for (String content: sshKeys) {
	        SshKey sshKey = new SshKey();
	        sshKey.setContent(content);
	        sshKey.setOwner(user);
	        sshKey.setCreatedAt(new Date());
	        sshKey.generateFingerprint();
	        syncMap.put(content, sshKey);
    	}

    	Map<String, SshKey> currentMap = new HashMap<>();
		user.getSshKeys().forEach(sshKey -> currentMap.put(sshKey.getContent(), sshKey));
		
		MapDifference<String, SshKey> diff = Maps.difference(currentMap, syncMap);
		
		diff.entriesOnlyOnLeft().values().forEach(sshKey -> delete(sshKey));
		
		diff.entriesOnlyOnRight().values().forEach(sshKey -> {
			if (findByFingerprint(sshKey.getFingerprint()) == null) 
				create(sshKey);	
			else 
				logger.warn("SSH key is already in use (fingerprint: {})", sshKey.getFingerprint());
		});
		
    }

	@Transactional
	@Override
	public void create(SshKey sshKey) {
		Preconditions.checkState(sshKey.isNew());
		dao.persist(sshKey);
	}

}
