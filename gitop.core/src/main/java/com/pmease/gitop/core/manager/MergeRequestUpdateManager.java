package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultMergeRequestUpdateManager;
import com.pmease.gitop.core.model.MergeRequestUpdate;

@ImplementedBy(DefaultMergeRequestUpdateManager.class)
public interface MergeRequestUpdateManager extends GenericDao<MergeRequestUpdate> {
}
