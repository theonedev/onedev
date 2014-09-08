package com.pmease.commons.git;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.util.SystemReader;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class GitUtils {
	
	public static final String NULL_SHA1 = StringUtils.repeat("0", 40);
    
	public static final Pattern PATTERN_HASH = Pattern.compile("[a-z0-9]{40}");
    
    public static final int SHORT_SHA_LENGTH = 7;

    public static boolean isHash(String sha) {
    	return PATTERN_HASH.matcher(sha).matches();
    }
    
    public static boolean isNullHash(String sha) {
    	return Objects.equal(NULL_SHA1, sha);
    }
    
    public static String abbreviateSHA(String sha, int length) {
        Preconditions.checkArgument(isHash(sha));
        return sha.substring(0, length);
    }

	public static String abbreviateSHA(String sha) {
		return abbreviateSHA(sha, SHORT_SHA_LENGTH);
	}

	public static String getBlobTypeName(int blobType) {
		if (blobType == FileMode.TYPE_FILE)
			return "File";
		else if (blobType == FileMode.TYPE_GITLINK)
			return "Sub module";
		else if (blobType == FileMode.TYPE_SYMLINK)
			return "Symbol link";
		else 
			return "Folder";
	}

	public static PersonIdent newPersonIdent(String name, String email, Date when) {
		return new PersonIdent(name, email, when.getTime(), 
				SystemReader.getInstance().getTimezone(when.getTime()));
	}
	
}
