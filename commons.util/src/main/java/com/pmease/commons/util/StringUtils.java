package com.pmease.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.pmease.commons.bootstrap.BootstrapUtils;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
	private static final String ENCRYPTION_KEY = "123456789012345678901234567890";
	
    public static String encrypt(String string) {
    	if (string == null)
    		return null;
        try {
            KeySpec keySpec = new DESedeKeySpec(ENCRYPTION_KEY.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            Cipher cipher = Cipher.getInstance("DESede");
        	
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = cipher.doFinal(string.getBytes("UTF8"));
            return new String(Base64.encodeBase64(ciphertext));
        } catch (Exception e) {
        	throw BootstrapUtils.unchecked(e);
        }
    }

    public static String decrypt(String string) {
    	if (string == null)
    		return null;
        try {
            KeySpec keySpec = new DESedeKeySpec(ENCRYPTION_KEY.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            Cipher cipher = Cipher.getInstance("DESede");
        	
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(Base64.decodeBase64(string.getBytes()));
            return new String(bytes, "UTF8");
        } catch (Exception e) {
        	throw BootstrapUtils.unchecked(e);
        }
    }

    /**
     * Split specified string with specified separator and trim the result fields. 
     * @param str 
     * @param separator
     * @return 
     * 			Modifiable collection of splitted fields. Leading and trailing white spaces will be trimmed 
     * 			from these fields. Element of the resulting collection will never be null or 
     * 			empty string.   
     */
	public static Collection<String> splitAndTrim(String str, String separator) {
		Collection<String> fields = new ArrayList<String>();
		for (String each: StringUtils.split(str, separator)) {
			if (each != null && each.trim().length() != 0)
				fields.add(each.trim());
		}
		return fields;
	}

	/**
	 * Parse specified string into tokens. Content surrounded with &quot; character
	 * is considered as a single token. For example: echo "hello world" will be parsed 
	 * into two tokens, respectively [echo], and [hello world]. The quote character 
	 * itself can be quoted and escaped in order to return as ordinary character. For 
	 * example: echo "hello \" world" will be parsed into two tokens: [echo] and 
	 * [hello " world].
	 * @param string
	 * @return
	 */
    public static String[] parseQuoteTokens(String string) {
    	List<String> commandTokens = new ArrayList<String>();
    	
		StreamTokenizer st = new StreamTokenizer(
				new BufferedReader(new StringReader(string)));
		st.resetSyntax();
		st.wordChars(0, 255);
		st.ordinaryChar(' ');
		st.ordinaryChar('\n');
		st.ordinaryChar('\t');
		st.ordinaryChar('\r');
		st.quoteChar('"');
		
		try {
			String token = null;
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				if (st.ttype == '"' || st.ttype == StreamTokenizer.TT_WORD) {
					if (token == null)
						token = st.sval;
					else
						token += st.sval;
				} else if (token != null) {
					commandTokens.add(token);
					token = null;
				}
			}
			if (token != null)
				commandTokens.add(token);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return commandTokens.toArray(new String[commandTokens.size()]);
    }	
}
