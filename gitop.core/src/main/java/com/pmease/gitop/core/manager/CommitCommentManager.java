package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultCommitCommentManager;
import com.pmease.gitop.model.CommitComment;

@ImplementedBy(DefaultCommitCommentManager.class)
public interface CommitCommentManager extends GenericDao<CommitComment> {
}
