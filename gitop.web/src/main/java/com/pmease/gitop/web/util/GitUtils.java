package com.pmease.gitop.web.util;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;

public class GitUtils {

	private GitUtils() {}
	
	public static final String NULL_SHA1 = StringUtils.repeat("0", 40);
    public static final Pattern PATTERN_HASH = Pattern.compile("[a-z0-9]{40}");
    
    public static final int SHORT_SHA_LENGTH = 10;

    public static boolean isHash(String sha) {
    	return PATTERN_HASH.matcher(sha).matches();
    }
    
	public static String getShortSha(String sha) {
		Preconditions.checkArgument(isHash(sha));
		return sha.substring(0, SHORT_SHA_LENGTH);
	}
	
	public static String getCommitSummary(Commit commit) {
		return commit.getSubject();
	}
	
	public static boolean hasCommits(File gitDir) {
		if (gitDir.exists()) {
			return new File(gitDir, "objects").list().length > 2
					|| new File(gitDir, "objects/pack").list().length > 0;
		}
		
		return false;
	}
}
