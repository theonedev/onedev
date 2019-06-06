package io.onedev.server.web.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

@SuppressWarnings("serial")
public class AffinalProjectsModel extends LoadableDetachableModel<Collection<Project>> {

	private final Long projectId;
	
	public AffinalProjectsModel(Long projectId) {
		this.projectId = projectId;
	}
	
	@Override
	protected Collection<Project> load() {
		Project project = OneDev.getInstance(Dao.class).load(Project.class, projectId);
		List<Project> affinals = project.getForkRoot().getForkDescendants();
		for (Iterator<Project> it = affinals.iterator(); it.hasNext();) {
			if (!SecurityUtils.canReadIssues(it.next().getFacade()))
				it.remove();
		}
		return affinals;
	}

}
