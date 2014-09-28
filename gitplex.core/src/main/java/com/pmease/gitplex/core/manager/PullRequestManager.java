package com.pmease.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@ImplementedBy(DefaultPullRequestManager.class)
public interface PullRequestManager {
    
    @Nullable PullRequest findOpen(Branch target, Branch source);

    void refresh(PullRequest request);
    
    /**
     * Integrate specified pull request.
     * 
     * @param request
     * 			pull request to be integrated
     * @param user
     * 			user initiating the integration, <tt>null</tt> to indicate system user
     * @param comment
     * 			comment for the integration
     * @return
     * 			<tt>true</tt> if integrated successfully, <tt>false</tt> if unable to 
     * 			update relevant refs due to ref lock failure. Ref lock failure can happen 
     * 			either because the ref is currently being updated by some other one, or 
     * 			the ref has been updated since last refresh of the pull request. In these 
     * 			cases, the best bet is to refresh pull request again and retry the 
     * 			integration  
     * @throws IllegalArgumentException 
     * 			if <tt>request.canIntegrate()</tt> returns false
     */
    boolean integrate(PullRequest request, @Nullable User user, @Nullable String comment);
    
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
    
	List<IntegrationStrategy> getApplicableIntegrationStrategies(PullRequest request);
    
}
