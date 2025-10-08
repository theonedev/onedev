package io.onedev.server.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectPullRequestStatusStat;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;

public interface PullRequestService extends EntityService<PullRequest> {

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
    PullRequest find(String uuid);

    void discard(User user, PullRequest request, @Nullable String note);

    void reopen(User user, PullRequest request, @Nullable String note);

    void checkAsync(PullRequest request, boolean sourceUpdated, boolean updateBuildCommit);

    void merge(User user, PullRequest request, @Nullable String commitMessage);

    void open(PullRequest request);

    void delete(PullRequest request);

    void deleteSourceBranch(User user, PullRequest request, @Nullable String note);

	void restoreSourceBranch(User user, PullRequest request, @Nullable String note);

	void checkReviews(PullRequest request, boolean sourceUpdated);

	void checkAutoMerge(PullRequest request);

	List<PullRequest> query(Subject subject, @Nullable Project targetProject, 
            EntityQuery<PullRequest> requestQuery, boolean loadExtraInfo, 
            int firstResult, int maxResults);

	int count(Subject subject, @Nullable Project targetProject, 
            @Nullable Criteria<PullRequest> requestCriteria);

	void delete(Collection<PullRequest> requests, Project project);

	List<ProjectPullRequestStatusStat> queryStatusStats(Collection<Project> projects);

	Map<Integer, Integer> queryDurationStats(Subject subject, Project project, @Nullable Criteria<PullRequest> pullRequestCriteria, 
            @Nullable Date startDate, @Nullable Date endDate, StatsGroup statsGroup);

	Map<Integer, Pair<Integer, Integer>> queryFrequencyStats(Subject subject, Project project,
            @Nullable Criteria<PullRequest> pullRequestCriteria, 
            @Nullable Date startDate, @Nullable Date endDate, StatsGroup statsGroup);

	ObjectId getComparisonBase(PullRequest request, ObjectId oldCommitId, ObjectId newCommitId);

	List<PullRequest> query(User submitter, Date fromDate, Date toDate);

    Collection<Long> getTargetProjectIds();
    
}
