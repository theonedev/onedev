package io.onedev.server.service;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import io.onedev.server.annotation.NoDBAccess;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.replica.ProjectReplica;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.patternset.PatternSet;

public interface ProjectService extends EntityService<Project> {

	@Nullable 
	Project findByPath(String path);
	
	@Nullable 
	ProjectFacade findFacadeById(Long id);

	@Nullable
	ProjectFacade findFacadeByKey(String key);

	@Nullable
	Project findByKey(String key);
	
	@Nullable
	ProjectFacade findFacadeByPath(String path);
	
	@Nullable
	Project findByServiceDeskEmailAddress(String serviceDeskEmailAddress);
	
	Project setup(Subject subject, String path);
	
	@Nullable
	Project find(@Nullable Project parent, String name);

	void fork(Project from, Project to);
	
	/**
	 * Create specified project as specified user. This method will also create parent project if not exists, 
	 * and parent creation will be audited
	 * 
	 * @param user
	 * @param project
	 */
	void create(User user, Project project);
	
	void update(Project project);
	
	void onDeleteBranch(Project project, String branchName);
	
	void clone(Project project, String repositoryUrl);
	
	void deleteBranch(Project project, String branchName);
	
	void onDeleteTag(Project project, String tagName);
	
	void deleteTag(Project project, String tagName);
	
	Repository getRepository(Long projectId);
	
	List<Project> query(Subject subject, EntityQuery<Project> query, boolean loadLabels, int firstResult, int maxResults);
	
	int count(Subject subject, Criteria<Project> criteria);

	Predicate getPathMatchPredicate(CriteriaBuilder builder, Path<Project> path, String pathPattern);
		
	List<ProjectFacade> getChildren(Long projectId);

	boolean hasChildren(Long projectId);
	
	void move(Collection<Project> projects, @Nullable Project parent);
		
	void delete(Collection<Project> projects);

	Collection<Long> getIds();
	
	Collection<Long> getSubtreeIds(Long projectId);
	
	Collection<Long> getPathMatchingIds(PatternSet patternSet);
	
	ProjectCache cloneCache();

	@Nullable
	ProjectFacade findFacade(Long projectId);
	
	@Nullable
	String getFavoriteQuery(@Nullable User user);
	
	@Nullable
	@NoDBAccess
	String getActiveServer(Long projectId, boolean mustExist);

	Set<Long> getActiveIds();

	Map<String, Collection<Long>> groupByActiveServers(Collection<Long> projectIds);

	<T> T runOnActiveServer(Long projectId, ClusterTask<T> task);

	<T> Map<String, T> runOnReplicaServers(Long projectId, ClusterTask<T> task);

	<T> Future<T> submitToActiveServer(Long projectId, ClusterTask<T> task);

	<T> Map<String, Future<T>> submitToReplicaServers(Long projectId, ClusterTask<T> task);

	File getLfsObjectsDir(Long projectId);

	Collection<String> getReservedNames();
	
	@Nullable
	ArtifactInfo getSiteArtifactInfo(Long projectId, String siteArtifactPath);

	void checkGitConfig(Long projectId, GitPackConfig gitPackConfig);

	void redistributeReplicas();
	
	void directoryModified(Long projectId, File directory);

	boolean hasLfsObjects(Long projectId);
	
	Map<String, ProjectReplica> getReplicas(Long projectId);
	
	Collection<Long> getIdsWithoutEnoughReplicas();
	
	Collection<Long> getIdsHasOutdatedReplicas();
	
	Collection<Long> getIdsMissingStorage();
	
	boolean hasOutdatedReplicas(Long projectId);
	
	boolean isWithoutEnoughReplicas(Long projectId);
	
	boolean isMissingStorage(Long projectId);
	
	void requestToSyncReplica(Long projectId, String syncWithServer);

	Collection<ObjectId> readLfsSinceCommits(Long projectId);
	
	void writeLfsSinceCommits(Long projectId, Collection<ObjectId> commitIds);
	
	void syncDirectory(Long projectId, String path, Consumer<String> childSyncer, String activeServer);
		
	void syncDirectory(Long projectId, String path, String readLock, String activeServer);

	void syncFile(Long projectId, String path, String readLock, String activeServer);

	@NoDBAccess
	File getProjectsDir();

	@NoDBAccess
	File getProjectDir(Long projectId);

	/**
	 * Get directory to store git repository of specified project
	 *
	 * @return
	 * 			directory to store git repository. The directory will be exist after calling this method
	 */
	@NoDBAccess
	File getGitDir(Long projectId);

	/**
	 * Get directory to store Lucene index of specified project
	 *
	 * @return
	 * 			directory to store lucene index. The directory will be exist after calling this method
	 */
	@NoDBAccess
	File getIndexDir(Long projectId);

	@NoDBAccess
	File getCacheDir(Long projectId);

	/**
	 * Get directory to store static content of specified project
	 *
	 * @return
	 * 			directory to store static content. The directory will exist after calling this method
	 */
	File getSiteDir(Long projectId);

	/**
	 * Get directory to store additional info of specified project
	 *
	 * @return
	 * 			directory to store additional info. The directory will exist after calling this method
	 */
	File getInfoDir(Long projectId);
	
	/**
	 * Get directory to store attachments of specified project
	 *
	 * @return
	 * 			directory store attachments. The directory will be exist after calling this method
	 */
	File getAttachmentDir(Long projectId);

	@NoDBAccess
	File getSubDir(Long projectId, String subdirPath);

	@NoDBAccess
	File getSubDir(Long projectId, String subdirPath, boolean createIfNotExist);
	
	void updateActiveServers();

	boolean isSharedDir(File dir, String remoteServer, Long projectId, String subPath);
	
}
