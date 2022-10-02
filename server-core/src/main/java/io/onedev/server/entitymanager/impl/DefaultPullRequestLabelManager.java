package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.LabelManager;
import io.onedev.server.entitymanager.PullRequestLabelManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestLabelManager extends BaseEntityLabelManager<PullRequestLabel> 
		implements PullRequestLabelManager {

	@Inject
    public DefaultPullRequestLabelManager(Dao dao, LabelManager labelManager) {
        super(dao, labelManager);
    }

	@Override
	protected PullRequestLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new PullRequestLabel();
		label.setRequest((PullRequest) entity);
		label.setSpec(spec);
		return label;
	}

}