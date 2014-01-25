package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultPullRequestCommentManager;
import com.pmease.gitop.model.PullRequestComment;

@ImplementedBy(DefaultPullRequestCommentManager.class)
public interface PullRequestCommentManager extends GenericDao<PullRequestComment> {
}
