package io.onedev.server.entitymanager.impl;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultSshKeyManager extends AbstractEntityManager<SshKey> implements SshKeyManager {

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
}
