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
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.model.Repository;
import com.pmease.gitop.core.storage.RepositoryStorage;

@Singleton
public class DefaultRepositoryManager extends AbstractGenericDao<Repository> implements RepositoryManager {

	private ConfigManager configManager;
	
	@Inject
	public DefaultRepositoryManager(GeneralDao generalDao, ConfigManager configManager) {
		super(generalDao);
		
		this.configManager = configManager;
	}

	@Transactional
	@Override
	public void save(Repository entity) {
		if (entity.isNew()) {
			super.save(entity);
			
			RepositoryStorage storage = locateStorage(entity);
			storage.clean();
			
			new Git(storage.ofCode()).init().bare(true).call();
		} else {
			super.save(entity);
		}
	}
	
	@Transactional
	@Override
	public void delete(Repository entity) {
		super.delete(entity);
		
		locateStorage(entity).delete();
	}

	@Override
	public RepositoryStorage locateStorage(Repository repository) {
		return new RepositoryStorage(new File(configManager.getStorageSetting().getRepoStorageDir(), repository.getId().toString())); 
	}

	@Override
	public Collection<Repository> findPublic() {
		return query(new Criterion[]{Restrictions.eq("publiclyAccessible", true)});
	}

	@Override
	public Repository find(String ownerName, String repositoryName) {
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("name", repositoryName));
		criteria.createAlias("owner", "owner");
		criteria.add(Restrictions.eq("owner.name", ownerName));
		
		criteria.setMaxResults(1);
		return (Repository) criteria.uniqueResult();
	}

}
