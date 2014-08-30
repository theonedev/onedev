package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.CommitCommentManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultCommitCommentManager implements CommitCommentManager {

	private final Dao dao;
	
	@Inject
	public DefaultCommitCommentManager(Dao dao) {
		this.dao = dao;
	}

	@Sessional
	@Override
	public List<CommitComment> findByFile(Repository repository, String file) {
		return dao.query(EntityCriteria.of(CommitComment.class)
				.add(Restrictions.eq("repository", repository))
				.add(Restrictions.eq("position.filePath", file))
				.addOrder(Order.asc("id")), 0, 0);
	}

	@Sessional
	@Override
	public List<CommitComment> findByCommit(Repository repository, String commit) {
		return dao.query(EntityCriteria.of(CommitComment.class)
				.add(Restrictions.eq("repository", repository))
				.add(Restrictions.eq("commit", commit))
				.addOrder(Order.asc("id")), 0, 0);
	}

	@Override
	public List<CommitComment> findByCommitAndFile(Repository repository, String commit, String filePath) {
		return dao.query(EntityCriteria.of(CommitComment.class)
				.add(Restrictions.eq("repository", repository))
				.add(Restrictions.eq("commit", commit))
				.add(Restrictions.eq("position.filePath", filePath))
				.addOrder(Order.asc("id")), 0, 0);
	}

	@Override
	public List<CommitComment> findByCommitDates(Repository repository, Date fromDate, Date toDate) {
		EntityCriteria<CommitComment> criteria = EntityCriteria.of(CommitComment.class);
		criteria.add(Restrictions.eq("repository", repository));
		if (fromDate != null)
			criteria.add(Restrictions.ge("commitDate", fromDate));
		if (toDate != null)
			criteria.add(Restrictions.le("commitDate", toDate));
		criteria.addOrder(Order.asc("id"));
		return dao.query(criteria, 0, 0);
	}

	@Override
	public List<CommitComment> findByCommitOrDates(Repository repository,
			String commit, Date fromDate, Date toDate) {
		EntityCriteria<CommitComment> criteria = EntityCriteria.of(CommitComment.class);
		criteria.add(Restrictions.eq("repository", repository));
		Conjunction conjunction = Restrictions.conjunction();
		if (fromDate != null)
			conjunction.add(Restrictions.ge("commitDate", fromDate));
		if (toDate != null)
			conjunction.add(Restrictions.le("commitDate", toDate));
		criteria.add(Restrictions.or(Restrictions.eq("commit", commit), conjunction));
		criteria.addOrder(Order.asc("id"));
		return dao.query(criteria, 0, 0);
	}

	@Override
	public Map<Integer, List<CommitComment>> findLineComments(Repository repository, 
			String commit, String filePath) {
		Map<Integer, List<CommitComment>> comments = new HashMap<>();
		CommitCommentManager manager = GitPlex.getInstance(CommitCommentManager.class);
		for (CommitComment comment: manager.findByCommitAndFile(repository, commit, filePath)) {
			CommentPosition position = comment.getPosition();
			if (position.getLineNo() != null && position.getFilePath().equals(filePath)) {
				List<CommitComment> lineComments = comments.get(position.getLineNo());
				if (lineComments == null) {
					lineComments = new ArrayList<>();
					comments.put(position.getLineNo(), lineComments);
				}
				lineComments.add(comment);
			}
		}
		return comments;
	}
}
