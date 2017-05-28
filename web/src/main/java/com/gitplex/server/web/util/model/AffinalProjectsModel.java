package com.gitplex.server.web.util.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.SecurityUtils;

@SuppressWarnings("serial")
public class AffinalProjectsModel extends LoadableDetachableModel<Collection<Project>> {

	private final Long repoId;
	
	public AffinalProjectsModel(Long repoId) {
		this.repoId = repoId;
	}
	
	@Override
	protected Collection<Project> load() {
		Project project = GitPlex.getInstance(Dao.class).load(Project.class, repoId);;
		List<Project> affinals = new ArrayList<>(project.getForkDescendants());
		affinals.remove(project);
		if (project.getForkedFrom() != null)
			affinals.remove(project.getForkedFrom());
		affinals.sort((repo1, repo2) -> {
			return repo1.getName().compareTo(repo2.getName());
		});
		if (project.getForkedFrom() != null)
			affinals.add(0, project.getForkedFrom());
		affinals.add(0, project);
		for (Iterator<Project> it = affinals.iterator(); it.hasNext();) {
			if (!SecurityUtils.canRead(it.next()))
				it.remove();
		}
		return affinals;
	}

}
