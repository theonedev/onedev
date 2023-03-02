package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.LabelManager;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultLabelManager extends BaseEntityManager<LabelSpec> implements LabelManager {

	@Inject
    public DefaultLabelManager(Dao dao) {
        super(dao);
    }

	@Override
	public LabelSpec find(String name) {
		EntityCriteria<LabelSpec> criteria = EntityCriteria.of(LabelSpec.class);
		criteria.add(Restrictions.eq(LabelSpec.PROP_NAME, name));
		return find(criteria);
	}

	private EntityCriteria<LabelSpec> getCriteria(@Nullable String term) {
		EntityCriteria<LabelSpec> criteria = EntityCriteria.of(LabelSpec.class);
		if (term != null) 
			criteria.add(Restrictions.ilike(LabelSpec.PROP_NAME, term, MatchMode.ANYWHERE));
		else
			criteria.setCacheable(true);
		return criteria;
	}
	
	@Sessional
	@Override
	public List<LabelSpec> query(String term, int firstResult, int maxResults) {
		EntityCriteria<LabelSpec> criteria = getCriteria(term);
		criteria.addOrder(Order.asc(LabelSpec.PROP_NAME));
		return query(criteria, firstResult, maxResults);
	}

	@Sessional
	@Override
	public int count(String term) {
		return count(getCriteria(term));
	}

	@Transactional
	@Override
	public void sync(List<LabelSpec> labels) {
		for (var label: labels) 
			dao.persist(label);
		for (var existingLabel: query()) {
			if (!labels.contains(existingLabel))
				delete(existingLabel);
		}
	}
	
}