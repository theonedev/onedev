package com.pmease.gitplex.core.manager;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultCommentVisitManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommentVisit;
import com.pmease.gitplex.core.model.Repository;

@ImplementedBy(DefaultCommentVisitManager.class)
public interface CommentVisitManager {
	
	/**
	 * Find comment visits by specified commit date range.
	 * 
	 * @param repository
	 * 			repository to find comment inside
	 * @param fromDate
	 * 			from date of associated commit
	 * @param toDate
	 * 			to date of associated commit
	 * @return
	 * 			list of found comment visits
	 */
	List<CommentVisit> findByCommitDates(Repository repository, @Nullable Date fromDate, @Nullable Date toDate);
	
	List<CommentVisit> findByCommitOrDates(Repository repository, String commit, 
			@Nullable Date fromDate, @Nullable Date toDate);

	CommentVisit find(Repository repository, String commit, @Nullable CommentPosition position);
}
