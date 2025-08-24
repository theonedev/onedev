package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

public abstract class BaseEntityLabelManager<T extends EntityLabel> extends BaseEntityManager<T> {

	private final LabelSpecManager labelSpecManager;
	
	@Inject
    public BaseEntityLabelManager(Dao dao, LabelSpecManager labelSpecManager) {
        super(dao);
        this.labelSpecManager = labelSpecManager;
    }

	@Transactional
	public void sync(LabelSupport<T> entity, Collection<String> labelNames) {
		var labelsToRemove = new HashSet<>();
		entity.getLabels().stream()
				.filter(it->!labelNames.contains(it.getSpec().getName()))
				.forEach(it-> {delete(it); labelsToRemove.add(it);});
		entity.getLabels().removeAll(labelsToRemove);
		
		Collection<String> existingLabelNames = entity.getLabels().stream()
				.map(it->it.getSpec().getName())
				.collect(Collectors.toSet());
		labelNames.stream().filter(it->!existingLabelNames.contains(it)).forEach(it-> {
			var labelSpec = labelSpecManager.find(it);
			if (labelSpec == null)
				throw new EntityNotFoundException("Label spec not found: " + it);
			var label = newEntityLabel((AbstractEntity) entity, labelSpec);
			dao.persist(label);
			entity.getLabels().add(label);
		});
	}

	protected abstract T newEntityLabel(AbstractEntity entity, LabelSpec spec);
	
}