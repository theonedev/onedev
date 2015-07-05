package com.pmease.commons.git;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.pmease.commons.git.exception.NotFileException;
import com.pmease.commons.git.exception.NotTreeException;
import com.pmease.commons.git.exception.ObjectAlreadyExistException;
import com.pmease.commons.git.exception.ObjectNotExistException;
import com.pmease.commons.git.exception.ObsoleteCommitException;
import com.pmease.commons.git.exception.RefUpdateException;

public class FileEdit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String oldPath;
	
	private final String newPath;
	
	private final byte[] content;
	
	public FileEdit(@Nullable String oldPath, @Nullable String newPath, byte[] content) {
		this.oldPath = GitUtils.normalizePath(oldPath);
		this.newPath= GitUtils.normalizePath(newPath);
		this.content = content;

		Preconditions.checkArgument(this.oldPath != null || this.newPath != null, 
				"Either oldPath or newPath should be defined");
	}

	public String getOldPath() {
		return oldPath;
	}

	public String getNewPath() {
		return newPath;
	}

	public byte[] getContent() {
		return content;
	}

	private ObjectId insertTree(RevTree revTree, TreeWalk treeWalk, ObjectInserter inserter, 
			@Nullable String currentOldPath, @Nullable String currentNewPath) {
        try {
	        TreeFormatter formatter = new TreeFormatter();
	        boolean appended = false;
    		boolean oldPathFound = false;
    		boolean newPathFound = false;
			while (treeWalk.next()) {
				String name = treeWalk.getNameString();
				if (name.equals(currentOldPath)) {
					if ((treeWalk.getFileMode(0).getBits() & FileMode.TYPE_FILE) == 0)
						throw new NotFileException("Path does not represent a file: " + treeWalk.getPathString());
					oldPathFound = true;
					if (name.equals(currentNewPath)) {
						newPathFound = true;
						ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, content);
						formatter.append(name, FileMode.REGULAR_FILE, blobId);
						appended = true;
					}
				} else if (name.equals(currentNewPath)) {
					throw new ObjectAlreadyExistException("Path already exist: " + treeWalk.getPathString());
				} else if (currentOldPath != null && currentOldPath.startsWith(name + "/")) {
					oldPathFound = true;
					TreeWalk childTreeWalk = TreeWalk.forPath(treeWalk.getObjectReader(), treeWalk.getPathString(), revTree);
					Preconditions.checkNotNull(childTreeWalk);
					childTreeWalk.enterSubtree();
					String childOldPath = currentOldPath.substring(name.length()+1);
					String childNewPath;
					if (currentNewPath != null && currentNewPath.startsWith(name + "/")) {
						childNewPath = currentNewPath.substring(name.length()+1);
						newPathFound = true;
					} else {
						childNewPath = null;
					}
					ObjectId childTreeId = insertTree(revTree, childTreeWalk, inserter, 
							childOldPath, childNewPath);
					if (childTreeId != null) { 
						formatter.append(name, FileMode.TREE, childTreeId);
						appended = true;
					}
				} else if (currentNewPath != null && currentNewPath.startsWith(name + "/")) {
					if ((treeWalk.getFileMode(0).getBits() & FileMode.TYPE_TREE) == 0)
						throw new NotTreeException("Path does not represent a tree: " + treeWalk.getPathString());
					newPathFound = true;
					TreeWalk childTreeWalk = TreeWalk.forPath(treeWalk.getObjectReader(), 
							treeWalk.getPathString(), revTree);
					Preconditions.checkNotNull(childTreeWalk);
					childTreeWalk.enterSubtree();
					String childNewPath = currentNewPath.substring(name.length()+1);
					ObjectId childTreeId = insertTree(revTree, childTreeWalk, inserter, null, childNewPath);
					if (childTreeId != null) { 
						formatter.append(name, treeWalk.getFileMode(0), childTreeId);
						appended = true;
					}
				} else {
					formatter.append(name, treeWalk.getFileMode(0), treeWalk.getObjectId(0));
					appended = true;
				}
			}
			
			if (currentOldPath != null && !oldPathFound) 
				throw new ObjectNotExistException("Path not exist: " + oldPath);
			
			if (currentNewPath != null && !newPathFound) {
				List<String> splitted = Splitter.on('/').splitToList(currentNewPath);
				
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
	 * @return 
	 * 			id of new commit
	 * @throws ObsoleteCommitException 
	 * 			if expected old commit id of the ref does not equal to 
	 * 			expectedOldCommitId, or if expectedOldCommitId is specified as <tt>null</tt> and 
	 * 			ref exists  
	 * @throws ObjectNotExistException 
	 * 			if file to delete does not exist when oldPath!=null&&newFile==null 
	 * @throws ObjectAlreadyExistException 
	 * 			if added/renamed file already exists when newFile!=null && (oldPath==null || !oldPath.equals(newFile.getPath()))
	 * 			 
	 */
	public ObjectId commit(Repository repo, String refName, 
			@Nullable ObjectId expectedOldCommitId, ObjectId parentCommitId, 
			PersonIdent authorAndCommitter, String commitMessage) {
		
		try (	RevWalk revWalk = new RevWalk(repo); 
				TreeWalk treeWalk = new TreeWalk(repo);
				ObjectInserter inserter = repo.newObjectInserter();) {

			RevTree revTree = revWalk.parseCommit(parentCommitId).getTree();
			treeWalk.addTree(revTree);
	        CommitBuilder commit = new CommitBuilder();
	        
	        commit.setTreeId(insertTree(revTree, treeWalk, inserter, oldPath, newPath));
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
	        	throw new ObsoleteCommitException(ru.getOldObjectId());
	        } else if (result != RefUpdate.Result.FAST_FORWARD) {
	        	throw new RefUpdateException(result);
	        } else {
	        	return commitId;
	        }
		} catch (RevisionSyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class File implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String path;
		
		private final byte[] content;
		
		public File(String path, byte[] content) {
			this.path = Preconditions.checkNotNull(GitUtils.normalizePath(path));
			this.content = content;
		}

		public String getPath() {
			return path;
		}

		public byte[] getContent() {
			return content;
		}
		
	}
}
