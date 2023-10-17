package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.entitymanager.PullRequestLabelManager;
import io.onedev.server.model.*;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestLabelManager extends BaseEntityLabelManager<PullRequestLabel> 
		implements PullRequestLabelManager {

	@Inject
    public DefaultPullRequestLabelManager(Dao dao, LabelSpecManager labelSpecManager) {
        super(dao, labelSpecManager);
    }

	@Override
	protected PullRequestLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new PullRequestLabel();
		label.setRequest((PullRequest) entity);
		label.setSpec(spec);
		return label;
	}

	@Override
	public void create(PullRequestLabel pullRequestLabel) {
		Preconditions.checkState(pullRequestLabel.isNew());
		dao.persist(pullRequestLabel);
	}
	
}