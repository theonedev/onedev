package com.gitplex.server.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.codecomment.CodeCommentActivityEvent;
import com.gitplex.server.event.codecomment.CodeCommentCreated;
import com.gitplex.server.event.codecomment.CodeCommentEvent;
import com.gitplex.server.event.codecomment.CodeCommentResolved;
import com.gitplex.server.event.codecomment.CodeCommentUnresolved;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CodeCommentStatusChangeManager;
import com.gitplex.server.manager.MailManager;
import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.UrlManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentRelation;
import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.CodeCommentActivity;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.util.markdown.MentionParser;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityManager<CodeComment> implements CodeCommentManager {

	private final ListenerRegistry listenerRegistry;
	
	private final CodeCommentStatusChangeManager codeCommentStatusChangeManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final MarkdownManager markdownManager;
	
	private final AccountManager accountManager;
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, ListenerRegistry listenerRegistry, MailManager mailManager, 
			CodeCommentStatusChangeManager codeCommentStatusChangeManager, UrlManager urlManager, 
			PullRequestManager pullRequestManager, MarkdownManager markdownManager, AccountManager accountManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.markdownManager = markdownManager;
		this.urlManager = urlManager;
		this.mailManager = mailManager;
		this.codeCommentStatusChangeManager = codeCommentStatusChangeManager;
		this.pullRequestManager = pullRequestManager;
		this.accountManager = accountManager;
	}

	@Sessional
	@Override
	public Collection<CodeComment> findAll(Depot depot, ObjectId commitId, String path) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		criteria.add(Restrictions.eq("commentPos.commit", commitId.name()));
		if (path != null)
			criteria.add(Restrictions.eq("commentPos.path", path));
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<CodeComment> findAll(Depot depot, ObjectId... commitIds) {
		Preconditions.checkArgument(commitIds.length > 0);
		
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		List<Criterion> criterions = new ArrayList<>();
		for (ObjectId commitId: commitIds) {
			criterions.add(Restrictions.eq("commentPos.commit", commitId.name()));
		}
		criteria.add(Restrictions.or(criterions.toArray(new Criterion[criterions.size()])));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void save(CodeComment comment) {
		CodeCommentCreated event;
		if (comment.isNew()) {
			event = new CodeCommentCreated(comment);
		} else {
			event = null;
		}
		dao.persist(comment);
		if (event != null) {
			listenerRegistry.post(event);
		}
	}

	@Transactional
	@Override
	public void delete(CodeComment comment) {
		dao.remove(comment);
	}

	@Sessional
	@Override
	public CodeComment find(String uuid) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("uuid", uuid));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<CodeComment> findAllAfter(Depot depot, String commentUUID) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("depot", depot));
		criteria.addOrder(Order.asc("id"));
		if (commentUUID != null) {
			CodeComment comment = find(commentUUID);
			if (comment != null) {
				criteria.add(Restrictions.gt("id", comment.getId()));
			}
		}
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void changeStatus(CodeCommentStatusChange statusChange) {
		statusChange.getComment().setResolved(statusChange.isResolved());
		
		codeCommentStatusChangeManager.save(statusChange);
		
		CodeCommentEvent event;
		if (statusChange.isResolved()) {
			event = new CodeCommentResolved(statusChange);
		} else {
			event = new CodeCommentUnresolved(statusChange);
		}
		listenerRegistry.post(event);
		statusChange.getComment().setLastEvent(event);
		save(statusChange.getComment());
	}

	@Transactional
	@Listen
	public void on(CodeCommentActivityEvent event) {
		CodeCommentActivity activity = event.getActivity();
		
		for (CodeCommentRelation relation: activity.getComment().getRelations()) {
			PullRequest request = relation.getRequest();
			PullRequestCodeCommentActivityEvent pullRequestCodeCommentActivityEvent = event.getPullRequestCodeCommentActivityEvent(request);
			listenerRegistry.post(pullRequestCodeCommentActivityEvent);
			request.setLastEvent(pullRequestCodeCommentActivityEvent);
			
			pullRequestManager.save(request);
		}
		
		if (activity.getComment().getRelations().isEmpty())
			sendNotifications(event);
	}

	@Override
	public void sendNotifications(CodeCommentEvent event) {
		if (event.getMarkdown() != null) {
			CodeComment comment = event.getComment();
			Collection<Account> mentionedUsers = new HashSet<>();
			for (Account user: new MentionParser().parseMentions(markdownManager.render(event.getMarkdown(), null, false))) {
				mentionedUsers.add(user);
			}
			String subject = "You are mentioned in a code comment on file " + comment.getCommentPos().getPath();
			String url;
			if (event instanceof CodeCommentCreated)
				url = urlManager.urlFor(((CodeCommentCreated)event).getComment(), null);
			else 
				url = urlManager.urlFor(((CodeCommentActivityEvent)event).getActivity(), null);
				
			String content = String.format(""
					+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
					+ "<p style='margin: 16px 0;'>"
					+ "For details, please visit <a href='%s'>%s</a>", 
					markdownManager.escape(event.getMarkdown()), url, url);
			
			mailManager.sendMailAsync(mentionedUsers.stream().map(Account::getEmail).collect(Collectors.toList()), 
					subject, decorateBody(subject + "." + content));
			
			Collection<Account> involvedUsers = new HashSet<>();
			RevCommit commit = comment.getDepot().getRevCommit(ObjectId.fromString(comment.getCommentPos().getCommit()));
			
			Account author = accountManager.find(commit.getAuthorIdent());
			if (author != null) 
				involvedUsers.add(author);
			involvedUsers.add(comment.getUser());
			involvedUsers.addAll(comment.getActivities().stream().map(CodeCommentActivity::getUser).collect(Collectors.toList()));
			involvedUsers.removeAll(mentionedUsers);
			involvedUsers.remove(event.getUser());
			
			subject = "You are involved in a code comment on file " + comment.getCommentPos().getPath();
			mailManager.sendMailAsync(involvedUsers.stream().map(Account::getEmail).collect(Collectors.toList()), 
					subject, decorateBody(subject + "." + content));
		}
	}
	
	private String decorateBody(String body) {
		return String.format(""
				+ "%s"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by GitPlex", 
				body);
	}		
	
}
