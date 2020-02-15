package io.onedev.server.git.ssh;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.common.digest.BaseDigest;

public class SshKeyUtils {
    public static final BaseDigest MD5_DIGESTER = new BaseDigest("MD5", 512);

    public static PublicKey decodePublicKey(String publicKey) throws IOException, GeneralSecurityException {
        StringReader stringReader = new StringReader(publicKey);
        List<AuthorizedKeyEntry> entries = AuthorizedKeyEntry.readAuthorizedKeys(stringReader, true);

        AuthorizedKeyEntry entry = entries.get(0);
        PublicKey pubEntry = entry.resolvePublicKey(PublicKeyEntryResolver.FAILING);
        return pubEntry;
    }
}
