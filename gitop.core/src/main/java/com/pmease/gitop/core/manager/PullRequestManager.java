package com.pmease.gitop.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultPullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;

@ImplementedBy(DefaultPullRequestManager.class)
public interface PullRequestManager extends GenericDao<PullRequest> {
    
    @Nullable PullRequest findOpen(Branch target, Branch source);
    
    void refresh(PullRequest request);
    
    boolean merge(PullRequest request);
    
    void decline(PullRequest request);
    
    void reopen(PullRequest request);
    
    /**
     * Find pull requests whose head commit or merge commit equals to specified commit.
     * 
     * @param commit
     * 			head commit or merge commit to match
     * @return
     * 			collection of matching pull requests
     */
    List<PullRequest> findByCommit(String commit);
    
    PullRequest create(String title, Branch target, Branch source, boolean autoMerge);
}
