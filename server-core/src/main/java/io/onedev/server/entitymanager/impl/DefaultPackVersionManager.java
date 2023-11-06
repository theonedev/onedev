package io.onedev.server.entitymanager.impl;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.entitymanager.PackBlobAuthorizationManager;
import io.onedev.server.entitymanager.PackBlobReferenceManager;
import io.onedev.server.entitymanager.PackVersionManager;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.model.PackVersion;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.onedev.server.model.AbstractEntity.PROP_ID;
import static io.onedev.server.model.PackVersion.*;
import static java.lang.Math.min;

@Singleton
public class DefaultPackVersionManager extends BaseEntityManager<PackVersion> 
		implements PackVersionManager, Serializable {
	
	private final PackBlobAuthorizationManager blobAuthorizationManager;
	private final PackBlobReferenceManager blobReferenceManager;
	
	@Inject
	public DefaultPackVersionManager(Dao dao, PackBlobAuthorizationManager blobAuthorizationManager, 
									 PackBlobReferenceManager blobReferenceManager) {
		super(dao);
		this.blobAuthorizationManager = blobAuthorizationManager;
		this.blobReferenceManager = blobReferenceManager;
	}
	
	private EntityCriteria<PackVersion> newCriteria(Project project, String type, @Nullable String query) {
		EntityCriteria<PackVersion> criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		if (query != null)
			criteria.add(Restrictions.ilike(PROP_NAME, query, MatchMode.ANYWHERE));
		return criteria;
	}
	
	@Sessional
	@Override
	public List<PackVersion> query(Project project, String type, @Nullable String query,
								   int firstResult, int maxResults) {
		var criteria = newCriteria(project, type, query);
		criteria.addOrder(Order.desc(PROP_ID));
		return query(criteria, firstResult, maxResults);
	}

	@Sessional
	@Override
	public List<String> queryTags(Project project, String type, String lastTag, int count) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = builder.createQuery(String.class);
		Root<PackVersion> root = criteriaQuery.from(PackVersion.class);
		criteriaQuery.select(root.get(PROP_NAME));
		criteriaQuery.where(
				builder.equal(root.get(PROP_PROJECT), project), 
				builder.equal(root.get(PROP_TYPE), type), 
				builder.notLike(root.get(PROP_NAME), "%:%"));
		criteriaQuery.orderBy(builder.asc(root.get(PROP_NAME)));
		
		if (lastTag != null) {
			var tags = getSession().createQuery(criteriaQuery).getResultList();
			var index = tags.indexOf(lastTag);
			if (index != -1) 
				return tags.subList(index + 1, min(tags.size(), index + 1 + count));
			else 
				return new ArrayList<>();
		} else {
			var query = getSession().createQuery(criteriaQuery);
			query.setMaxResults(count);
			return query.getResultList();
		}
	}

	@Sessional
	@Override
	public int count(Project project, String type, @Nullable String query) {
		return count(newCriteria(project, type, query));
	}

	@Sessional
	@Override
	public PackVersion findByName(Project project, String type, String name) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.eq(PROP_NAME, name));
		return find(criteria);
	}

	@Sessional
	@Override
	public PackVersion findByDataHash(Project project, String type, String dataHash) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.eq(PROP_DATA_HASH, dataHash));
		return find(criteria);
	}

	@Transactional
	@Override
	public void deleteByName(Project project, String type, String name) {
		var packVersion = findByName(project, type, name);
		if (packVersion != null)
			delete(packVersion);
	}
	
	@Transactional
	@Override
	public void deleteByDataHash(Project project, String type, String dataHash) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.eq(PROP_DATA_HASH, dataHash));
		for (var packVersion: query(criteria)) 
			delete(packVersion);
	}
	
	@Transactional
	@Override
	public void createOrUpdate(PackVersion packVersion, Collection<PackBlob> packBlobs) {
		dao.persist(packVersion);
		for (var packBlob: packBlobs) {
			blobAuthorizationManager.authorize(packVersion.getProject(), packBlob);
			if (packVersion.getBlobReferences().stream().noneMatch(it -> it.getPackBlob().equals(packBlob))) {
				var blobReference = new PackBlobReference();
				blobReference.setPackVersion(packVersion);
				blobReference.setPackBlob(packBlob);
				blobReferenceManager.create(blobReference);
			}
		}
		for (var blobReference: packVersion.getBlobReferences()) {
			if (packBlobs.stream().noneMatch(it -> it.equals(blobReference.getPackBlob())))
				blobReferenceManager.delete(blobReference);
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PackVersionManager.class);
	}
	
}
