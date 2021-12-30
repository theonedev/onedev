package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectPullRequestStats;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.criteria.Criteria;

public interface PullRequestManager extends EntityManager<PullRequest> {
    
    @Nullable 
    PullRequest findEffective(ProjectAndBranch target, ProjectAndBranch source);
    
    Map<ProjectAndBranch, PullRequest> findEffectives(ProjectAndBranch target, Collection<ProjectAndBranch> sources);
    
    @Nullable 
    PullRequest findOpen(ProjectAndBranch target, ProjectAndBranch source);
    
    Collection<PullRequest> queryOpenTo(ProjectAndBranch target);

    Collection<PullRequest> queryOpen(ProjectAndBranch sourceOrTarget);
    
    @Nullable
    PullRequest find(Project targetProject, long number);
    
    @Nullable
    PullRequest findByFQN(String fqn);
    
    @Nullable
    PullRequest findByUUID(String uuid);
    
    @Nullable
    PullRequest find(ProjectScopedNumber fqn);
    
	@Nullable
	PullRequest findLatest(Project targetProject);
	
    void discard(PullRequest request, @Nullable String note);
    
    void reopen(PullRequest request, @Nullable String note);

    void check(PullRequest request);
    
    void merge(PullRequest request, @Nullable String commitMessage);
    
    void open(PullRequest request);
    
    void delete(PullRequest request);
    
	void deleteSourceBranch(PullRequest request, @Nullable String note);
	
	void restoreSourceBranch(PullRequest request, @Nullable String note);
	
	int countOpen(Project targetProject);

	void checkReviews(PullRequest request, List<User> unpreferableReviewers);
	
	List<PullRequest> query(@Nullable Project targetProject, EntityQuery<PullRequest> requestQuery, 
			int firstResult, int maxResults, boolean loadReviews, boolean loadBuilds);
	
	int count(@Nullable Project targetProject, Criteria<PullRequest> requestCriteria);
	
	List<PullRequest> query(Project targetProject, String term, int count);

	void delete(Collection<PullRequest> requests);
	
	void saveDescription(PullRequest request, @Nullable String description);
	
	List<ProjectPullRequestStats> queryStats(Collection<Project> projects);
	
}
