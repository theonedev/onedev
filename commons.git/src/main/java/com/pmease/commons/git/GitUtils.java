package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.SystemReader;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class GitUtils {
	
	public static final String NULL_SHA1 = StringUtils.repeat("0", 40);
    
	public static final Pattern PATTERN_HASH = Pattern.compile("[a-z0-9]{40}");
    
    public static final int SHORT_SHA_LENGTH = 8;
    
    public static boolean isHash(String sha) {
    	return PATTERN_HASH.matcher(sha).matches();
    }
    
    public static boolean isNullHash(String sha) {
    	return Objects.equal(NULL_SHA1, sha);
    }
    
    public static boolean isEmptyPath(String path) {
    	return Strings.isNullOrEmpty(path) || Objects.equal(path, DiffEntry.DEV_NULL);
    }
    
    public static String abbreviateSHA(String sha, int length) {
        Preconditions.checkArgument(isHash(sha));
        return sha.substring(0, length);
    }

	public static String abbreviateSHA(String sha) {
		return abbreviateSHA(sha, SHORT_SHA_LENGTH);
	}
	
	public static String getShortMessage(RevCommit commit) {
		return StringUtils.substringBefore(commit.getFullMessage(), "\n").trim();
	}

	@Nullable
	public static String getDetailMessage(RevCommit commit) {
		String body = StringUtils.substringAfter(commit.getFullMessage(), "\n").trim();
		return body.length()!=0?body:null;
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
	
	public static int comparePath(@Nullable String path1, @Nullable String path2) {
		List<String> segments1 = splitPath(path1);
		List<String> segments2 = splitPath(path2);
	
		int index = 0;
		for (String segment1: segments1) {
			if (index<segments2.size()) {
				int result = segment1.compareTo(segments2.get(index));
				if (result != 0)
					return result;
			} else {
				return 1;
			}
			index++;
		}
		if (index<segments2.size())
			return -1;
		else
			return 0;
	}

	public static List<String> splitPath(@Nullable String path) {
		List<String> pathElements = new ArrayList<>();
		if (path != null) {
			for (String element: Splitter.on("/").split(path)) {
				if (element.length() != 0)
					pathElements.add(element);
			}
		}
		return pathElements;
	}

	public static @Nullable String joinPath(List<String> pathSegments) {
		List<String> nonEmptyElements = new ArrayList<>();
		for (String element: pathSegments){
			if (element.length() != 0)
				nonEmptyElements.add(element);
		}
		if (!nonEmptyElements.isEmpty()) {
			return Joiner.on("/").join(nonEmptyElements);
		} else {
			return null;
		}
	}

	public static @Nullable String normalizePath(@Nullable String path) {
		return joinPath(splitPath(path));
	}

	/**
     * Convert a git reference name to branch name.
     * 
     * @param refName
     *			name of the git reference 	
     * @return
     * 			name of the branch, or <tt>null</tt> if specified ref
     * 			does not represent a branch
     */ 
    public static @Nullable String ref2branch(String refName) {
		if (refName.startsWith(Constants.R_HEADS)) 
			return refName.substring(Constants.R_HEADS.length());
		else
			return null;
    }
    
    public static String branch2ref(String branch) {
    	return Constants.R_HEADS + branch; 
    }    

	/**
     * Convert a git reference name to tag name.
     * 
     * @param refName
     *			name of the git reference 	
     * @return
     * 			name of the tag, or <tt>null</tt> if specified ref
     * 			does not represent a tag
     */ 
    public static @Nullable String ref2tag(String refName) {
		if (refName.startsWith(Constants.R_TAGS)) 
			return refName.substring(Constants.R_TAGS.length());
		else
			return null;
    }
    
    public static String tag2ref(String tag) {
    	return Constants.R_TAGS + tag; 
    }    
    
    public static BlobIdent getOldBlobIdent(DiffEntry diffEntry, String oldRev) {
    	BlobIdent blobIdent;
		if (diffEntry.getChangeType() != ChangeType.ADD) {
			blobIdent = new BlobIdent(oldRev, diffEntry.getOldPath(), diffEntry.getOldMode().getBits());
			AnyObjectId id = diffEntry.getOldId().toObjectId();
			blobIdent.id = id!=null?id.name():null;
		} else {
			blobIdent = new BlobIdent(oldRev, null, null);
		}
		return blobIdent;
    }
    
    public static BlobIdent getNewBlobIdent(DiffEntry diffEntry, String newRev) {
    	BlobIdent blobIdent;
		if (diffEntry.getChangeType() != ChangeType.DELETE) {
			blobIdent = new BlobIdent(newRev, diffEntry.getNewPath(), diffEntry.getNewMode().getBits());
			AnyObjectId id = diffEntry.getNewId().toObjectId();
			blobIdent.id = id!=null?id.name():null;
		} else {
			blobIdent = new BlobIdent(newRev, null, null);
		}
		return blobIdent;
    }
    
}
