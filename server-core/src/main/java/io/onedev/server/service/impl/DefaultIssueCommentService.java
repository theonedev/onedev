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
import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.event.project.issue.IssueCommentEdited;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueCommentRemoveData;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueCommentService;

@Singleton
public class DefaultIssueCommentService extends BaseEntityService<IssueComment> implements IssueCommentService {

	@Inject
	private IssueChangeService changeService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void update(IssueComment comment) {
 		Preconditions.checkState(!comment.isNew());
		dao.persist(comment);
		listenerRegistry.post(new IssueCommentEdited(comment));
	}

	@Transactional
	@Override
	public void delete(User user, IssueComment comment) {
		dao.remove(comment);
		comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()-1);
		
		IssueChange change = new IssueChange();
		change.setIssue(comment.getIssue());
		change.setUser(user);
		change.setData(new IssueCommentRemoveData());
		changeService.create(change, null);
	}

	public void create(IssueComment comment) {
		create(comment, new ArrayList<>());
	}
	
	@Transactional
	@Override
	public void create(IssueComment comment, Collection<String> notifiedEmailAddresses) {
		Preconditions.checkState(comment.isNew());
		dao.persist(comment);
		comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()+1);
		listenerRegistry.post(new IssueCommentCreated(comment, notifiedEmailAddresses));
	}

	@Sessional
	@Override
	public List<IssueComment> query(User submitter, Date fromDate, Date toDate) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueComment> query = builder.createQuery(IssueComment.class);
		From<IssueComment, IssueComment> root = query.from(IssueComment.class);
		
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(builder.equal(root.get(IssueComment.PROP_USER), submitter));
		predicates.add(builder.greaterThanOrEqualTo(root.get(IssueComment.PROP_DATE), fromDate));
		predicates.add(builder.lessThanOrEqualTo(root.get(IssueComment.PROP_DATE), toDate));
			
		query.where(predicates.toArray(new Predicate[0]));
		
		return getSession().createQuery(query).getResultList();
	}

}
