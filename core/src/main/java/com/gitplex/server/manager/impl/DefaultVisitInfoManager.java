package com.gitplex.server.manager.impl;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.lifecycle.SystemStopping;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentCreated;
import com.gitplex.server.event.pullrequest.PullRequestCommentCreated;
import com.gitplex.server.event.pullrequest.PullRequestOpened;
import com.gitplex.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.VisitInfoManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.CodeCommentActivity;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.VersionUtils;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.EnvironmentConfig;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultVisitInfoManager implements VisitInfoManager {

	private static final int INFO_VERSION = 1;
	
	private static final String INFO_DIR = "visit";
	
	private static final String PULL_REQUEST_STORE = "pullRequest";
	
	private static final String CODE_COMMENT_STORE = "codeComment";

	private final Map<Long, Environment> envs = new HashMap<>();
	
	private final StorageManager storageManager;
	
	private final Dao dao;
	
	@Inject
	public DefaultVisitInfoManager(Dao dao, StorageManager storageManager) {
		this.dao = dao;
		this.storageManager = storageManager;
	}
	
	private synchronized Environment getEnv(Long projectId) {
		Environment env = envs.get(projectId);
		if (env == null) {
			File infoDir = getInfoDir(projectId);
			VersionUtils.checkInfoVersion(infoDir, INFO_VERSION);
			EnvironmentConfig config = new EnvironmentConfig();
			config.setEnvCloseForcedly(true);
			env = Environments.newInstance(infoDir, config);
			envs.put(projectId, env);
		}
		return env;
	}
	
	private File getInfoDir(Long projectId) {
		File infoDir = new File(storageManager.getProjectInfoDir(projectId), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}
	
	private Store getStore(Environment env, String storeName) {
		return env.computeInTransaction(new TransactionalComputable<Store>() {
		    @Override
		    public Store compute(Transaction txn) {
		        return env.openStore(storeName, StoreConfig.WITHOUT_DUPLICATES, txn);
		    }
		});		
	}

	private byte[] getBytes(@Nullable ByteIterable byteIterable) {
		if (byteIterable != null)
			return Arrays.copyOf(byteIterable.getBytesUnsafe(), byteIterable.getLength());
		else
			return null;
	}
	
	@Override
	public void visit(User user, PullRequest request) {
		Environment env = getEnv(request.getTargetProject().getId());
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
		Environment env = getEnv(comment.getRequest().getTargetProject().getId());
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
		Environment env = getEnv(request.getTargetProject().getId());
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
		Environment env = getEnv(comment.getRequest().getTargetProject().getId());
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

	private byte[] longToBytes(long value) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(value);
	    return buffer.array();
	}

	private long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip(); 
	    return buffer.getLong();
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
			Long projectId = event.getEntity().getId();
			dao.doAfterCommit(new Runnable() {

				@Override
				public void run() {
					synchronized (envs) {
						Environment env = envs.remove(projectId);
						if (env != null)
							env.close();
					}
				}
				
			});
		}
	}
	
	@Listen
	public void on(SystemStopping event) {
		synchronized (envs) {
			for (Environment env: envs.values())
				env.close();
		}
	}

	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}

	static class StringPairByteIterable extends ArrayByteIterable {
		StringPairByteIterable(String uuid1, String uuid2) {
			super(getBytes(uuid1, uuid2));
		}
		
		private static byte[] getBytes(String uuid1, String uuid2) {
			byte[] bytes1 = uuid1.getBytes();
			byte[] bytes2 = uuid2.getBytes();
			byte[] bytes = new byte[bytes1.length+bytes2.length];
			System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
			System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
			return bytes;
		}
	}

}
