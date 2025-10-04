package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.LabelSpecService;

public abstract class BaseEntityLabelService<T extends EntityLabel> extends BaseEntityService<T> {

	@Inject
	private LabelSpecService labelSpecService;

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
			var labelSpec = labelSpecService.find(it);
			if (labelSpec == null)
				throw new EntityNotFoundException("Label spec not found: " + it);
			var label = newEntityLabel((AbstractEntity) entity, labelSpec);
			dao.persist(label);
			entity.getLabels().add(label);
		});
	}

	protected abstract T newEntityLabel(AbstractEntity entity, LabelSpec spec);
	
}