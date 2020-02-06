package io.onedev.server.git.ssh;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.common.digest.BaseDigest;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

public class SshKeyUtils {
    public static final BaseDigest MD5_DIGESTER = new BaseDigest("MD5", 512);

    public static List<SshKey> loadUserKeys(User user, Dao dao) {
        SimpleExpression eq = Restrictions.eq("owner", user);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        
        List<SshKey> keys = dao.query(entityCriteria);
        return keys;
    }
    
    public static SshKey loadKeyByDigest(String digest, Dao dao) {
        SimpleExpression eq = Restrictions.eq("digest", digest);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        
        return dao.find(entityCriteria);
    }

    public static PublicKey decodePublicKey(String publicKey) throws IOException, GeneralSecurityException {
        StringReader stringReader = new StringReader(publicKey);
        List<AuthorizedKeyEntry> entries = AuthorizedKeyEntry.readAuthorizedKeys(stringReader, true);

        AuthorizedKeyEntry entry = entries.get(0);
        PublicKey pubEntry = entry.resolvePublicKey(PublicKeyEntryResolver.FAILING);
        return pubEntry;
    }
    
    public static boolean isKeyAlreadyInUse(String keyDigest, Dao dao) {
        SimpleExpression eq = Restrictions.eq("digest", keyDigest);
        EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
        
        List<SshKey> keys = dao.query(entityCriteria);
        
        return keys.size() > 0;
    }
}
