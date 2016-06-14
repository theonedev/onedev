package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.manager.CodeCommentInfoManager;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.SequentialWorkManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.WorkManager;

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
public class DefaultCodeCommentInfoManager implements CodeCommentInfoManager, DepotListener, 
		LifecycleListener, CodeCommentListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultCodeCommentInfoManager.class);
	
	private static final String INFO_DIR = "codeCommentInfo";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final ByteIterable LAST_COMMENT_KEY = new StringByteIterable("lastComment");

	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final WorkManager workManager;
	
	private final SequentialWorkManager sequentialWorkManager;
	
	private final DepotManager depotManager;
	
	private final CodeCommentManager codeCommentManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Dao dao;
	
	private final Map<Long, Environment> envs = new HashMap<>();
	
	@Inject
	public DefaultCodeCommentInfoManager(Dao dao, DepotManager depotManager, StorageManager storageManager, 
			CodeCommentManager codeCommentManager, WorkManager workManager, 
			SequentialWorkManager sequentialWorkManager, UnitOfWork unitOfWork) {
		this.dao = dao;
		this.depotManager = depotManager;
		this.storageManager = storageManager;
		this.codeCommentManager = codeCommentManager;
		this.workManager = workManager;
		this.sequentialWorkManager = sequentialWorkManager;
		this.unitOfWork = unitOfWork;
	}
	
	private String getSequentialExecutorKey(Depot depot) {
		return "repository-" + depot.getId() + "-collectCodeCommentInfo";
	}
	
	private synchronized Environment getEnv(Depot depot) {
		Environment env = envs.get(depot.getId());
		if (env == null) {
			EnvironmentConfig config = new EnvironmentConfig();
			config.setLogCacheShared(false);
			config.setMemoryUsage(1024*1024*64);
			config.setLogFileSize(64*1024);
			env = Environments.newInstance(getInfoDir(depot), config);
			envs.put(depot.getId(), env);
		}
		return env;
	}
	
	private File getInfoDir(Depot depot) {
		File infoDir = new File(storageManager.getCacheDir(depot), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}
	
	private Store getStore(final Environment env, final String storeName) {
		return env.computeInTransaction(new TransactionalComputable<Store>() {
		    @Override
		    public Store compute(Transaction txn) {
		        return env.openStore(storeName, StoreConfig.WITHOUT_DUPLICATES, txn);
		    }
		});		
	}

	@Override
	public synchronized void onDeleteDepot(Depot depot) {
		sequentialWorkManager.removeExecutor(getSequentialExecutorKey(depot));
		Environment env = envs.remove(depot.getId());
		if (env != null)
			env.close();
		FileUtils.deleteDir(getInfoDir(depot));
	}
	
	@Override
	public void onRenameDepot(Depot renamedDepot, String oldName) {
	}

	private byte[] getBytes(@Nullable ByteIterable byteIterable) {
		if (byteIterable != null)
			return Arrays.copyOf(byteIterable.getBytesUnsafe(), byteIterable.getLength());
		else
			return null;
	}
	
	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}

	@Override
	public void systemStarting() {
	}

	@Override
	public void systemStarted() {
	}
	
	@Override
	public void collect(Depot depot) {
		Long depotId = depot.getId();
		sequentialWorkManager.execute(getSequentialExecutorKey(depot), new PrioritizedRunnable(PRIORITY) {

			@Override
			public void run() {
				try {
					workManager.submit(new PrioritizedRunnable(PRIORITY) {

						@Override
						public void run() {
							unitOfWork.call(new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									doCollect(depotManager.load(depotId));
									return null;
								}
								
							});
						}

					}).get();
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Error collecting comment information", e);
				}
			}

		});
	}
	
	private void doCollect(Depot depot) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);

		AtomicReference<String> lastComment = new AtomicReference<>();
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(final Transaction txn) {
				byte[] value = getBytes(store.get(txn, LAST_COMMENT_KEY));
				lastComment.set(value!=null?new String(value):null);									
			}
			
		});
		
		for (CodeComment comment: codeCommentManager.queryAfter(depot, lastComment.get())) {
			env.executeInTransaction(new TransactionalExecutable() {

				@SuppressWarnings("unchecked")
				@Override
				public void execute(Transaction txn) {
					ByteIterable key = new StringByteIterable(comment.getUUID());
					
					byte[] keyBytes = new byte[20];
					ObjectId.fromString(comment.getCommit()).copyRawTo(keyBytes, 0);
					ByteIterable commitKey = new ArrayByteIterable(keyBytes);
					byte[] valueBytes = getBytes(store.get(txn, commitKey));
					Map<String, String> comments;
					if (valueBytes != null)
						comments = (Map<String, String>) SerializationUtils.deserialize(valueBytes);
					else
						comments = new HashMap<>();
					if (comment.getCompareContext() != null)
						comments.put(comment.getUUID(), comment.getCompareContext().getCompareCommit());
					else
						comments.put(comment.getUUID(), null);
					store.add(txn, commitKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) comments)));
					store.add(txn, LAST_COMMENT_KEY, key);
				}
				
			});
		}
	}
	
	@Override
	public synchronized void systemStopping() {
		for (Environment env: envs.values())
			env.close();
	}

	@Override
	public void systemStopped() {
	}

	@Override
	public void onTransferDepot(Depot depot, Account oldAccount) {
	}

	@Override
	public void onSaveDepot(Depot depot) {
	}

	@Override
	public Map<String, String> getComments(Depot depot, ObjectId commit) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Map<String, String>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Map<String, String> compute(Transaction txn) {
				byte[] keyBytes = new byte[20];
				commit.copyRawTo(keyBytes, 0);
				ByteIterable commitKey = new ArrayByteIterable(keyBytes);
				byte[] valueBytes = getBytes(store.get(txn, commitKey));
				if (valueBytes != null) {
					return (Map<String, String>) SerializationUtils.deserialize(valueBytes);
				} else {
					return new HashMap<>();
				}
			}
		});
	}

	@Sessional
	@Override
	public void onSaveComment(CodeComment comment) {
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				collect(comment.getDepot());
			}
			
		});
	}

	@Override
	public void onDeleteComment(CodeComment comment) {
	}

	@Override
	public void removeComment(Depot depot, ObjectId commit, String comment) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {

			@SuppressWarnings("unchecked")
			@Override
			public void execute(Transaction txn) {
				byte[] keyBytes = new byte[20];
				commit.copyRawTo(keyBytes, 0);
				ByteIterable commitKey = new ArrayByteIterable(keyBytes);
				byte[] valueBytes = getBytes(store.get(txn, commitKey));
				Collection<String> storedComments;
				if (valueBytes != null)
					storedComments = (Collection<String>) SerializationUtils.deserialize(valueBytes);
				else
					storedComments = new HashSet<>();
				storedComments.remove(comment);
				store.add(txn, commitKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) storedComments)));
			}
			
		});
	}

}
