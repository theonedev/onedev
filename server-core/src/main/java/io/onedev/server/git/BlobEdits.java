package io.onedev.server.git;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.server.git.exception.BlobEditException;
import io.onedev.server.git.exception.NotTreeException;
import io.onedev.server.git.exception.ObjectAlreadyExistsException;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.git.exception.ObsoleteCommitException;

public class BlobEdits implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Set<String> oldPaths;
	
	private final Map<String, BlobContent> newBlobs;
	
	public BlobEdits(Set<String> oldPaths, Map<String, BlobContent> newBlobs) {
		this.oldPaths = new HashSet<>();
		for (String oldPath: oldPaths) {
			String normalizedPath = GitUtils.normalizePath(oldPath);
			if (normalizedPath != null)
				this.oldPaths.add(normalizedPath);
			else
				throw new BlobEditException("Invalid old path: " + oldPath);
		}
		this.newBlobs = new HashMap<>();
		for (Map.Entry<String, BlobContent> entry: newBlobs.entrySet()) { 
			String normalizedPath = GitUtils.normalizePath(entry.getKey());
			if (normalizedPath != null)
				this.newBlobs.put(normalizedPath, entry.getValue());
			else
				throw new BlobEditException("Invalid new path: " + entry.getKey());
		}
	}

	public Set<String> getOldPaths() {
		return oldPaths;
	}

	public Map<String, BlobContent> getNewBlobs() {
		return newBlobs;
	}

	private ObjectId insertTree(RevTree revTree, TreeWalk treeWalk, ObjectInserter inserter, 
			String parentPath, Set<String> currentOldPaths, Map<String, BlobContent> currentNewBlobs) {
        try {
    		List<TreeFormatterEntry> entries = new ArrayList<>();
    		while (revTree != null && treeWalk.next()) {
				String name = treeWalk.getNameString();
				if (currentOldPaths.contains(name)) {
					currentOldPaths.remove(name);
					BlobContent currentNewBlob = currentNewBlobs.remove(name);
					if (currentNewBlob != null) {
						ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, currentNewBlob.getBytes());
						entries.add(new TreeFormatterEntry(name, currentNewBlob.getMode(), blobId));
					}
				} else if (currentNewBlobs.containsKey(name)) {
					if ((treeWalk.getRawMode(0) & FileMode.TYPE_MASK) == FileMode.TYPE_TREE) {
						throw new ObjectAlreadyExistsException("Path already exist: " + treeWalk.getPathString());
					} else {
						BlobContent currentNewBlob = currentNewBlobs.remove(name);
						ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, currentNewBlob.getBytes());
						entries.add(new TreeFormatterEntry(name, currentNewBlob.getMode(), blobId));
					}
				} else {
					Set<String> childOldPaths = new HashSet<>();
					for (Iterator<String> it = currentOldPaths.iterator(); it.hasNext();) {
						String currentOldPath = it.next();
						if (currentOldPath.startsWith(name + "/")) {
							childOldPaths.add(currentOldPath.substring(name.length()+1));
							it.remove();
						}
					}
					Map<String, BlobContent> childNewBlobs = new HashMap<>();
					for (Iterator<Map.Entry<String, BlobContent>> it = currentNewBlobs.entrySet().iterator(); 
							it.hasNext();) {
						Map.Entry<String, BlobContent> entry = it.next();
						if (entry.getKey().startsWith(name +"/")) {
							childNewBlobs.put(entry.getKey().substring(name.length()+1), entry.getValue());
							it.remove();
						}
					}
					if (!childOldPaths.isEmpty() || !childNewBlobs.isEmpty()) {
		    			if ((treeWalk.getFileMode(0).getBits() & FileMode.TYPE_TREE) != 0) {
							TreeWalk childTreeWalk = TreeWalk.forPath(treeWalk.getObjectReader(), treeWalk.getPathString(), 
									revTree);
							Preconditions.checkNotNull(childTreeWalk);
							childTreeWalk.enterSubtree();
							ObjectId childTreeId = insertTree(revTree, childTreeWalk, inserter, treeWalk.getPathString(), 
									childOldPaths, childNewBlobs);
							if (childTreeId != null) 
								entries.add(new TreeFormatterEntry(name, FileMode.TREE, childTreeId));
		    			} else {
							throw new NotTreeException("Path does not represent a tree: " + treeWalk.getPathString());
		    			}
					} else {
						entries.add(new TreeFormatterEntry(name, treeWalk.getFileMode(0), treeWalk.getObjectId(0)));
					}
				} 
			}
			
    		if (!currentOldPaths.isEmpty()) {
    			String nonExistPath = currentOldPaths.iterator().next();
    			if (parentPath != null)
    				nonExistPath = parentPath + "/" + nonExistPath;
				throw new ObjectNotFoundException("Unable to find path " + nonExistPath);
    		}
    		
			if (!currentNewBlobs.isEmpty()) {
				Set<String> files = new HashSet<>();
				for (Map.Entry<String, BlobContent> entry: currentNewBlobs.entrySet()) {
					String path = entry.getKey();
					if (!path.contains("/")) {
						files.add(path);
						entries.add(new TreeFormatterEntry(path, entry.getValue().getMode(), 
								inserter.insert(Constants.OBJ_BLOB, entry.getValue().getBytes())));
						files.add(path);
					}
				}				
				Set<String> topLevelPathSegments = new LinkedHashSet<>();
				for (String path: currentNewBlobs.keySet()) {
					if (path.contains("/")) {
						String topLevelPathSegment = StringUtils.substringBefore(path, "/");
						if (files.contains(topLevelPathSegment)) {
							String blobPath = topLevelPathSegment;
							if (parentPath != null)
								blobPath = parentPath + "/" + path;
							throw new ObjectAlreadyExistsException("Overlapped blob path: " + blobPath);
						} else {
							topLevelPathSegments.add(topLevelPathSegment);
						}
					}
				}
				for (String topLevelPathSegment: topLevelPathSegments) {
					Map<String, BlobContent> childNewBlobs = new HashMap<>();
					for (Map.Entry<String, BlobContent> entry: currentNewBlobs.entrySet()) {
						String path = entry.getKey();
						if (path.startsWith(topLevelPathSegment + "/"))
							childNewBlobs.put(path.substring(topLevelPathSegment.length()+1), entry.getValue());
					}				
					if (parentPath == null)
						parentPath = topLevelPathSegment;
					else
						parentPath += "/" + topLevelPathSegment;
					ObjectId childTreeId = insertTree(revTree, treeWalk, inserter, parentPath, 
							Sets.newHashSet(), childNewBlobs);
					if (childTreeId != null) 
						entries.add(new TreeFormatterEntry(topLevelPathSegment, FileMode.TREE, childTreeId));
				}
			}
			if (!entries.isEmpty()) {
				TreeFormatter formatter = new TreeFormatter();
				Collections.sort(entries);
				for (TreeFormatterEntry entry: entries)
					formatter.append(entry.name, entry.mode, entry.id);
				return inserter.insert(formatter);
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Commit specified file into specified repository.
	 * 
	 * @param repository 
	 * 			repository to make the new commit
	 * @param refName
	 * 			ref name to associate the new commit with
	 * @param expectedOldCommitId
	 * 			expected old commit id of above ref
	 * @param parentCommitId
	 * 			parent commit id of the new commit
	 * @param authorAndCommitter
	 * 			author and committer person ident for the new commit
	 * @param commitMessage
	 * 			commit message for the new commit
	 * @return 
	 * 			id of new commit
	 * @throws ObsoleteCommitException 
	 * 			if expected old commit id of the ref does not equal to 
	 * 			expectedOldCommitId, or if expectedOldCommitId is specified as <tt>null</tt> and 
	 * 			ref exists  
	 * @throws ObjectNotFoundException 
	 * 			if file to delete does not exist when oldPath!=null&&newFile==null 
	 * @throws ObjectAlreadyExistsException 
	 * 			if added/renamed file already exists when newFile!=null && (oldPath==null || !oldPath.equals(newFile.getPath()))
	 * 			 
	 */
	public ObjectId commit(Repository repository, String refName, ObjectId expectedOldCommitId, ObjectId parentCommitId, 
			PersonIdent authorAndCommitter, String commitMessage) {
		
		try (	RevWalk revWalk = new RevWalk(repository); 
				TreeWalk treeWalk = new TreeWalk(repository);
				ObjectInserter inserter = repository.newObjectInserter();) {

	        CommitBuilder commit = new CommitBuilder();
	        
	        commit.setAuthor(authorAndCommitter);
	        commit.setCommitter(authorAndCommitter);
	        commit.setMessage(commitMessage);

			RevTree revTree;
	        if (!parentCommitId.equals(ObjectId.zeroId())) {
	        	commit.setParentId(parentCommitId);
				revTree = revWalk.parseCommit(parentCommitId).getTree();
				treeWalk.addTree(revTree);
	        } else {
				revTree = null;
	        }

			ObjectId treeId = insertTree(revTree, treeWalk, inserter, null, new HashSet<>(oldPaths), 
					new HashMap<>(newBlobs));
	        
	        if (treeId != null)
	        	commit.setTreeId(treeId);
	        else 
	        	commit.setTreeId(inserter.insert(new TreeFormatter()));
	        
	        ObjectId commitId = inserter.insert(commit);
	        inserter.flush();
	        RefUpdate ru = repository.updateRef(refName);
	        ru.setRefLogIdent(authorAndCommitter);
	        ru.setNewObjectId(commitId);
	        ru.setExpectedOldObjectId(expectedOldCommitId);
	        GitUtils.updateRef(ru);
	        
	        return commitId;
		} catch (RevisionSyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class TreeFormatterEntry implements Comparable<TreeFormatterEntry> {

		final String name;
		
		final FileMode mode;
		
		final ObjectId id;
		
		final byte[] nameBytes;
		
		public TreeFormatterEntry(String name, FileMode mode, ObjectId id) {
			this.name = name;
			this.mode = mode;
			this.id = id;
			if (mode.equals(FileMode.TYPE_TREE))
				nameBytes = (name + "/").getBytes(StandardCharsets.UTF_8);
			else
				nameBytes = name.getBytes(StandardCharsets.UTF_8);
		}

		@Override
		public int compareTo(TreeFormatterEntry o) {
			for (int i = 0; i<nameBytes.length && i<o.nameBytes.length; i++) {
				final int cmp = (nameBytes[i] & 0xff) - (o.nameBytes[i] & 0xff);
				if (cmp != 0)
					return cmp;
			}
			return nameBytes.length - o.nameBytes.length;
		}

	}
}
