package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.LabelManager;
import io.onedev.server.entitymanager.ProjectLabelManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultProjectLabelManager extends BaseEntityLabelManager<ProjectLabel> implements ProjectLabelManager {
	
	@Inject
    public DefaultProjectLabelManager(Dao dao, LabelManager labelManager) {
        super(dao, labelManager);
    }

	@Override
	protected ProjectLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new ProjectLabel();
		label.setProject((Project) entity);
		label.setSpec(spec);
		return label;
	}

}