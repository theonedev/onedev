package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.listener.PullRequestUpdateListener;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.PullRequestInfoManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
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
public class DefaultPullRequestInfoManager implements PullRequestInfoManager, DepotListener, 
		LifecycleListener, PullRequestUpdateListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestInfoManager.class);
	
	private static final String INFO_DIR = "pullRequest";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final ByteIterable LAST_UPDATE_KEY = new StringByteIterable("lastUpdate");

	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final WorkManager workManager;
	
	private final SequentialWorkManager sequentialWorkManager;
	
	private final DepotManager depotManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Dao dao;
	
	private final Map<Long, Environment> envs = new HashMap<>();
	
	@Inject
	public DefaultPullRequestInfoManager(Dao dao, DepotManager depotManager, StorageManager storageManager, 
			PullRequestUpdateManager pullRequestUpdateManager, WorkManager workManager, 
			SequentialWorkManager sequentialWorkManager, UnitOfWork unitOfWork) {
		this.dao = dao;
		this.depotManager = depotManager;
		this.storageManager = storageManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.workManager = workManager;
		this.sequentialWorkManager = sequentialWorkManager;
		this.unitOfWork = unitOfWork;
	}
	
	private String getSequentialExecutorKey(Depot depot) {
		return "repository-" + depot.getId() + "-collectPullRequestInfo";
	}
	
	private synchronized Environment getEnv(Depot depot) {
		Environment env = envs.get(depot.getId());
		if (env == null) {
			EnvironmentConfig config = new EnvironmentConfig();
			config.setLogCacheShared(false);
			config.setMemoryUsage(1024*1024*16);
			env = Environments.newInstance(getInfoDir(depot), config);
			envs.put(depot.getId(), env);
		}
		return env;
	}
	
	private File getInfoDir(Depot depot) {
		File infoDir = new File(storageManager.getInfoDir(depot), INFO_DIR);
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
					logger.error("Error collecting pull request information", e);
				}
			}

		});
	}
	
	private void doCollect(Depot depot) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);

		String lastUpdate = env.computeInTransaction(new TransactionalComputable<String>() {
			
			@Override
			public String compute(Transaction txn) {
				byte[] value = getBytes(store.get(txn, LAST_UPDATE_KEY));
				return value!=null?new String(value):null;									
			}
			
		});
		
		for (PullRequestUpdate update: pullRequestUpdateManager.queryAfter(depot, lastUpdate)) {
			try (RevWalk revWalk = new RevWalk(depot.getRepository())) {
				List<ObjectId> commits = new ArrayList<>();
				revWalk.markStart(revWalk.lookupCommit(ObjectId.fromString(update.getHeadCommitHash())));
				revWalk.markUninteresting(revWalk.lookupCommit(ObjectId.fromString(update.getBaseCommitHash())));
				revWalk.forEach(commit->commits.add(commit.getId().copy()));
				env.executeInTransaction(new TransactionalExecutable() {

					@SuppressWarnings("unchecked")
					@Override
					public void execute(Transaction txn) {
						ByteIterable key = new StringByteIterable(update.getUUID());
						for (ObjectId commit: commits) {
							byte[] keyBytes = new byte[20];
							commit.copyRawTo(keyBytes, 0);
							ByteIterable commitKey = new ArrayByteIterable(keyBytes);
							byte[] valueBytes = getBytes(store.get(txn, commitKey));
							Collection<String> requests;
							if (valueBytes != null)
								requests = (Collection<String>) SerializationUtils.deserialize(valueBytes);
							else
								requests = new HashSet<>();
							requests.add(update.getRequest().getUUID());
							store.put(txn, commitKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) requests)));
						}
						store.put(txn, LAST_UPDATE_KEY, key);
					}
					
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}		
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
	public Collection<String> getRequests(Depot depot, ObjectId commit) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Collection<String>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Collection<String> compute(Transaction txn) {
				byte[] keyBytes = new byte[20];
				commit.copyRawTo(keyBytes, 0);
				ByteIterable commitKey = new ArrayByteIterable(keyBytes);
				byte[] valueBytes = getBytes(store.get(txn, commitKey));
				if (valueBytes != null) {
					return (Collection<String>) SerializationUtils.deserialize(valueBytes);
				} else {
					return new HashSet<>();
				}
			}
		});
	}

	@Sessional
	@Override
	public void onSaveUpdate(PullRequestUpdate update) {
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				collect(update.getRequest().getTargetDepot());
			}
			
		});
	}

	@Override
	public void removeRequest(Depot depot, Collection<ObjectId> commits, String request) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {

			@SuppressWarnings("unchecked")
			@Override
			public void execute(Transaction txn) {
				for (ObjectId commit: commits) {
					byte[] keyBytes = new byte[20];
					commit.copyRawTo(keyBytes, 0);
					ByteIterable commitKey = new ArrayByteIterable(keyBytes);
					byte[] valueBytes = getBytes(store.get(txn, commitKey));
					Collection<String> storedRequests;
					if (valueBytes != null)
						storedRequests = (Collection<String>) SerializationUtils.deserialize(valueBytes);
					else
						storedRequests = new HashSet<>();
					storedRequests.remove(request);
					store.put(txn, commitKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) storedRequests)));
				}
			}
			
		});
	}
	
	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}

}
