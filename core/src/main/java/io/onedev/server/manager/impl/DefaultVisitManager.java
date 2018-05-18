package io.onedev.server.manager.impl;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.pullrequest.PullRequestCommented;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.event.pullrequest.PullRequestStatusChangeEvent;
import io.onedev.server.manager.StorageManager;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityRemoved;
import io.onedev.utils.FileUtils;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultVisitManager extends AbstractEnvironmentManager implements VisitManager {

	private static final int INFO_VERSION = 5;
	
	private static final String INFO_DIR = "visit";
	
	private static final String PULL_REQUEST_STORE = "pullRequest";
	
	private static final String PULL_REQUEST_CODE_COMMENTS_STORE = "pullRequestCodeComments";
	
	private static final String CODE_COMMENT_STORE = "codeComment";

	private static final String ISSUE_STORE = "issue";
	
	private final StorageManager storageManager;
	
	@Inject
	public DefaultVisitManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}
	
	@Override
	public void visitIssue(User user, Issue issue) {
		visitIssue(user, issue, new Date(System.currentTimeMillis()+1000L));
	}

	@Override
	public void visitIssue(User user, Issue issue, Date date) {
		Environment env = getEnv(issue.getProject().getId().toString());
		Store store = getStore(env, ISSUE_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				long millis = readLong(store, txn, new StringPairByteIterable(user.getUUID(), issue.getUUID()), -1);
				if (millis < date.getTime())
					writeLong(store, txn, new StringPairByteIterable(user.getUUID(), issue.getUUID()), date.getTime());
			}
			
		});
	}
	
	@Override
	public void visitPullRequest(User user, PullRequest request) {
		visitPullRequest(user, request, new Date(System.currentTimeMillis()+1000L));
	}
	
	@Override
	public void visitPullRequest(User user, PullRequest request, Date date) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				long millis = readLong(store, txn, new StringPairByteIterable(user.getUUID(), request.getUUID()), -1);
				if (millis < date.getTime())
					writeLong(store, txn, new StringPairByteIterable(user.getUUID(), request.getUUID()), date.getTime());
			}
			
		});
	}
	
	@Override
	public void visitPullRequestCodeComments(User user, PullRequest request) {
		visitPullRequestCodeComments(user, request, new Date(System.currentTimeMillis()+1000L));
	}
	
	@Override
	public void visitPullRequestCodeComments(User user, PullRequest request, Date date) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_CODE_COMMENTS_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				long millis = readLong(store, txn, new StringPairByteIterable(user.getUUID(), request.getUUID()), -1);
				if (millis < date.getTime())
					writeLong(store, txn, new StringPairByteIterable(user.getUUID(), request.getUUID()), date.getTime());
			}
			
		});
	}
	
	@Override
	public void visitCodeComment(User user, CodeComment comment) {
		visitCodeComment(user, comment, new Date(System.currentTimeMillis()+1000L));
	}
	
	@Override
	public void visitCodeComment(User user, CodeComment comment, Date date) {
		Environment env = getEnv(comment.getProject().getId().toString());
		Store store = getStore(env, CODE_COMMENT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				long millis = readLong(store, txn, new StringPairByteIterable(user.getUUID(), comment.getUUID()), -1);
				if (millis < date.getTime())
					writeLong(store, txn, new StringPairByteIterable(user.getUUID(), comment.getUUID()), date.getTime());
			}
			
		});
	}

	@Override
	public Date getPullRequestVisitDate(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new StringPairByteIterable(user.getUUID(), request.getUUID()), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			}
			
		});
	}

	@Override
	public Date getIssueVisitDate(User user, Issue issue) {
		Environment env = getEnv(issue.getProject().getId().toString());
		Store store = getStore(env, ISSUE_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new StringPairByteIterable(user.getUUID(), issue.getUUID()), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			}
			
		});
	}
	
	@Override
	public Date getPullRequestCodeCommentsVisitDate(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_CODE_COMMENTS_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new StringPairByteIterable(user.getUUID(), request.getUUID()), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			}
			
		});
	}
	
	@Override
	public Date getCodeCommentVisitDate(User user, CodeComment comment) {
		Environment env = getEnv(comment.getProject().getId().toString());
		Store store = getStore(env, CODE_COMMENT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new StringPairByteIterable(user.getUUID(), comment.getUUID()), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			}
			
		});
	}

	@Listen
	public void on(CodeCommentEvent event) {
		visitCodeComment(event.getUser(), event.getComment());
	}

	@Listen
	public void on(PullRequestCommented event) {
		visitPullRequest(event.getUser(), event.getRequest());
	}
	
	@Listen
	public void on(PullRequestCodeCommentEvent event) {
		if (!event.isPassive())
			visitPullRequest(event.getUser(), event.getRequest());
	}
	
	@Listen
	public void on(PullRequestOpened event) {
		if (event.getRequest().getSubmitter() != null)
			visitPullRequest(event.getRequest().getSubmitter(), event.getRequest());
	}
	
	@Listen
	public void on(PullRequestStatusChangeEvent event) {
		if (event.getUser() != null)
			visitPullRequest(event.getUser(), event.getRequest());
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project)
			removeEnv(event.getEntity().getId().toString());
	}
	
	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(storageManager.getProjectInfoDir(Long.valueOf(envKey)), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

}
