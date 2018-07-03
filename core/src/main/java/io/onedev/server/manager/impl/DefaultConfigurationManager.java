package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultConfigurationManager extends AbstractEntityManager<Configuration> 
		implements ConfigurationManager {

	private final ProjectManager projectManager;
	
	@Inject
	public DefaultConfigurationManager(Dao dao, ProjectManager projectManager) {
		super(dao);
		this.projectManager = projectManager;
	}

	@Sessional
	@Override
	public Configuration find(Project project, String name) {
		EntityCriteria<Configuration> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("name", name));
		return find(criteria);
	}

	@Transactional
	@Override
	public void save(Configuration configuration, String oldName) {
		super.save(configuration);
    	if (oldName != null && !oldName.equals(configuration.getName())) {
    		for (Project project: projectManager.findAll()) {
    			for (BranchProtection protection: project.getBranchProtections())
    				protection.onRenameConfiguration(oldName, configuration.getName());
    		}
    	}
	}

	@Transactional
	@Override
	public void delete(Configuration configuration) {
		super.delete(configuration);
		for (Project project: projectManager.findAll()) {
			for (BranchProtection protection: project.getBranchProtections())
				protection.onDeleteConfiguration(configuration.getName());
		}
	}

}
