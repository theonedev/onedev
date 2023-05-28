package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultLabelSpecManager extends BaseEntityManager<LabelSpec> implements LabelSpecManager {

	@Inject
    public DefaultLabelSpecManager(Dao dao) {
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
	public void sync(List<LabelSpec> labelSpecs) {
		for (var label: labelSpecs) 
			dao.persist(label);
		for (var existingLabel: query()) {
			if (!labelSpecs.contains(existingLabel))
				delete(existingLabel);
		}
	}

	@Transactional
	@Override
	public void create(LabelSpec labelSpec) {
		Preconditions.checkState(labelSpec.isNew());
		dao.persist(labelSpec);
	}

	@Transactional
	@Override
	public void update(LabelSpec labelSpec) {
		Preconditions.checkState(!labelSpec.isNew());
		dao.persist(labelSpec);
	}
}