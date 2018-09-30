package io.onedev.server.manager.impl;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.pullrequest.PullRequestActionEvent;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.pullrequest.PullRequestCommentAdded;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.manager.StorageManager;
import io.onedev.server.manager.UserInfoManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

/**
 * Store project visit information here as we only need to load a single database to sort projects based on user 
 * visit information
 * 
 * @author robin
 *
 */
@Singleton
public class DefaultUserInfoManager extends AbstractEnvironmentManager implements UserInfoManager {

	private static final int INFO_VERSION = 5;
	
	private static final String PROJECT_VISIT_STORE = "projectVisit";
	
	private static final String PULL_REQUEST_VISIT_STORE = "pullRequestVisit";
	
	private static final String PULL_REQUEST_NOTIFICATION_STORE = "pullRequestNotification";
	
	private static final String PULL_REQUEST_CODE_COMMENTS_VISIT_STORE = "pullRequestCodeCommentsVisit";
	
	private static final String CODE_COMMENT_VISIT_STORE = "codeCommentVisit";

	private static final String ISSUE_VISIT_STORE = "issueVisit";
	
	private static final String ISSUE_NOTIFICATION_STORE = "issueNotification";
	
	private final StorageManager storageManager;
	
	@Inject
	public DefaultUserInfoManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}
	
	@Override
	public void visit(User user, Project project) {
		Environment env = getEnv(user.getId().toString());
		Store store = getStore(env, PROJECT_VISIT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				writeLong(store, txn, new LongByteIterable(project.getId()), System.currentTimeMillis()+1000L);
			}
			
		});
	}

	@Override
	public Date getVisitDate(UserFacade user, ProjectFacade project) {
		Environment env = getEnv(user.getId().toString());
		Store store = getStore(env, PROJECT_VISIT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new LongByteIterable(project.getId()), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			}
			
		});
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof User)
			removeEnv(event.getEntity().getId().toString());
	}

	@Override
	protected File getEnvDir(String envKey) {
		return storageManager.getUserInfoDir(Long.valueOf(envKey));
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}
	
	@Override
	public void visitIssue(User user, Issue issue) {
		Environment env = getEnv(issue.getProject().getId().toString());
		Store store = getStore(env, ISSUE_VISIT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				long time = new DateTime().plusSeconds(1).getMillis();
				writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), issue.getId())), time);
			}
			
		});
		
		setIssueNotified(user, issue, false);
	}

	@Override
	public void visitPullRequest(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_VISIT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				long time = new DateTime().plusSeconds(1).getMillis();
				writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), request.getId())), time);
			}
			
		});
		
		setPullRequestNotified(user, request, false);
	}
	
	@Override
	public void visitPullRequestCodeComments(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_CODE_COMMENTS_VISIT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				long time = new DateTime().plusSeconds(1).getMillis();
				writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), request.getId())), time);
			}
			
		});
	}
	
	@Override
	public void visitCodeComment(User user, CodeComment comment) {
		Environment env = getEnv(comment.getProject().getId().toString());
		Store store = getStore(env, CODE_COMMENT_VISIT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				long time = new DateTime().plusSeconds(1).getMillis();
				writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), comment.getId())), time);
			}
			
		});
	}

	@Override
	public Date getPullRequestVisitDate(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_VISIT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), request.getId())), -1);
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
		Store store = getStore(env, ISSUE_VISIT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), issue.getId())), -1);
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
		Store store = getStore(env, PULL_REQUEST_CODE_COMMENTS_VISIT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), request.getId())), -1);
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
		Store store = getStore(env, CODE_COMMENT_VISIT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), comment.getId())), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			}
			
		});
	}

	@Listen
	public void on(IssueEvent event) {
		visitIssue(event.getUser(), event.getIssue());
	}
	
	@Listen
	public void on(CodeCommentEvent event) {
		visitCodeComment(event.getUser(), event.getComment());
	}

	@Listen
	public void on(PullRequestCommentAdded event) {
		visitPullRequest(event.getUser(), event.getRequest());
	}
	
	@Listen
	public void on(PullRequestCodeCommentEvent event) {
		if (!event.isDerived()) {
			visitPullRequest(event.getUser(), event.getRequest());
			visitPullRequestCodeComments(event.getUser(), event.getRequest());
		}
	}
	
	@Listen
	public void on(PullRequestOpened event) {
		if (event.getRequest().getSubmitter() != null)
			visitPullRequest(event.getRequest().getSubmitter(), event.getRequest());
	}
	
	@Listen
	public void on(PullRequestActionEvent event) {
		if (event.getUser() != null)
			visitPullRequest(event.getUser(), event.getRequest());
	}

	@Override
	public boolean isNotified(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_NOTIFICATION_STORE);
		return env.computeInTransaction(new TransactionalComputable<Boolean>() {
			
			@Override
			public Boolean compute(Transaction txn) {
				return readBoolean(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), request.getId())), false);
			}
			
		});
	}

	@Override
	public boolean isNotified(User user, Issue issue) {
		Environment env = getEnv(issue.getProject().getId().toString());
		Store store = getStore(env, ISSUE_NOTIFICATION_STORE);
		return env.computeInTransaction(new TransactionalComputable<Boolean>() {
			
			@Override
			public Boolean compute(Transaction txn) {
				return readBoolean(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), issue.getId())), false);
			}
			
		});
	}

	@Override
	public void setPullRequestNotified(User user, PullRequest request, boolean notified) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		
		Store store = getStore(env, PULL_REQUEST_NOTIFICATION_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				writeBoolean(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), request.getId())), notified);
			}
			
		});
	}

	@Override
	public void setIssueNotified(User user, Issue issue, boolean notified) {
		Environment env = getEnv(issue.getProject().getId().toString());
		
		Store store = getStore(env, ISSUE_NOTIFICATION_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				writeBoolean(store, txn, new LongsByteIterable(Lists.newArrayList(user.getId(), issue.getId())), notified);
			}
			
		});
	}

}
