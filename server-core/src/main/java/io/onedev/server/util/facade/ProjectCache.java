package io.onedev.server.util.facade;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.MapProxy;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

import static io.onedev.server.util.match.WildcardUtils.matchPath;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

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
			if (matchPath(pathPattern.toLowerCase(), project.getPath().toLowerCase()))
				ids.add(project.getId());
		}
		return ids;
	}

	public Collection<Long> getMatchingIds(PatternSet patternSet) {
		Collection<Long> ids = new HashSet<>();
		for (ProjectFacade project: values()) {
			if (patternSet.matches(new PathMatcher(), project.getPath()))
				ids.add(project.getId());
		}
		return ids;
	}
	
	public Collection<Long> getSubtreeIds(Long id) {
		return getSubtreeIds(values(), id);
	}

	private Collection<Long> getSubtreeIds(Collection<ProjectFacade> projects, Long id) {
		Collection<Long> treeIds = Sets.newHashSet(id);
		for (ProjectFacade facade: projects) {
			if (id.equals(facade.getParentId()))
				treeIds.addAll(getSubtreeIds(projects, facade.getId()));
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
    		if (project.getPath().equalsIgnoreCase(path))
    			return project;
    	}
    	return null;
    }
    
	public List<ProjectFacade> getChildren(Long id) {
		return getChildren(values(), id);
	}

	private List<ProjectFacade> getChildren(Collection<ProjectFacade> projects, Long id) {
		List<ProjectFacade> children = new ArrayList<>();
		for (ProjectFacade facade: projects) {
			if (id.equals(facade.getParentId()))
				children.add(facade);
		}
		Collections.sort(children, comparing(ProjectFacade::getName));
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
		return keySet().stream().map(projectManager::load).collect(toSet());
	}
	
	public Comparator<Project> comparingPath() {
		return (o1, o2) -> get(o1.getId()).getPath().compareTo(get(o2.getId()).getPath());		
	}

}
