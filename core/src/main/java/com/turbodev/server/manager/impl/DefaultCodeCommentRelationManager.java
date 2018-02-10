package com.turbodev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.turbodev.launcher.loader.Listen;
import com.turbodev.launcher.loader.ListenerRegistry;
import com.turbodev.server.event.codecomment.CodeCommentCreated;
import com.turbodev.server.event.codecomment.CodeCommentReplied;
import com.turbodev.server.event.pullrequest.PullRequestCodeCommentCreated;
import com.turbodev.server.event.pullrequest.PullRequestCodeCommentReplied;
import com.turbodev.server.manager.CodeCommentRelationManager;
import com.turbodev.server.manager.PullRequestManager;
import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.CodeCommentRelation;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.EntityCriteria;

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

	private void save(CodeCommentRelation relation, boolean passive) {
		super.save(relation);
		CodeComment comment = relation.getComment();
		PullRequest request = relation.getRequest();
		if (request.getLastEvent() == null || comment.getDate().after(request.getLastEvent().getDate())) {
			PullRequestCodeCommentCreated event = new PullRequestCodeCommentCreated(request, comment, passive); 
			listenerRegistry.post(event);
			request.setLastEvent(event);
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
	public void on(CodeCommentCreated event) {
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
			relation.getRequest().setLastEvent(pullRequestCodeCommentReplied);
			pullRequestManager.save(relation.getRequest());
		}
	}
	
}
