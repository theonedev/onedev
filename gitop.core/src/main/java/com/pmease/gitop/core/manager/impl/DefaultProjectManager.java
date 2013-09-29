package com.pmease.gitop.core.manager.impl;

import java.io.File;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.storage.ProjectStorage;

@Singleton
public class DefaultProjectManager extends AbstractGenericDao<Project> implements ProjectManager {

	private ConfigManager configManager;
	
	@Inject
	public DefaultProjectManager(GeneralDao generalDao, ConfigManager configManager) {
		super(generalDao);
		
		this.configManager = configManager;
	}

	@Transactional
	@Override
	public void save(Project entity) {
		if (entity.isNew()) {
			super.save(entity);
			
			ProjectStorage storage = locateStorage(entity);
			storage.clean();
			
			new Git(storage.ofCode()).init().bare(true).call();
		} else {
			super.save(entity);
		}
	}
	
	@Transactional
	@Override
	public void delete(Project entity) {
		super.delete(entity);
		
		locateStorage(entity).delete();
	}

	@Override
	public ProjectStorage locateStorage(Project project) {
		return new ProjectStorage(new File(configManager.getStorageSetting().getStorageDir(), project.getId().toString())); 
	}

	@Override
	public Collection<Project> findPublic() {
		return query(new Criterion[]{Restrictions.eq("publiclyAccessible", true)});
	}

	@Override
	public Project find(String ownerName, String projectName) {
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("name", projectName));
		criteria.createAlias("owner", "owner");
		criteria.add(Restrictions.eq("owner.name", ownerName));
		
		criteria.setMaxResults(1);
		return (Project) criteria.uniqueResult();
	}

}
