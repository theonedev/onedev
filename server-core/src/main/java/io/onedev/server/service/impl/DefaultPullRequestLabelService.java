package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import com.google.common.base.Preconditions;

import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestLabelAdded;
import io.onedev.server.event.project.pullrequest.PullRequestLabelRemoved;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestLabelService;

@Singleton
public class DefaultPullRequestLabelService extends BaseEntityLabelService<PullRequestLabel>
		implements PullRequestLabelService {

	@Inject
	private ListenerRegistry listenerRegistry;

	@Override
	protected PullRequestLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new PullRequestLabel();
		label.setRequest((PullRequest) entity);
		label.setSpec(spec);
		return label;
	}

	@Transactional
	@Override
	public void create(PullRequestLabel pullRequestLabel) {
		Preconditions.checkState(pullRequestLabel.isNew());
		dao.persist(pullRequestLabel);
		listenerRegistry.post(new PullRequestLabelAdded(SecurityUtils.getUser(), new Date(),
				pullRequestLabel.getRequest(), pullRequestLabel.getSpec().getName()));
	}

	@Transactional
	@Override
	public void delete(PullRequestLabel pullRequestLabel) {
		super.delete(pullRequestLabel);
		listenerRegistry.post(new PullRequestLabelRemoved(SecurityUtils.getUser(), new Date(),
				pullRequestLabel.getRequest(), pullRequestLabel.getSpec().getName()));
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