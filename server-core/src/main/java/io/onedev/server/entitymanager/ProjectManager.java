package io.onedev.server.entitymanager;

import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.replica.ProjectReplica;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.security.permission.BasePermission;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.patternset.PatternSet;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface ProjectManager extends EntityManager<Project> {

	@Nullable 
	Project findByPath(String path);
	
	@Nullable 
	ProjectFacade findFacadeById(Long id);

	@Nullable
	ProjectFacade findFacadeByPath(String path);
	
	@Nullable
	Project findByServiceDeskName(String serviceDeskName);
	
	Project setup(String path);
	
	@Nullable
	Project find(@Nullable Project parent, String name);

	void fork(Project from, Project to);
	
	void create(Project project);
	
	void update(Project project);
	
	void onDeleteBranch(Project project, String branchName);
	
	void clone(Project project, String repositoryUrl);
	
	void deleteBranch(Project project, String branchName);
	
	void onDeleteTag(Project project, String tagName);
	
	void deleteTag(Project project, String tagName);
	
	Repository getRepository(Long projectId);
	
	List<Project> query(EntityQuery<Project> query, int firstResult, int maxResults);
	
	int count(Criteria<Project> criteria);

	Predicate getPathMatchPredicate(CriteriaBuilder builder, Path<Project> path, String pathPattern);
		
	List<ProjectFacade> getChildren(Long projectId);
	
	void move(Collection<Project> projects, @Nullable Project parent);

	void delete(Collection<Project> projects);
	
	Collection<Long> getIds();
	
	Collection<Long> getSubtreeIds(Long projectId);
	
	Collection<Long> getPathMatchingIds(PatternSet patternSet);
	
	Collection<Project> getPermittedProjects(BasePermission permission);
	
	ProjectCache cloneCache();
	
	@Nullable
	String getFavoriteQuery();
	
	@Nullable
	String getActiveServer(Long projectId, boolean mustExist);
	
	Collection<Long> getActiveIds();
	
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

	File getStorageDir();

	File getStorageDir(Long projectId);

	/**
	 * Get directory to store git repository of specified project
	 *
	 * @return
	 * 			directory to store git repository. The directory will be exist after calling this method
	 */
	File getGitDir(Long projectId);

	/**
	 * Get directory to store Lucene index of specified project
	 *
	 * @return
	 * 			directory to store lucene index. The directory will be exist after calling this method
	 */
	File getIndexDir(Long projectId);

	/**
	 * Get directory to store static content of specified project
	 *
	 * @return
	 * 			directory to store static content. The directory will be exist after calling this method
	 */
	File getSiteDir(Long projectId);

	/**
	 * Get directory to store additional info of specified project
	 *
	 * @return
	 * 			directory to store additional info. The directory will be exist after calling this method
	 */
	File getInfoDir(Long projectId);

	/**
	 * Get directory to store attachments of specified project
	 *
	 * @return
	 * 			directory store attachments. The directory will be exist after calling this method
	 */
	File getAttachmentDir(Long projectId);

	File getSubDir(Long projectId, String subdirPath);
	
	void updateActiveServers();

}
