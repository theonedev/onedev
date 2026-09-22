package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import com.google.common.base.Preconditions;

import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildLabelAdded;
import io.onedev.server.event.project.build.BuildLabelRemoved;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildLabel;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildLabelService;

@Singleton
public class DefaultBuildLabelService extends BaseEntityLabelService<BuildLabel> implements BuildLabelService {

	@Inject
	private ListenerRegistry listenerRegistry;

	@Override
	protected BuildLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new BuildLabel();
		label.setBuild((Build) entity);
		label.setSpec(spec);
		return label;
	}

	@Transactional
	@Override
	public void create(BuildLabel buildLabel) {
		Preconditions.checkState(buildLabel.isNew());
		dao.persist(buildLabel);
		listenerRegistry.post(new BuildLabelAdded(SecurityUtils.getUser(), new Date(),
				buildLabel.getBuild(), buildLabel.getSpec().getName()));
	}

	@Transactional
	@Override
	public void delete(BuildLabel buildLabel) {
		super.delete(buildLabel);
		listenerRegistry.post(new BuildLabelRemoved(SecurityUtils.getUser(), new Date(),
				buildLabel.getBuild(), buildLabel.getSpec().getName()));
	}

	@Sessional
	@Override
	public void populateLabels(List<Build> builds) {
		var builder = getSession().getCriteriaBuilder();
		CriteriaQuery<BuildLabel> labelQuery = builder.createQuery(BuildLabel.class);
		Root<BuildLabel> labelRoot = labelQuery.from(BuildLabel.class);
		labelQuery.select(labelRoot);
		labelQuery.where(labelRoot.get(BuildLabel.PROP_BUILD).in(builds));

		for (var build: builds)
			build.setLabels(new ArrayList<>());

		for (var label: getSession().createQuery(labelQuery).getResultList())
			label.getBuild().getLabels().add(label);
	}

}