package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultMergeRequestManager;
import com.pmease.gitop.core.model.MergeRequest;

@ImplementedBy(DefaultMergeRequestManager.class)
public interface MergeRequestManager extends GenericDao<MergeRequest> {
}
