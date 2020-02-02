package io.onedev.server.git.ssh;

import java.util.List;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

public class SshUtils {
    public static List<SshKey> loadUserKeys(User user, Dao dao) {
        SimpleExpression eq = Restrictions.eq("owner", user);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        
        List<SshKey> keys = dao.query(entityCriteria);
        return keys;
    }
}
