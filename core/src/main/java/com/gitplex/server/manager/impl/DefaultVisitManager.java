package com.gitplex.server.manager.impl;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentCreated;
import com.gitplex.server.event.pullrequest.PullRequestCommentCreated;
import com.gitplex.server.event.pullrequest.PullRequestOpened;
import com.gitplex.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.VisitManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.CodeCommentActivity;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.util.FileUtils;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultVisitManager extends AbstractEnvironmentManager implements VisitManager {

	private static final int INFO_VERSION = 1;
	
	private static final String INFO_DIR = "visit";
	
	private static final String PULL_REQUEST_STORE = "pullRequest";
	
	private static final String CODE_COMMENT_STORE = "codeComment";

	private final StorageManager storageManager;
	
	private final Dao dao;
	
	@Inject
	public DefaultVisitManager(Dao dao, StorageManager storageManager) {
		this.dao = dao;
		this.storageManager = storageManager;
	}
	
	@Override
	public void visit(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				store.put(txn, new StringPairByteIterable(user.getUUID(), request.getUUID()), 
						new ArrayByteIterable(longToBytes(System.currentTimeMillis()+1000L)));
			}
			
		});
	}

	@Override
	public void visit(User user, CodeComment comment) {
		Environment env = getEnv(comment.getRequest().getTargetProject().getId().toString());
		Store store = getStore(env, CODE_COMMENT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				store.put(txn, new StringPairByteIterable(user.getUUID(), comment.getUUID()), 
						new ArrayByteIterable(longToBytes(System.currentTimeMillis()+1000L)));
			}
			
		});
	}

	@Override
	public Date getVisitDate(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, PULL_REQUEST_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				byte[] bytes = getBytes(store.get(txn, new StringPairByteIterable(user.getUUID(), request.getUUID())));
				if (bytes != null)
					return new Date(bytesToLong(bytes));
				else
					return null;
			}
			
		});
	}

	@Override
	public Date getVisitDate(User user, CodeComment comment) {
		Environment env = getEnv(comment.getRequest().getTargetProject().getId().toString());
		Store store = getStore(env, CODE_COMMENT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				byte[] bytes = getBytes(store.get(txn, new StringPairByteIterable(user.getUUID(), comment.getUUID())));
				if (bytes != null)
					return new Date(bytesToLong(bytes));
				else
					return null;
			}
			
		});
	}

	@Listen
	public void on(PullRequestCodeCommentActivityEvent event) {
		CodeCommentActivity activity = event.getActivity();
		if (activity.getUser() != null) {
			visit(activity.getUser(), activity.getComment());
		}
	}

	@Listen
	public void on(PullRequestCodeCommentCreated event) {
		if (event.getComment().getUser() != null) 
			visit(event.getComment().getUser(), event.getComment());
	}

	@Listen
	public void on(PullRequestCommentCreated event) {
		visit(event.getComment().getUser(), event.getRequest());
	}
	
	@Listen
	public void on(PullRequestOpened event) {
		if (event.getRequest().getSubmitter() != null)
			visit(event.getRequest().getSubmitter(), event.getRequest());
	}
	
	@Listen
	public void on(PullRequestStatusChangeEvent event) {
		if (event.getUser() != null)
			visit(event.getUser(), event.getRequest());
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			dao.doAfterCommit(new Runnable() {

				@Override
				public void run() {
					removeEnv(event.getEntity().getId().toString());
				}
				
			});
		}
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
