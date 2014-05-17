package com.pmease.gitop.core.manager;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.core.manager.impl.DefaultPullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;

@ImplementedBy(DefaultPullRequestManager.class)
public interface PullRequestManager {
    
    @Nullable PullRequest findOpen(Branch target, Branch source);

    void refresh(PullRequest request);
    
    PullRequest preview(Branch target, Branch source, User submitter, File sandbox);
    
    boolean merge(PullRequest request, @Nullable User user, @Nullable String comment);
    
    void discard(PullRequest request, User user, @Nullable String comment);
    
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
    
    void send(PullRequest request);
    
    void delete(PullRequest request);
}
