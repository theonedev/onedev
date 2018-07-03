package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.launcher.loader.Listen;
import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.codecomment.CodeCommentAdded;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentAdded;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentReplied;
import io.onedev.server.manager.CodeCommentRelationManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentRelation;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultCodeCommentRelationManager extends AbstractEntityManager<CodeCommentRelation> 
		implements CodeCommentRelationManager {

	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultCodeCommentRelationManager(Dao dao, ListenerRegistry listenerRegistry, 
			PullRequestManager pullRequestManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.pullRequestManager = pullRequestManager;
	}

	@Override
	public CodeCommentRelation find(PullRequest request, CodeComment comment) {
		EntityCriteria<CodeCommentRelation> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", request));
		criteria.add(Restrictions.eq("comment", comment));
		return find(criteria);
	}

	private void save(CodeCommentRelation relation, boolean derived) {
		super.save(relation);
		CodeComment comment = relation.getComment();
		PullRequest request = relation.getRequest();
		if (request.getLastActivity() == null || comment.getDate().after(request.getLastActivity().getDate())) {
			PullRequestCodeCommentAdded event = new PullRequestCodeCommentAdded(request, comment, derived); 
			listenerRegistry.post(event);
			request.setLastActivity(event);
			pullRequestManager.save(request);
		}
	}
	
	@Transactional
	@Override
	public void save(CodeCommentRelation relation) {
		save(relation, true);
	}
	
	@Listen
	@Transactional
	public void on(CodeCommentAdded event) {
		if (event.getRequest() != null) {
			CodeCommentRelation relation = new CodeCommentRelation();
			relation.setComment(event.getComment());
			relation.setRequest(event.getRequest());
			save(relation, false);
		}
	}
	
	@Listen
	@Transactional
	public void on(CodeCommentReplied event) {
		for (CodeCommentRelation relation: event.getComment().getRelations()) {
			PullRequestCodeCommentReplied pullRequestCodeCommentReplied = new PullRequestCodeCommentReplied(
					relation.getRequest(), event.getReply(), !relation.getRequest().equals(event.getRequest())); 
			listenerRegistry.post(pullRequestCodeCommentReplied);
			relation.getRequest().setLastActivity(pullRequestCodeCommentReplied);
			pullRequestManager.save(relation.getRequest());
		}
	}
	
}
