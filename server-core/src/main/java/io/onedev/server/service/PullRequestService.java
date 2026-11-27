package io.onedev.server.service;

import dev.langchain4j.model.chat.ChatModel;
import io.onedev.server.exception.RedundantPullRequestException;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectPullRequestStatusStat;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.subject.Subject;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /**
     * Check that the request can be opened and populate its initial update, assignees, reviewers,
     * and merge strategy. Call this before previewing the request or generating a title from its commits.
     * Repeated calls recheck eligibility and preserve draft edits.
     * @throws RedundantPullRequestException if the branches are identical, a request is already open
     *         for them, a previous request merged the same source head, or the target is up to date
     */
    void prepareDraft(PullRequest request);

    /** Prepare and persist a new pull request. Callers may supply a fresh request or an edited draft. */
    void open(PullRequest request);

    /** Reconcile from current refs and protection rules after the calling transaction commits. */
    default void reconcileAsync(PullRequest request) {
        reconcileAsync(request, false);
    }

    /** Set refreshBuildCommit to select the current merge preview even if only the target branch advanced. */
    void reconcileAsync(PullRequest request, boolean refreshBuildCommit);

    void merge(User user, PullRequest request, @Nullable String commitMessage);

    void delete(PullRequest request);

    void deleteSourceBranch(User user, PullRequest request, @Nullable String note);

	void restoreSourceBranch(User user, PullRequest request, @Nullable String note);

	List<PullRequest> query(Subject subject, @Nullable Project targetProject, 
            EntityQuery<PullRequest> query, boolean loadExtraInfo, 
            int firstResult, int maxResults);

	int count(Subject subject, @Nullable Project targetProject, @Nullable Criteria<PullRequest> criteria);

	void delete(Collection<PullRequest> requests, Project project);

	List<ProjectPullRequestStatusStat> queryStatusStats(Collection<Project> projects);

	Map<Integer, Integer> queryDurationStats(Subject subject, Project project, 
            @Nullable Criteria<PullRequest> criteria, @Nullable Date startDate, @Nullable Date endDate, 
            StatsGroup statsGroup);

	Map<Integer, Pair<Integer, Integer>> queryFrequencyStats(Subject subject, Project project,
            @Nullable Criteria<PullRequest> criteria, @Nullable Date startDate, @Nullable Date endDate, 
            StatsGroup statsGroup);

	List<PullRequest> query(User submitter, Date fromDate, Date toDate);

    Collection<Long> getTargetProjectIds();

	List<PullRequest> queryAfter(Long projectId, Long afterRequestId, int count);

    Pair<String, String> suggestTitleAndDescription(PullRequest request, ChatModel chatModel, 
            boolean suggestTitle, boolean suggestDescription);

}
