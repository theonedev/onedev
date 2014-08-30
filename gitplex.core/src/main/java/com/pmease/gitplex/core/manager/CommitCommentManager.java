package com.pmease.gitplex.core.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultCommitCommentManager;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.Repository;

@ImplementedBy(DefaultCommitCommentManager.class)
public interface CommitCommentManager {
	
	/**
	 * Find comments by commit.
	 * 
	 * @param repository
	 * 			repository to find comment inside
	 * @param commit
	 * 			commit to find comment for
	 * @return
	 * 			list of comments ordered by id
	 */
	List<CommitComment> findByCommit(Repository repository, String commit);

	/**
	 * Find comments by file.
	 * 
	 * @param repository
	 * 			repository to find comment inside
	 * @param commit
	 * 			file to find comment for
	 * @return
	 * 			list of comments ordered by id
	 */
	List<CommitComment> findByFile(Repository repository, String file);
	
	/**
	 * Find comments by commit and file.
	 * 
	 * @param repository
	 * 			repository to find comment inside
	 * @param commit
	 * 			commit to find comment for
	 * @param file
	 * 			file to find comment for
	 * @return
	 * 			list of comments ordered by id
	 */
	List<CommitComment> findByCommitAndFile(Repository repository, String commit, String file);
	
	/**
	 * Find comments by specified commit date range.
	 * 
	 * @param repository
	 * 			repository to find comment inside
	 * @param fromDate
	 * 			from date of associated commit
	 * @param toDate
	 * 			to date of associated commit
	 * @return
	 * 			list of found comments ordered by id
	 */
	List<CommitComment> findByCommitDates(Repository repository, @Nullable Date fromDate, @Nullable Date toDate);
	
	List<CommitComment> findByCommitOrDates(Repository repository, String commit, 
			@Nullable Date fromDate, @Nullable Date toDate);

	Map<Integer, List<CommitComment>> findLineComments(Repository repository, String commit, String filePath);
}
