package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.BuildLabelManager;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildLabel;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.Dao;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class DefaultBuildLabelManager extends BaseEntityLabelManager<BuildLabel> implements BuildLabelManager {

	@Inject
    public DefaultBuildLabelManager(Dao dao, LabelSpecManager labelSpecManager) {
        super(dao, labelSpecManager);
    }

	@Override
	protected BuildLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new BuildLabel();
		label.setBuild((Build) entity);
		label.setSpec(spec);
		return label;
	}

	@Override
	public void create(BuildLabel buildLabel) {
		Preconditions.checkState(buildLabel.isNew());
		dao.persist(buildLabel);
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