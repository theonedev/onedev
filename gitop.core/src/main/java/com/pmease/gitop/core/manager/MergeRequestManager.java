package com.pmease.gitop.core.manager;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultMergeRequestManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

@ImplementedBy(DefaultMergeRequestManager.class)
public interface MergeRequestManager extends GenericDao<MergeRequest> {
    
    @Nullable MergeRequest findOpened(Branch target, @Nullable Branch source, User user);
    
}
