package com.pmease.gitop.web.util;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
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
	
	public static boolean hasCommits(File gitDir) {
		if (gitDir.exists()) {
			return new File(gitDir, "objects").list().length > 2
					|| new File(gitDir, "objects/pack").list().length > 0;
		}
		
		return false;
	}
	
	public static @Nullable String getDefaultBranch(Git git) {
		String rev = git.parseRevision(Constants.MASTER, false);
		if (rev != null) {
			return Constants.MASTER;
		}
		
		// FIXME: here --heads is not a pattern
		List<RefInfo> refs = git.showRefs("--heads");
		if (refs.isEmpty()) {
			return null;
		} else {
			RefInfo ref = Iterables.getFirst(refs, null);
			return ref.getName().substring(Constants.R_HEADS.length());
		}
	}
	
	public static @Nullable Commit getLastCommit(Git git, String revision, String path) {
		List<Commit> commits = git.log(null, revision, path, 1);
		return Iterables.getFirst(commits, null);
	}
}
