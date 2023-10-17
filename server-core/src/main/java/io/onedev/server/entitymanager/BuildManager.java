package io.onedev.server.entitymanager;

import java.io.File;
import java.util.*;

import javax.annotation.Nullable;

import io.onedev.server.model.*;
import io.onedev.server.util.artifact.ArtifactInfo;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectBuildStats;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.StatusInfo;
import io.onedev.server.util.criteria.Criteria;

public interface BuildManager extends EntityManager<Build> {
	
	@Nullable
	Build find(Project project, long number);

	@Nullable
	Build find(String buildFQN);
	
    @Nullable
    Build findByUUID(String uuid);

	@Nullable
	Build find(ProjectScopedNumber buildFQN);
	
	@Nullable
	Build findLastFinished(Project project, String jobName, @Nullable String refName);

	@Nullable
	Build findStreamPrevious(Build build, @Nullable Build.Status status);
	
	Collection<Long> queryStreamPreviousNumbers(Build build, @Nullable Build.Status status, int limit);

	Collection<Build> query(Project project, ObjectId commitId, @Nullable String jobName, 
							@Nullable String refName, @Nullable Optional<PullRequest> request, 
							@Nullable Optional<Issue> issue, Map<String, List<String>> params, 
							@Nullable String pipeline);

	Collection<Build> query(Project project, ObjectId commitId, @Nullable String jobName, 
			@Nullable String pipeline);

	Collection<Build> query(Project project, ObjectId commitId, @Nullable String pipeline);

	Map<ObjectId, Map<String, Collection<StatusInfo>>> queryStatus(Project project, Collection<ObjectId> commitIds);

	void create(Build build);
	
	void update(Build build);

	Map<Long, Long> queryUnfinished();
	
	Collection<Build> queryUnfinished(Project project, String jobName, @Nullable String refName, 
									  @Nullable Optional<PullRequest> request, @Nullable Optional<Issue> issue, 
									  @Nullable Map<String, List<String>> params);
	
	List<Build> query(Project project, String term, int count);

	List<Build> query(@Nullable Project project, EntityQuery<Build> buildQuery, int firstResult, int maxResults);

	int count(@Nullable Project project, Criteria<Build> buildCriteria);

	Collection<Long> queryIds(Project project, EntityQuery<Build> buildQuery, int firstResult, int maxResults);

	Collection<Long> getNumbers(Long projectId);

	Collection<Long> filterNumbers(Long projectId, Collection<String> commitHashes);

	Collection<String> getJobNames(@Nullable Project project);

	List<String> queryVersions(Project project, String matchWith, int count);

	Collection<String> getAccessibleJobNames(Project project);

	Map<Project, Collection<String>> getAccessibleJobNames();
	
	void populateBuilds(Collection<PullRequest> requests);
	
	void delete(Collection<Build> builds);
	
	Collection<Build> query(Agent agent, @Nullable Build.Status status);
	
	List<ProjectBuildStats> queryStats(Collection<Project> projects);
	
	@Nullable
	ArtifactInfo getArtifactInfo(Build build, @Nullable String artifactPath);
	
	void deleteArtifact(Build build, @Nullable String artifactPath);
	
	void syncBuilds(Long projectId, String activeServer);
	
	File getStorageDir(Long projectId, Long buildNumber);
	
}
