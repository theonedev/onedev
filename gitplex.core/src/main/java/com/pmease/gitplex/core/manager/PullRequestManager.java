package com.pmease.gitplex.core.manager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

public interface PullRequestManager {
    
    @Nullable PullRequest findLatest(Branch target, Branch source);
    
    Collection<PullRequest> findOpenTo(Branch target, Repository source);

    Collection<PullRequest> findOpenFrom(Branch source, Repository target);

    boolean canIntegrate(PullRequest request);
    
    /**
     * Integrate specified pull request.
     * 
     * @param request
     * 			pull request to be integrated
     * @param user
     * 			user initiating the integration, <tt>null</tt> to indicate system user
     * @param comment
     * 			comment for the integration
     */
    void integrate(PullRequest request, @Nullable User user, @Nullable String comment);
    
    void discard(PullRequest request, @Nullable User user, @Nullable String comment);
    
    void onTargetBranchUpdate(PullRequest request);
    
    void onSourceBranchUpdate(PullRequest request);
    
    void onGateKeeperUpdate(PullRequest request);
    
    /**
     * Preview integration of this pull request.
     * 
     * @param request
     * 			pull request to preview integration of
     * @return
     * 			integration preview, or <tt>null</tt> if preview is being calculating
     */
    IntegrationPreview previewIntegration(PullRequest request);
    
    /**
     * Delete git refs of this pull request and all its updates.
     * 
     * @param request
     *			pull request whose git refs and update refs to be deleted 	
     */
    void deleteRefs(PullRequest request);
    
    void open(PullRequest request, @Nullable Object listenerData);
    
    void delete(PullRequest request);
    
	List<IntegrationStrategy> getApplicableIntegrationStrategies(PullRequest request);

}
