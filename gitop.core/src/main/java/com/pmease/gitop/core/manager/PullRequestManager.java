package com.pmease.gitop.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultPullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;

@ImplementedBy(DefaultPullRequestManager.class)
public interface PullRequestManager extends GenericDao<PullRequest> {
    
    @Nullable PullRequest findOpen(Branch target, Branch source);
    
    void refresh(PullRequest request);
    
    boolean merge(PullRequest request);
    
    void decline(PullRequest request);
    
    /**
     * Find pull requests whose head commit or merge commit equals to specified commit.
     * 
     * @param commit
     * 			head commit or merge commit to match
     * @return
     * 			collection of matching pull requests
     */
    List<PullRequest> findByCommit(String commit);
    
    /**
     * Delete git refs of this pull request and all its updates.
     * 
     * @param request
     *			pull request whose git refs and update refs to be deleted 	
     */
    void deleteRefs(PullRequest request);
    
    PullRequest create(Branch target, Branch source, User submitter, String title, 
    		@Nullable String description, boolean autoMerge);
}
