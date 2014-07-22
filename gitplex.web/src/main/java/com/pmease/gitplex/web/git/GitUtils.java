package com.pmease.gitplex.web.git;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;

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
	
	public static @Nullable Commit getLastCommit(Git git, String revision, String path) {
		List<Commit> commits = git.log(null, revision, path, 1, 0);
		return Iterables.getFirst(commits, null);
	}
	
	public static @Nullable String parseEmail(String mail) {
		if (mail == null || mail.length() == 0)
			return null;
		
		if (mail.charAt(0) == '<')
			mail = mail.substring(1, mail.length() - 1);
		
		return mail;
	}
	
	/**
	 * Parse the original git raw date to Java date. The raw git date is in
	 * unix timestamp with timezone like:
	 * 1392312299 -0800
	 * 
	 * @param input the input raw date string
	 * @return Java date
	 */
	public static Date parseRawDate(String input) {
		String[] pieces = Iterables.toArray(Splitter.on(" ").split(input), String.class);
		return new Date(Long.valueOf(pieces[0]) * 1000L);
	}
	
	/**
	 * Parse the raw user information into UserInfo object, the raw information
	 * should be in format <code>[name] [<email>] [epoch timezone]</code>, for 
	 * example:
	 * 
	 * Jacob Thornton <jacobthornton@gmail.com> 1328060294 -0800
	 * 
	 * @param raw
	 * @return
	 */
	public static @Nullable PersonIdent parseGitContrib(String raw) {
		if (Strings.isNullOrEmpty(raw))
			return null;
		
		int pos1 = raw.indexOf('<');
		if (pos1 <= 0)
			throw new IllegalArgumentException("Raw " + raw);
		
		String name = raw.substring(0, pos1 - 1);
		
		int pos2 = raw.indexOf('>');
		if (pos2 <= 0)
			throw new IllegalArgumentException("Raw " + raw);
		
		String time = raw.substring(pos2 + 1).trim();
		Date when = parseRawDate(time);
		
		String email = raw.substring(pos1 + 1, pos2 - 1);
		
		return Git.newPersonIdent(name, email, when);
	}
	
}
