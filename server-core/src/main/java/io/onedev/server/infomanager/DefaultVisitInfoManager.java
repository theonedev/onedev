package io.onedev.server.infomanager;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.storage.StorageManager;
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
public class DefaultVisitInfoManager extends AbstractMultiEnvironmentManager 
		implements VisitInfoManager, Serializable {

	private static final int INFO_VERSION = 6;
	
	private static final String INFO_DIR = "visit";
	
	private static final String PULL_REQUEST_STORE = "pullRequestVisit";
	
	private static final String PULL_REQUEST_CODE_COMMENT_STORE = "pullRequestCodeCommentsVisit";
	
	private static final String CODE_COMMENT_STORE = "codeCommentVisit";

	private static final String ISSUE_STORE = "issueVisit";
	
	private final StorageManager storageManager;
	
	private final ProjectManager projectManager;
	
	private final ClusterManager clusterManager;
	
	@Inject
	public DefaultVisitInfoManager(StorageManager storageManager, ProjectManager projectManager, 
			ClusterManager clusterManager) {
		this.storageManager = storageManager;
		this.projectManager = projectManager;
		this.clusterManager = clusterManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(VisitInfoManager.class);
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			UUID storageServerUUID = projectManager.getStorageServerUUID(projectId, false);
			if (storageServerUUID != null) {
				clusterManager.runOnServer(storageServerUUID, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() throws Exception {
						removeEnv(projectId.toString());
						return null;
					}
					
				});
			}
		} 
	}

	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(storageManager.getProjectInfoDir(Long.valueOf(envKey)), INFO_DIR);
		FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}
	
	@Override
	public void visitIssue(User user, Issue issue) {
		Long projectId = issue.getProject().getId();
		Long userId = user.getId();
		Long issueId = issue.getId();
		projectManager.submitToProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, ISSUE_STORE);
				env.executeInTransaction(new TransactionalExecutable() {
					
					@Override
					public void execute(Transaction txn) {
						long time = new DateTime().plusSeconds(1).getMillis();
						writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, issueId)), time);
					}
					
				});
				return null;
			}
			
		});
	}

	@Override
	public void visitPullRequest(User user, PullRequest request) {
		Long projectId = request.getProject().getId();
		Long userId = user.getId();
		Long requestId = request.getId();
		projectManager.submitToProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, PULL_REQUEST_STORE);
				env.executeInTransaction(new TransactionalExecutable() {
					
					@Override
					public void execute(Transaction txn) {
						long time = new DateTime().plusSeconds(1).getMillis();
						writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, requestId)), time);
					}
					
				});
				return null;
			}
			
		});
	}
	
	@Override
	public void visitCodeComment(User user, CodeComment comment) {
		Long projectId = comment.getProject().getId();
		Long userId = user.getId();
		Long commentId = comment.getId();
		projectManager.submitToProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, CODE_COMMENT_STORE);
				env.executeInTransaction(new TransactionalExecutable() {
					
					@Override
					public void execute(Transaction txn) {
						long time = new DateTime().plusSeconds(1).getMillis();
						writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, commentId)), time);
					}
					
				});
				return null;
			}
			
		});
	}

	@Override
	public void visitPullRequestCodeComments(User user, PullRequest request) {
		Long userId = user.getId();
		Long projectId = request.getProject().getId();
		Long requestId = request.getId();
		projectManager.submitToProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, PULL_REQUEST_CODE_COMMENT_STORE);
				env.executeInTransaction(new TransactionalExecutable() {
					
					@Override
					public void execute(Transaction txn) {
						long time = new DateTime().plusSeconds(1).getMillis();
						writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, requestId)), time);
					}
					
				});
				return null;
			}
			
		});
	}

	@Override
	public Date getPullRequestVisitDate(User user, PullRequest request) {
		Long userId = user.getId();
		Long requestId = request.getId();
		Long projectId = request.getProject().getId();
		return projectManager.runOnProjectServer(projectId, new ClusterTask<Date>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Date call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, PULL_REQUEST_STORE);
				return env.computeInTransaction(new TransactionalComputable<Date>() {
					
					@Override
					public Date compute(Transaction txn) {
						long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, requestId)), -1);
						if (millis != -1)
							return new Date(millis);
						else
							return null;
					}
					
				});
			}
			
		});
	}

	@Override
	public Date getIssueVisitDate(User user, Issue issue) {
		Long userId = user.getId();
		Long projectId = issue.getProject().getId();
		Long issueId = issue.getId();
		return projectManager.runOnProjectServer(projectId, new ClusterTask<Date>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Date call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, ISSUE_STORE);
				return env.computeInTransaction(new TransactionalComputable<Date>() {
					
					@Override
					public Date compute(Transaction txn) {
						long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, issueId)), -1);
						if (millis != -1)
							return new Date(millis);
						else
							return null;
					}
					
				});
			}
			
		});
	}
	
	@Override
	public Date getCodeCommentVisitDate(User user, CodeComment comment) {
		Long projectId = comment.getProject().getId();
		Long commentId = comment.getId();
		Long userId = user.getId();
		return projectManager.runOnProjectServer(projectId, new ClusterTask<Date>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Date call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, CODE_COMMENT_STORE);
				return env.computeInTransaction(new TransactionalComputable<Date>() {
					
					@Override
					public Date compute(Transaction txn) {
						long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, commentId)), -1);
						if (millis != -1)
							return new Date(millis);
						else
							return null;
					}
					
				});
			}
			
		});
	}
	
	@Override
	public Date getPullRequestCodeCommentsVisitDate(User user, PullRequest request) {
		Long userId = user.getId();
		Long projectId = request.getProject().getId();
		Long requestId = request.getId();
		return projectManager.runOnProjectServer(projectId, new ClusterTask<Date>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Date call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, PULL_REQUEST_CODE_COMMENT_STORE);
				return env.computeInTransaction(new TransactionalComputable<Date>() {
					
					@Override
					public Date compute(Transaction txn) {
						long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, requestId)), -1);
						if (millis != -1)
							return new Date(millis);
						else
							return null;
					}
					
				});
			}
			
		});
	}
	
	@Listen
	public void on(IssueEvent event) {
		if (event.getUser() != null)
			visitIssue(event.getUser(), event.getIssue());
	}
	
	@Listen
	public void on(CodeCommentEvent event) {
		if (event.getUser() != null)
			visitCodeComment(event.getUser(), event.getComment());
	}

	@Listen
	public void on(PullRequestEvent event) {
		if (event.getUser() != null) {
			visitPullRequest(event.getUser(), event.getRequest());
			if (event instanceof PullRequestCodeCommentEvent)
				visitPullRequestCodeComments(event.getUser(), event.getRequest());
		}
	}

}
