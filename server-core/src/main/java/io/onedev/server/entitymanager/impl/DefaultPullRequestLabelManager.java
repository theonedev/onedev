package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.entitymanager.PullRequestLabelManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;

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

	@Sessional
	@Override
	public void populateLabels(Collection<PullRequest> pullRequests) {
		var builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PullRequestLabel> labelQuery = builder.createQuery(PullRequestLabel.class);
		Root<PullRequestLabel> labelRoot = labelQuery.from(PullRequestLabel.class);
		labelQuery.select(labelRoot);
		labelQuery.where(labelRoot.get(PullRequestLabel.PROP_REQUEST).in(pullRequests));

		for (var pullRequest: pullRequests)
			pullRequest.setLabels(new ArrayList<>());

		for (var label: getSession().createQuery(labelQuery).getResultList())
			label.getRequest().getLabels().add(label);
	}

}