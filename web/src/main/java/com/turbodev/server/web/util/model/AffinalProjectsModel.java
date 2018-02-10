package com.turbodev.server.web.util.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.turbodev.server.TurboDev;
import com.turbodev.server.model.Project;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.security.SecurityUtils;

@SuppressWarnings("serial")
public class AffinalProjectsModel extends LoadableDetachableModel<Collection<Project>> {

	private final Long projectId;
	
	public AffinalProjectsModel(Long projectId) {
		this.projectId = projectId;
	}
	
	@Override
	protected Collection<Project> load() {
		Project project = TurboDev.getInstance(Dao.class).load(Project.class, projectId);
		List<Project> affinals = project.getForkRoot().getForkDescendants();
		for (Iterator<Project> it = affinals.iterator(); it.hasNext();) {
			if (!SecurityUtils.canRead(it.next()))
				it.remove();
		}
		return affinals;
	}

}
