package com.pmease.gitplex.core.comment;

import java.util.List;

import com.pmease.gitplex.core.model.CommitComment;

public interface CommentLoader {
	/**
	 * Load list of comments on specified commit, ordered by comment id.
	 * 
	 * @param commit
	 * 			commit to load comments for
	 * @return
	 * 			list of commit comments on specified commit ordered by comment id, 
	 * 			including comments on commit itself, comments on files in the commit, 
	 * 			and comments on file lines in the commit. Empty list should be 
	 * 			returned if there are no any comments on specified commit
	 */
	List<CommitComment> loadComments(String commit);
}
