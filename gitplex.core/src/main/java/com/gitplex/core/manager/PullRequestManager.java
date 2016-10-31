package com.gitplex.core.manager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.gitplex.core.entity.support.DepotAndBranch;
import com.gitplex.core.entity.support.IntegrationPreview;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface PullRequestManager extends EntityManager<PullRequest> {
    
    @Nullable 
    PullRequest findOpen(DepotAndBranch target, DepotAndBranch source);
    
    Collection<PullRequest> findAllOpenTo(DepotAndBranch target, @Nullable Depot sourceDepot);

    Collection<PullRequest> findAllOpenFrom(DepotAndBranch source, @Nullable Depot targetDepot);
   
    Collection<PullRequest> findAllOpen(DepotAndBranch sourceOrTarget);
    
    @Nullable
    PullRequest find(Depot target, long number);
    
	@Nullable
	PullRequest find(String uuid);
	
	@Nullable
	PullRequest findLatest(Depot depot, Account submitter);
    
    /**
     * Integrate specified pull request.
     * 
     * @param request
     * 			pull request to be integrated
     * @param note
     * 			comment for the integration
     */
    void integrate(PullRequest request, @Nullable String note);
    
    void discard(PullRequest request, @Nullable String note);
    
    void reopen(PullRequest request, @Nullable String note);

    void changeAssignee(PullRequest request);
    
    void check(PullRequest request);
    
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
    
    void open(PullRequest request);
    
    void delete(PullRequest request);
    
	List<IntegrationStrategy> getApplicableIntegrationStrategies(PullRequest request);

	void deleteSourceBranch(PullRequest request, @Nullable String note);
	
	void restoreSourceBranch(PullRequest request, @Nullable String note);
	
	int countOpen(Depot depot);
	
}
