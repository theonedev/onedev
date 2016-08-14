package com.pmease.gitplex.core.entity.persistlistener;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;
import org.hibernate.CallbackException;
import org.hibernate.type.Type;

import com.pmease.commons.hibernate.PersistListener;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.event.mention.AccountMentionedInCodeComment;
import com.pmease.gitplex.core.event.mention.AccountMentionedInPullRequest;
import com.pmease.gitplex.core.util.markdown.MentionParser;

@Singleton
public class AccountMentionGenerator implements PersistListener {

	private final MarkdownManager markdownManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public AccountMentionGenerator(MarkdownManager markdownManager, ListenerRegistry listenerRegistry) {
		this.markdownManager = markdownManager;
		this.listenerRegistry = listenerRegistry;
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		return false;
	}

	private Collection<Account> parseNewMentions(String content, String prevContent) {
		Collection<Account> newMentions;
		if (!Objects.equal(content, prevContent)) {
			MentionParser mentionParser = new MentionParser();
			if (content != null) {
				newMentions = mentionParser.parseMentions(markdownManager.parse(content));
			} else {
				newMentions = new HashSet<>();
			}
			if (prevContent != null)
				newMentions.removeAll(mentionParser.parseMentions(markdownManager.parse(prevContent)));
		} else {
			newMentions = new HashSet<>();
		}
		return newMentions;
	}
	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) throws CallbackException {
		if (entity instanceof PullRequest) {
			PullRequest request = (PullRequest) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("description")) {
					String description = (String) currentState[i];
					String prevDescription = (String) previousState[i];
					for (Account account: parseNewMentions(description, prevDescription)) {
						listenerRegistry.post(new AccountMentionedInPullRequest(
								request, account, description));
					}
					break;
				}
			}
		} else if (entity instanceof PullRequestComment) {
			PullRequestComment comment = (PullRequestComment) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) currentState[i];
					String prevContent = (String) previousState[i];
					for (Account account: parseNewMentions(content, prevContent)) {
						listenerRegistry.post(new AccountMentionedInPullRequest(
								comment.getRequest(), account, content));
					}
					break;
				}
			}
		} else if (entity instanceof PullRequestStatusChange) {
			PullRequestStatusChange statusChange = (PullRequestStatusChange) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("note")) {
					String note = (String) currentState[i];
					String prevNote = (String) previousState[i];
					for (Account account: parseNewMentions(note, prevNote)) {
						listenerRegistry.post(new AccountMentionedInPullRequest(
								statusChange.getRequest(), account, note));
					}
					break;
				}
			}
		} else if (entity instanceof CodeComment) {
			CodeComment comment = (CodeComment) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) currentState[i];
					String prevContent = (String) previousState[i];
					for (Account account: parseNewMentions(content, prevContent)) {
						listenerRegistry.post(new AccountMentionedInCodeComment(
								comment, account, content));
					}
					break;
				}
			}
		} else if (entity instanceof CodeCommentReply) {
			CodeCommentReply reply = (CodeCommentReply) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) currentState[i];
					String prevContent = (String) previousState[i];
					for (Account account: parseNewMentions(content, prevContent)) {
						listenerRegistry.post(new AccountMentionedInCodeComment(
								reply.getComment(), account, content));
					}
					break;
				}
			}
		} else if (entity instanceof CodeCommentStatusChange) {
			CodeCommentStatusChange statusChange = (CodeCommentStatusChange) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("note")) {
					String note = (String) currentState[i];
					String prevNote = (String) previousState[i];
					for (Account account: parseNewMentions(note, prevNote)) {
						listenerRegistry.post(new AccountMentionedInCodeComment(
								statusChange.getComment(), account, note));
					}
					break;
				}
			}
		} 
		
		return false;
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		if (entity instanceof PullRequest) {
			PullRequest request = (PullRequest) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("description")) {
					String description = (String) state[i];
					if (description != null) {
						String html = markdownManager.parse(description);
						Collection<Account> mentions = new MentionParser().parseMentions(html);
						for (Account user: mentions) {
							listenerRegistry.post(new AccountMentionedInPullRequest(
									request, user, description));
						}
					}
					break;
				}
			}
		} else if (entity instanceof PullRequestComment) {
			PullRequestComment comment = (PullRequestComment) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) state[i];
					String html = markdownManager.parse(content);
					Collection<Account> mentions = new MentionParser().parseMentions(html);
					for (Account user: mentions) {
						listenerRegistry.post(new AccountMentionedInPullRequest(
								comment.getRequest(), user, content));
					}
					break;
				}
			}
		} else if (entity instanceof PullRequestStatusChange) {
			PullRequestStatusChange statusChange = (PullRequestStatusChange) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("note")) {
					String note = (String) state[i];
					if (note != null) {
						String html = markdownManager.parse(note);
						Collection<Account> mentions = new MentionParser().parseMentions(html);
						for (Account user: mentions) {
							listenerRegistry.post(new AccountMentionedInPullRequest(
									statusChange.getRequest(), user, note));
						}
					}
					break;
				}
			}
		} else if (entity instanceof CodeComment) {
			CodeComment comment = (CodeComment) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) state[i];
					String html = markdownManager.parse(content);
					Collection<Account> mentions = new MentionParser().parseMentions(html);
					for (Account user: mentions) {
						listenerRegistry.post(new AccountMentionedInCodeComment(
								comment, user, content));
					}
					break;
				}
			}
		} else if (entity instanceof CodeCommentReply) {
			CodeCommentReply reply = (CodeCommentReply) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("content")) {
					String content = (String) state[i];
					String html = markdownManager.parse(content);
					Collection<Account> mentions = new MentionParser().parseMentions(html);
					for (Account user: mentions) {
						listenerRegistry.post(new AccountMentionedInCodeComment(
								reply.getComment(), user, content));
					}
					break;
				}
			}
		} else if (entity instanceof CodeCommentStatusChange) {
			CodeCommentStatusChange statusChange = (CodeCommentStatusChange) entity;
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("note")) {
					String note = (String) state[i];
					if (note != null) {
						String html = markdownManager.parse(note);
						Collection<Account> mentions = new MentionParser().parseMentions(html);
						for (Account user: mentions) {
							listenerRegistry.post(new AccountMentionedInCodeComment(
									statusChange.getComment(), user, note));
						}
					}
					break;
				}
			}
		} 
		
		return true;
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
	}

}
