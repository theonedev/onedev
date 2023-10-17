package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

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