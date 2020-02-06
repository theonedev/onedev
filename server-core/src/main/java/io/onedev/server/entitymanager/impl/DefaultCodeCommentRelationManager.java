package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.CodeCommentRelationManager;
import io.onedev.server.event.codecomment.CodeCommentCreated;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentCreated;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentReplied;
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
	
	@Inject
	public DefaultCodeCommentRelationManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Override
	public CodeCommentRelation find(PullRequest request, CodeComment comment) {
		EntityCriteria<CodeCommentRelation> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", request));
		criteria.add(Restrictions.eq("comment", comment));
		return find(criteria);
	}

	private void save(CodeCommentRelation relation, boolean derived) {
		PullRequest request = relation.getRequest();
		if (relation.isNew())
			request.setCommentCount(request.getCommentCount() + relation.getComment().getReplyCount() + 1);
		super.save(relation);
		CodeComment comment = relation.getComment();
		if (comment.getCreateDate().after(request.getLastUpdate().getDate())) {
			PullRequestCodeCommentCreated event = new PullRequestCodeCommentCreated(request, comment, derived); 
			listenerRegistry.post(event);
		}
	}
	
	@Transactional
	@Override
	public void save(CodeCommentRelation relation) {
		save(relation, true);
	}
	
	@Transactional
	@Override
	public void delete(CodeCommentRelation relation) {
		super.delete(relation);
		PullRequest request = relation.getRequest();
		request.setCommentCount(request.getCommentCount() - relation.getComment().getReplyCount() - 1);
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
		}
	}
	
}
