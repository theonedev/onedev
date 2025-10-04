package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import com.google.common.base.Preconditions;

import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.project.pullrequest.PullRequestCommentEdited;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestCommentRemovedData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestCommentService;

@Singleton
public class DefaultPullRequestCommentService extends BaseEntityService<PullRequestComment>
		implements PullRequestCommentService {

	@Inject
	private PullRequestChangeService changeService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void delete(User user, PullRequestComment comment) {
		super.delete(comment);
		comment.getRequest().setCommentCount(comment.getRequest().getCommentCount()-1);
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(comment.getRequest());
		change.setData(new PullRequestCommentRemovedData());
		change.setUser(user);
		changeService.create(change, null);
	}

	@Transactional
	@Override
	public void create(PullRequestComment comment) {
		create(comment, new ArrayList<>());
	}
	
	@Transactional
	@Override
	public void create(PullRequestComment comment, Collection<String> notifiedEmailAddresses) {
		Preconditions.checkState(comment.isNew());
		dao.persist(comment);
		comment.getRequest().setCommentCount(comment.getRequest().getCommentCount()+1);
		listenerRegistry.post(new PullRequestCommentCreated(comment, notifiedEmailAddresses));
	}

	@Transactional
	@Override
	public void update(PullRequestComment comment) {
		Preconditions.checkState(!comment.isNew());
		dao.persist(comment);
		listenerRegistry.post(new PullRequestCommentEdited(comment));
	}

	@Override
	public List<PullRequestComment> query(User submitter, Date fromDate, Date toDate) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PullRequestComment> query = builder.createQuery(PullRequestComment.class);
		From<PullRequestComment, PullRequestComment> root = query.from(PullRequestComment.class);
		
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(builder.equal(root.get(PullRequestComment.PROP_USER), submitter));
		predicates.add(builder.greaterThanOrEqualTo(root.get(PullRequestComment.PROP_DATE), fromDate));
		predicates.add(builder.lessThanOrEqualTo(root.get(PullRequestComment.PROP_DATE), toDate));
			
		query.where(predicates.toArray(new Predicate[0]));
		
		return getSession().createQuery(query).getResultList();
	}
	
}
