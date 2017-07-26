package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.codecomment.CodeCommentCreated;
import com.gitplex.server.event.codecomment.CodeCommentReplied;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentCreated;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentReplied;
import com.gitplex.server.manager.CodeCommentRelationManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentRelation;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

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
