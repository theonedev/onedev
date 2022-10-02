package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.BuildLabelManager;
import io.onedev.server.entitymanager.LabelManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildLabel;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultBuildLabelManager extends BaseEntityLabelManager<BuildLabel> implements BuildLabelManager {

	@Inject
    public DefaultBuildLabelManager(Dao dao, LabelManager labelManager) {
        super(dao, labelManager);
    }

	@Override
	protected BuildLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new BuildLabel();
		label.setBuild((Build) entity);
		label.setSpec(spec);
		return label;
	}

}