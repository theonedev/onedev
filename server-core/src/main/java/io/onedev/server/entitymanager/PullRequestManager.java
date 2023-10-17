package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectPullRequestStats;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.criteria.Criteria;
import org.eclipse.jgit.lib.ObjectId;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    void checkAsync(PullRequest request, boolean sourceUpdated, boolean updateBuildCommit);
    
    void merge(PullRequest request, @Nullable String commitMessage);
    
    void open(PullRequest request);
    
    @Override
    void delete(PullRequest request);
    
	void deleteSourceBranch(PullRequest request, @Nullable String note);
	
	void restoreSourceBranch(PullRequest request, @Nullable String note);
	
	void checkReviews(PullRequest request, boolean sourceUpdated);
	
	List<PullRequest> query(@Nullable Project targetProject, EntityQuery<PullRequest> requestQuery, 
			boolean loadReviewsAndBuilds, int firstResult, int maxResults);
	
	int count(@Nullable Project targetProject, Criteria<PullRequest> requestCriteria);
	
	List<PullRequest> query(Project targetProject, String term, int count);

	void delete(Collection<PullRequest> requests, Project project);
	
	List<ProjectPullRequestStats> queryStats(Collection<Project> projects);
	
	ObjectId getComparisonBase(PullRequest request, ObjectId oldCommitId, ObjectId newCommitId);
	
}
