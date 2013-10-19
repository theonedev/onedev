package com.pmease.gitop.core.manager.impl;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.storage.ProjectStorage;
import com.pmease.gitop.core.validation.ProjectNameReservation;

@Singleton
public class DefaultProjectManager extends AbstractGenericDao<Project> implements ProjectManager {

	private final Set<ProjectNameReservation> nameReservations;
	
	private final StorageManager storageManager;
	
	@Inject
	public DefaultProjectManager(GeneralDao generalDao, StorageManager storageManager, 
	        Set<ProjectNameReservation> nameReservations) {
		super(generalDao);
		
		this.storageManager = storageManager;
		this.nameReservations = nameReservations;
	}

	@Transactional
	@Override
	public void save(Project entity) {
		if (entity.isNew()) {
			super.save(entity);
			
			ProjectStorage storage = storageManager.getStorage(entity);
			storage.clean();
			
			File codeDir = storage.ofCode();
			FileUtils.createDir(codeDir);
			new Git(codeDir).init().bare(true).call();
		} else {
			File codeDir = storageManager.getStorage(entity).ofCode();
			if (!codeDir.exists()) {
				FileUtils.createDir(codeDir);
				new Git(codeDir).init().bare(true).call();
			}
			super.save(entity);
		}
	}
	
	@Transactional
	@Override
	public void delete(Project entity) {
		super.delete(entity);
		
		storageManager.getStorage(entity).delete();
	}

	@Override
	public Collection<Project> findPublic() {
		return query(new Criterion[]{Restrictions.eq("publiclyAccessible", true)});
	}

	@Sessional
	@Override
	public Project find(String ownerName, String projectName) {
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("name", projectName));
		criteria.createAlias("owner", "owner");
		criteria.add(Restrictions.eq("owner.name", ownerName));
		
		criteria.setMaxResults(1);
		return (Project) criteria.uniqueResult();
	}

	@Override
	public Set<String> getReservedNames() {
		Set<String> reservedNames = new HashSet<String>();
		for (ProjectNameReservation each: nameReservations)
			reservedNames.addAll(each.getReserved());
		
		return reservedNames;
	}

	@Sessional
	@Override
	public Project find(User owner, String projectName) {
		return find(Restrictions.eq("owner.id", owner.getId()), Restrictions.eq("name", projectName));
	}

}
