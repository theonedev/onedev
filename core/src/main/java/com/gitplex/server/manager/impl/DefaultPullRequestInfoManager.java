package com.gitplex.server.manager.impl;

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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.depot.DepotDeleted;
import com.gitplex.server.event.lifecycle.SystemStopping;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.manager.BatchWorkManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.manager.PullRequestInfoManager;
import com.gitplex.server.manager.PullRequestUpdateManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityPersisted;
import com.gitplex.server.util.BatchWorker;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.concurrent.Prioritized;

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
public class DefaultPullRequestInfoManager implements PullRequestInfoManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestInfoManager.class);
	
	private static final String INFO_DIR = "pullRequest";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final ByteIterable LAST_UPDATE_KEY = new StringByteIterable("lastUpdate");

	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final DepotManager depotManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Dao dao;
	
	private final Map<Long, Environment> envs = new HashMap<>();
	
	@Inject
	public DefaultPullRequestInfoManager(Dao dao, DepotManager depotManager, StorageManager storageManager, 
			PullRequestUpdateManager pullRequestUpdateManager, BatchWorkManager batchWorkManager, 
			UnitOfWork unitOfWork) {
		this.dao = dao;
		this.depotManager = depotManager;
		this.storageManager = storageManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
	}
	
	private BatchWorker getBatchWorker(Depot depot) {
		Long depotId = depot.getId();
		return new BatchWorker("repository-" + depotId + "-collectPullRequestInfo") {

			@Override
			public void doWork(Collection<Prioritized> works) {
				unitOfWork.call(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						collect(depotManager.load(depotId));
						return null;
					}
					
				});
			}
			
		};
	}
	
	private Environment getEnv(Depot depot) {
		synchronized (envs) {
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

	@Transactional
	@Listen
	public void on(DepotDeleted event) {
		Long depotId = event.getDepot().getId();
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				synchronized (envs) {
					Environment env = envs.remove(depotId);
					if (env != null)
						env.close();
				}
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
	public void collect(Depot depot) {
		logger.debug("Collecting pull request info (repository: {})...", depot);
		
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);

		String lastUpdate = env.computeInTransaction(new TransactionalComputable<String>() {
			
			@Override
			public String compute(Transaction txn) {
				byte[] value = getBytes(store.get(txn, LAST_UPDATE_KEY));
				return value!=null?new String(value):null;									
			}
			
		});
		
		for (PullRequestUpdate update: pullRequestUpdateManager.findAllAfter(depot, lastUpdate)) {
			try (RevWalk revWalk = new RevWalk(depot.getRepository())) {
				List<ObjectId> commits = new ArrayList<>();
				RevCommit headCommit = GitUtils.parseCommit(revWalk, ObjectId.fromString(update.getHeadCommitHash()));
				RevCommit baseCommit = GitUtils.parseCommit(revWalk, ObjectId.fromString(update.getBaseCommitHash()));
				if (headCommit != null && baseCommit != null) {
					revWalk.markStart(headCommit);
					revWalk.markUninteresting(baseCommit);
					commits.add(baseCommit);
					revWalk.forEach(commit->commits.add(commit));
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
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}		
		}
		logger.debug("Pull request info collected (repository: {})", depot);
	}
	
	@Listen
	public void on(SystemStopping event) {
		synchronized (envs) {
			for (Environment env: envs.values())
				env.close();
		}
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
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.isNew() && event.getEntity() instanceof PullRequestUpdate) {
			PullRequestUpdate update = (PullRequestUpdate) event.getEntity();
			dao.doAfterCommit(new Runnable() {

				@Override
				public void run() {
					Depot depot = update.getRequest().getTargetDepot();
					batchWorkManager.submit(getBatchWorker(depot), new Prioritized(PRIORITY));
				}
				
			});
		}
	}

	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}

}
