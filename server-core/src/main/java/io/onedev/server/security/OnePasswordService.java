package io.onedev.server.security;

import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.StringUtils;

@Singleton
public class OnePasswordService implements PasswordService {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");

    private static final Logger logger = LoggerFactory.getLogger(OnePasswordService.class);

    @Override
    public String encryptPassword(Object plaintextPassword) throws IllegalArgumentException {
        String str;
        if (plaintextPassword instanceof char[])
            str = new String((char[]) plaintextPassword);
        else if (plaintextPassword instanceof String)
            str = (String) plaintextPassword;
        else 
            throw new IllegalArgumentException("Unsupported password type " + plaintextPassword.getClass());

        return BCrypt.hashpw(str, BCrypt.gensalt());
    }

    @Override
    public boolean passwordsMatch(Object submittedPlaintext, String encrypted) {
        String raw;

        if (submittedPlaintext instanceof char[]) 
            raw = new String((char[]) submittedPlaintext);
        else if (submittedPlaintext instanceof String) 
            raw = (String) submittedPlaintext;
        else 
            throw new IllegalArgumentException("Unsupported password type " + submittedPlaintext.getClass());

        if (StringUtils.isBlank(encrypted)) {
            logger.warn("Empty encoded password");
            return false;
        }

        if (!BCRYPT_PATTERN.matcher(encrypted).matches()) {
            logger.warn("Encoded password does not look like BCrypt");
            return false;
        }

        return BCrypt.checkpw(raw, encrypted);
    }

}
