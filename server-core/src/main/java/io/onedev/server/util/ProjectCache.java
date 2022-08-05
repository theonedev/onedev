package io.onedev.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectCache {

	private final Map<Long, ProjectFacade> projects;

	public ProjectCache(Map<Long, ProjectFacade> projects) {
		this.projects = projects;
	}

	public boolean isSelfOrAncestorOf(Long parentId, Long childId) {
		if (parentId.equals(childId)) { 
			return true;
		} else {
			ProjectFacade childProject = projects.get(childId);
			if (childProject.getParentId() != null)
				return isSelfOrAncestorOf(parentId, childProject.getParentId());
			else
				return false;
		}
	}
	
	public Collection<Long> getIds() {
		return projects.keySet();
	}
	
	public Collection<ProjectFacade> getAll() {
		return projects.values();
	}
	
	@Nullable
	public ProjectFacade get(Long id) {
		return projects.get(id);
	}
	
	public void cache(ProjectFacade project) {
		projects.put(project.getId(), project);
	}
	
	public void remove(Long id) {
		projects.remove(id);
	}
	
	@Nullable
	public String getPath(Long id) {
		ProjectFacade project = projects.get(id);
		if (project != null) {
			if (project.getParentId() != null) {
				String parentPath = getPath(project.getParentId());
				if (parentPath != null)
					return parentPath + "/" + project.getName();
				else
					return null;
			} else {
				return project.getName();
			}
		} else {
			return null;
		}
	}
	
	public Collection<Long> getMatchingIds(String pathPattern) {
		Collection<Long> ids = new HashSet<>();
		for (Long id: projects.keySet()) {
			String path = getPath(id);
			if (path != null && WildcardUtils.matchPath(pathPattern, path))
				ids.add(id);
		}
		return ids;
	}

	public Collection<Long> getSubtreeIds(Long id) {
		Collection<Long> treeIds = Sets.newHashSet(id);
		for (ProjectFacade facade: projects.values()) {
			if (id.equals(facade.getParentId()))
				treeIds.addAll(getSubtreeIds(facade.getId()));
		}
		return treeIds;
	}
	
    @Nullable
    public Long findId(String path) {
    	Long projectId = null;
    	for (String name: Splitter.on("/").omitEmptyStrings().trimResults().split(path)) {
    		projectId = findId(projectId, name);
    		if (projectId == null)
    			break;
    	}
    	return projectId;
    }
    
	@Nullable
    public Long findId(@Nullable Long parentId, String name) {
		for (ProjectFacade project: projects.values()) {
			if (project.getName().equalsIgnoreCase(name) && Objects.equals(parentId, project.getParentId())) 
				return project.getId();
		}
		return null;
    }
	
	public List<ProjectFacade> getChildren(Long id) {
		List<ProjectFacade> children = new ArrayList<>();
		for (ProjectFacade facade: projects.values()) {
			if (id.equals(facade.getParentId()))
				children.add(facade);
		}
		Collections.sort(children, new Comparator<ProjectFacade>() {

			@Override
			public int compare(ProjectFacade o1, ProjectFacade o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		return children;
	}
	
	@Override
	public ProjectCache clone() {
		return new ProjectCache(projects);
	}
	
}
