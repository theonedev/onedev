package com.pmease.commons.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
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
    
    public static final int SHORT_SHA_LENGTH = 10;
    
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

	private static ObjectId insertTree(RevTree revTree, TreeWalk treeWalk, ObjectInserter inserter, 
			String path, byte[] content) {
        try {
	        TreeFormatter formatter = new TreeFormatter();
	        boolean appended = false;
    		boolean found = false;
			while (treeWalk.next()) {
				String name = treeWalk.getNameString();
				if (name.equals(path)) {
					if (content != null) {
						ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, content);
						formatter.append(name, FileMode.REGULAR_FILE, blobId);
						appended = true;
					}
					found = true;
				} else if (path.startsWith(name + "/")) {
					TreeWalk subtreeWalk = TreeWalk.forPath(treeWalk.getObjectReader(), treeWalk.getPathString(), revTree);
					Preconditions.checkNotNull(subtreeWalk);
					subtreeWalk.enterSubtree();
					String subpath = path.substring(name.length()+1);
					ObjectId subtreeId = insertTree(revTree, subtreeWalk, inserter, subpath, content);
					if (subtreeId != null) { 
						formatter.append(name, treeWalk.getFileMode(0), subtreeId);
						appended = true;
					}
					if (subtreeId == null || !subtreeId.equals(treeWalk.getObjectId(0)))
						found = true;
				} else {
					formatter.append(name, treeWalk.getFileMode(0), treeWalk.getObjectId(0));
					appended = true;
				}
			}
			if (!found) {
				if (content == null)
					throw new ObjectNotFoundException("Unable to find blob: " + path);
				List<String> splitted = Splitter.on('/').splitToList(path);
				
				ObjectId childId = null;
				FileMode childMode = null;
				String childName = null;
				
				for (int i=splitted.size()-1; i>=0; i--) {
					if (childId == null) {
						childName = splitted.get(i);
						childId = inserter.insert(Constants.OBJ_BLOB, content);
						childMode = FileMode.REGULAR_FILE;
					} else {
						TreeFormatter childFormatter = new TreeFormatter();
						childFormatter.append(childName, childMode, childId);
						childName = splitted.get(i);
						childId = inserter.insert(childFormatter);
						childMode = FileMode.TREE;
					}
				}

				Preconditions.checkState(childId!=null && childMode != null && childName != null);
				formatter.append(childName, childMode, childId);
				appended = true;
			}
			if (appended)
				return inserter.insert(formatter);
			else
				return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Commit specified file into specified repository.
	 * 
	 * @param repo 
	 * 			repository to make the new commit
	 * @param refName
	 * 			ref name to associate the new commit with
	 * @param expectedOldCommitId
	 * 			expected old commit id of above ref, use <tt>null</tt> to expect a non-existent ref
	 * @param parentCommitId
	 * 			parent commit id of the new commit
	 * @param authorAndCommitter
	 * 			author and committer person ident for the new commit
	 * @param commitMessage
	 * 			commit message for the new commit
	 * @param path
	 * 			path of the file
	 * @param content
	 * 			content of the file, use <tt>null</tt> to delete the file
	 * @return 
	 * 			id of new commit
	 * @throws 
	 * 			ObsoleteOldCommitException if expected old commit id of the ref does not equal to 
	 * 			expectedOldCommitId, or if expectedOldCommitId is specified as <tt>null</tt> and 
	 * 			ref exists  
	 * 			 
	 */
	public static ObjectId commitFile(Repository repo, String refName, 
			@Nullable ObjectId expectedOldCommitId, ObjectId parentCommitId, 
			PersonIdent authorAndCommitter, String commitMessage, 
			String path, byte[] content) {
		try (	RevWalk revWalk = new RevWalk(repo); 
				TreeWalk treeWalk = new TreeWalk(repo);
				ObjectInserter inserter = repo.newObjectInserter();) {

			path = normalizePath(path);
			
			RevTree revTree = revWalk.parseCommit(parentCommitId).getTree();
			treeWalk.addTree(revTree);
	        CommitBuilder commit = new CommitBuilder();
	        commit.setTreeId(insertTree(revTree, treeWalk, inserter, path, content));
	        commit.setAuthor(authorAndCommitter);
	        commit.setCommitter(authorAndCommitter);
	        commit.setParentId(parentCommitId);
	        commit.setMessage(commitMessage);
	        
	        ObjectId commitId = inserter.insert(commit);
	        inserter.flush();
	        
	        RefUpdate ru = repo.updateRef(refName);
	        ru.setRefLogIdent(authorAndCommitter);
	        ru.setNewObjectId(commitId);
	        ru.setExpectedOldObjectId(expectedOldCommitId);
	        RefUpdate.Result result = ru.update();
	        if (result == RefUpdate.Result.LOCK_FAILURE 
	        		&& !Objects.equal(expectedOldCommitId, ru.getOldObjectId())) {
	        	throw new ObsoleteOldCommitException(ru.getOldObjectId());
	        } else if (result != RefUpdate.Result.FAST_FORWARD) {
	        	throw new RefUpdateException(result);
	        } else {
	        	return commitId;
	        }
		} catch (RevisionSyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
