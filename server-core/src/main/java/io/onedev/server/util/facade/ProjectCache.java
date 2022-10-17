package io.onedev.server.util.facade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.MapProxy;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectCache extends MapProxy<Long, ProjectFacade> implements Serializable {

	public ProjectCache(Map<Long, ProjectFacade> delegate) {
		super(delegate);
	}

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
	
	public Collection<Long> getMatchingIds(String pathPattern) {
		Collection<Long> ids = new HashSet<>();
		for (ProjectFacade project: values()) {
			if (WildcardUtils.matchPath(pathPattern.toLowerCase(), project.getPath().toLowerCase()))
				ids.add(project.getId());
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
    	ProjectFacade project = find(path);
    	return project != null? project.getId(): null;
    }
    
    @Nullable
    public ProjectFacade find(String path) {
    	for (ProjectFacade project: values()) {
    		if (project.getPath().equals(path))
    			return project;
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
		return new ProjectCache(new HashMap<>(delegate));
	}

	public double getSimilarScore(Project project, @Nullable String term) {
		String path = get(project.getId()).getPath();
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
				return get(o1.getId()).getPath().compareTo(get(o2.getId()).getPath());
			}
			
		};		
	}

}
