package io.onedev.server.service;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.annotation.NoDBAccess;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectBuildStatusStat;
import io.onedev.server.util.StatusInfo;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;

public interface BuildService extends EntityService<Build> {
	
	@Nullable
	Build find(Project project, long number);
	
    @Nullable
    Build find(String uuid);
	
	@Nullable
	Build findLastFinished(Project project, String jobName, @Nullable String refName);

	@Nullable
	Build findStreamPrevious(Build build, Build.@Nullable Status status);
	
	Collection<Long> queryStreamPreviousNumbers(Build build, Build.@Nullable Status status, int limit);

	Collection<Build> query(Project project, ObjectId commitId, @Nullable String jobName, 
							@Nullable String refName, @Nullable Optional<PullRequest> request, 
							@Nullable Optional<Issue> issue, Map<String, List<String>> params);

	@Nullable
	Build findPreviousSuccessfulSimilar(Build build);

	Collection<Build> query(Project project, ObjectId commitId, @Nullable String jobName);

	Collection<Build> query(Project project, ObjectId commitId);

	Map<ObjectId, Map<String, Collection<StatusInfo>>> queryStatus(Project project, Collection<ObjectId> commitIds);

	void create(Build build);
	
	void update(Build build);

	Map<Long, Long> queryUnfinished();
	
	Collection<Build> queryUnfinished(Project project, String jobName, @Nullable String refName, 
									  @Nullable Optional<PullRequest> request, @Nullable Optional<Issue> issue, 
									  @Nullable Map<String, List<String>> params);
	
	List<Build> query(Subject subject, Project project, String fuzzyQuery, int count);

	List<Build> query(Subject subject, @Nullable Project project, EntityQuery<Build> buildQuery, 
					  boolean loadLabels, int firstResult, int maxResults);

	int count(Subject subject, @Nullable Project project, Criteria<Build> buildCriteria);

	Collection<Long> getNumbers(Long projectId);

	Collection<Long> filterNumbers(Long projectId, Collection<String> commitHashes);

	Collection<String> getJobNames(@Nullable Project project);

	List<String> queryVersions(Subject subject, Project project, String matchWith, int count);

	Map<Integer, Pair<Integer, Integer>> queryDurationStats(Subject subject, Project project, @Nullable Criteria<Build> buildCriteria, 
			@Nullable Date startDate, @Nullable Date endDate, StatsGroup group);

	Map<Integer, Integer> queryFrequencyStats(Subject subject, Project project, @Nullable Criteria<Build> buildCriteria, 
			@Nullable Date startDate, @Nullable Date endDate, StatsGroup group);
	
	Collection<String> getAccessibleJobNames(Subject subject, Project project);

	Map<Project, Collection<String>> getAccessibleJobNames(Subject subject);
	
	void populateBuilds(Collection<PullRequest> requests);
	
	void delete(Collection<Build> builds);
	
	Collection<Build> query(Agent agent, Build.@Nullable Status status);
	
	List<ProjectBuildStatusStat> queryStatusStats(Collection<Project> projects);
	
	@Nullable
	ArtifactInfo getArtifactInfo(Build build, @Nullable String artifactPath);
	
	void deleteArtifact(Build build, @Nullable String artifactPath);
	
	void syncBuilds(Long projectId, String activeServer);

	@NoDBAccess
	File getBuildDir(Long projectId, Long buildNumber);

	@NoDBAccess
	File getArtifactsDir(Long projectId, Long buildNumber);

}
