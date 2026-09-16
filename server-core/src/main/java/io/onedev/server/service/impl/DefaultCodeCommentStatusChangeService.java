package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import com.google.common.base.Preconditions;

import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.codecomment.CodeCommentStatusChanged;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentStatusChanged;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.CodeCommentStatusChangeService;

@Singleton
public class DefaultCodeCommentStatusChangeService extends BaseEntityService<CodeCommentStatusChange>
		implements CodeCommentStatusChangeService {

	@Inject
	private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void create(CodeCommentStatusChange change, String note) {
		Preconditions.checkState(change.isNew());
		
		CodeComment comment = change.getComment();
		if (comment.isResolved() == change.isResolved())
			throw new NotAcceptableException("Code comment is already " + (change.isResolved() ? "resolved" : "unresolved"));

		comment.setResolved(change.isResolved());
		
		dao.persist(change);
		
		if (note != null) {
			CodeCommentReply reply = new CodeCommentReply();
			reply.setComment(comment);
			reply.setCompareContext(change.getCompareContext());
			reply.setContent(note);
			reply.setDate(new Date(change.getDate().getTime() - 100));
			reply.setUser(change.getUser());
			dao.persist(reply);
			
			comment.setReplyCount(comment.getReplyCount()+1);
		}
		listenerRegistry.post(new CodeCommentStatusChanged(change, note));
		
		PullRequest request = comment.getCompareContext().getPullRequest();
		if (request != null) 
			listenerRegistry.post(new PullRequestCodeCommentStatusChanged(request, change, note));
	}
	
	@Transactional
	@Override
	public void create(Collection<CodeCommentStatusChange> changes, String note) {
		for (CodeCommentStatusChange  change: changes)
			create(change, note);
	}

	@Sessional
	@Override
	public List<CodeCommentStatusChange> query(User creator, Date fromDate, Date toDate) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<CodeCommentStatusChange> query = builder.createQuery(CodeCommentStatusChange.class);
		From<CodeCommentStatusChange, CodeCommentStatusChange> root = query.from(CodeCommentStatusChange.class);
		
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(builder.equal(root.get(CodeCommentStatusChange.PROP_USER), creator));
		predicates.add(builder.greaterThanOrEqualTo(root.get(CodeCommentStatusChange.PROP_DATE), fromDate));
		predicates.add(builder.lessThanOrEqualTo(root.get(CodeCommentStatusChange.PROP_DATE), toDate));
			
		query.where(predicates.toArray(new Predicate[0]));
		
		return getSession().createQuery(query).getResultList();
	}
	
}
