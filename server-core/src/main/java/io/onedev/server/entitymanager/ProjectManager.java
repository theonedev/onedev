package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.shiro.authz.Permission;
import org.eclipse.jgit.lib.Repository;

import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.ProjectFacade;

public interface ProjectManager extends EntityManager<Project> {
	
	@Nullable 
	Project findByPath(String path);
	
	@Nullable
	Project findByServiceDeskName(String serviceDeskName);
	
	Project initialize(String path);
	
	@Nullable
	Project find(@Nullable Project parent, String name);

	void fork(Project from, Project to);
	
	void create(Project project);
	
	void onDeleteBranch(Project project, String branchName);
	
	void clone(Project project, String repositoryUrl);
	
	void deleteBranch(Project project, String branchName);
	
	void onDeleteTag(Project project, String tagName);
	
	void deleteTag(Project project, String tagName);
	
	Repository getRepository(Project project);
	
	List<Project> query(EntityQuery<Project> query, int firstResult, int maxResults);
	
	int count(Criteria<Project> criteria);

	Predicate getPathMatchPredicate(CriteriaBuilder builder, Path<Project> path, String pathPattern);
		
	List<ProjectFacade> getChildren(Long projectId);
	
	void move(Collection<Project> projects, @Nullable Project parent);

	void delete(Collection<Project> projects);
	
	Collection<Long> getIds();
	
	Collection<Long> getSubtreeIds(Long projectId);
	
	Collection<Project> getPermittedProjects(Permission permission);
	
	ProjectCache cloneCache();
	
	@Nullable
	String getFavoriteQuery();
	
}
