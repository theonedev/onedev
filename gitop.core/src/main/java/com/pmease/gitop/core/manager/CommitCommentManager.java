package com.pmease.gitop.core.manager;

import java.util.Map;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultCommitCommentManager;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;

@ImplementedBy(DefaultCommitCommentManager.class)
public interface CommitCommentManager extends GenericDao<CommitComment> {
	
	Map<String, Integer> getCommitCommentStats(Project project);
	
}
