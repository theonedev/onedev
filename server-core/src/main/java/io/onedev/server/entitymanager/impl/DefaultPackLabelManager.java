package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.entitymanager.PackLabelManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackLabel;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultPackLabelManager extends BaseEntityLabelManager<PackLabel> implements PackLabelManager {

	@Inject
    public DefaultPackLabelManager(Dao dao, LabelSpecManager labelSpecManager) {
        super(dao, labelSpecManager);
    }

	@Override
	protected PackLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new PackLabel();
		label.setPack((Pack) entity);
		label.setSpec(spec);
		return label;
	}

	@Override
	public void create(PackLabel packLabel) {
		Preconditions.checkState(packLabel.isNew());
		dao.persist(packLabel);
	}
	
}