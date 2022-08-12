package io.onedev.server.util.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectCache extends HashMap<Long, ProjectFacade> {

	private static final long serialVersionUID = 1L;
	
	public boolean isSelfOrAncestorOf(Long parentId, Long childId) {
		if (parentId.equals(childId)) { 
			return true;
		} else {
			ProjectFacade childProject = get(childId);
			if (childProject.getParentId() != null)
				return isSelfOrAncestorOf(parentId, childProject.getParentId());
			else
				return false;
		}
	}
	
	@Nullable
	public String getPath(Long id) {
		ProjectFacade project = get(id);
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
		for (Long id: keySet()) {
			String path = getPath(id);
			if (path != null && WildcardUtils.matchPath(pathPattern, path))
				ids.add(id);
		}
		return ids;
	}

	public Collection<Long> getSubtreeIds(Long id) {
		Collection<Long> treeIds = Sets.newHashSet(id);
		for (ProjectFacade facade: values()) {
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
		for (ProjectFacade project: values()) {
			if (project.getName().equalsIgnoreCase(name) && Objects.equals(parentId, project.getParentId())) 
				return project.getId();
		}
		return null;
    }
	
	public List<ProjectFacade> getChildren(Long id) {
		List<ProjectFacade> children = new ArrayList<>();
		for (ProjectFacade facade: values()) {
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
		ProjectCache clone = new ProjectCache();
		clone.putAll(this);
		return clone;
	}

	public double getSimilarScore(Project project, @Nullable String term) {
		String path = getPath(project.getId());
		return Similarities.getSimilarScore(path, term);
	}

	public Collection<Project> getProjects() {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		return keySet().stream().map(it->projectManager.load(it)).collect(Collectors.toSet());
	}
	
	public Comparator<Project> comparingPath() {
		return new Comparator<Project>() {

			@Override
			public int compare(Project o1, Project o2) {
				return getPath(o1.getId()).compareTo(getPath(o2.getId()));
			}
			
		};		
	}
	
}
