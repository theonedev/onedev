package io.onedev.server.service.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.LabelSpecService;

@Singleton
public class DefaultLabelSpecService extends BaseEntityService<LabelSpec> implements LabelSpecService {

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
	public void createOrUpdate(LabelSpec labelSpec) {
		dao.persist(labelSpec);
	}

}