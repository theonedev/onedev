package com.pmease.commons.git;

import java.util.Date;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.util.SystemReader;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

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
	 * Parse the raw user information into PersonIdent object, the raw information
	 * should be in format <code>[name] [<email>] [epoch timezone]</code>, for 
	 * example:
	 * 
	 * Jacob Thornton <jacobthornton@gmail.com> 1328060294 -0800
	 * 
	 * @param raw
	 * @return
	 */
	public static @Nullable PersonIdent parsePersonIdent(String raw) {
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
		
		return newPersonIdent(name, email, when);
	}
}
