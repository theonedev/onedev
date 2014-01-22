package com.pmease.gitop.web.git;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.RefInfo;

public class GitUtils {

	private GitUtils() {}
	
	public static final String NULL_SHA1 = StringUtils.repeat("0", 40);
    public static final Pattern PATTERN_HASH = Pattern.compile("[a-z0-9]{40}");
    
    public static final int SHORT_SHA_LENGTH = 10;

    public static boolean isHash(String sha) {
    	return PATTERN_HASH.matcher(sha).matches();
    }
    
    public static boolean isNullHash(String sha) {
    	return Strings.isNullOrEmpty(sha) || Objects.equal(NULL_SHA1, sha);
    }
    
    public static boolean isEmptyPath(String path) {
    	return Strings.isNullOrEmpty(path) || 
    			Objects.equal(path, DiffEntry.DEV_NULL);
    }
    
    public static String abbreviateSHA(String sha, int length) {
        Preconditions.checkArgument(isHash(sha));
        return sha.substring(0, length);
    }

	public static String abbreviateSHA(String sha) {
		return abbreviateSHA(sha, SHORT_SHA_LENGTH);
	}
	
	public static String getCommitSummary(Commit commit) {
		return commit.getSubject();
	}
	
	public static @Nullable String getDefaultBranch(Git git) {
		String defaultBranch = git.resolveDefaultBranch();
		
		// FIXME: here --heads is not a pattern
		List<RefInfo> refs = git.showRefs("--heads");
		if (refs.isEmpty()) {
			return defaultBranch;
		} else {
			for (RefInfo each : refs) {
				if (each.getName().substring(Constants.R_HEADS.length()).equals(defaultBranch)) {
					return defaultBranch;
				}
			}
			
			RefInfo ref = Iterables.getFirst(refs, null);
			return ref.getName().substring(Constants.R_HEADS.length());
		}
	}
	
	public static @Nullable Commit getLastCommit(Git git, String revision, String path) {
		List<Commit> commits = git.log(null, revision, path, 1, 0);
		return Iterables.getFirst(commits, null);
	}
}
