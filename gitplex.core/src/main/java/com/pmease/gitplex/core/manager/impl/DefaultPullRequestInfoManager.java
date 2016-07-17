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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.concurrent.Prioritized;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.BatchWorkManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.PullRequestInfoManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.support.BatchWorker;

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
		LifecycleListener, PullRequestListener {

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

	@Override
	public void onDeleteDepot(Depot depot) {
		batchWorkManager.remove(getBatchWorker(depot));
		synchronized (envs) {
			Environment env = envs.remove(depot.getId());
			if (env != null)
				env.close();
		}
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
	
	@Override
	public void systemStopping() {
		synchronized (envs) {
			for (Environment env: envs.values())
				env.close();
		}
	}

	@Override
	public void systemStopped() {
	}

	@Override
	public void onTransferDepot(Depot depot, Account oldAccount) {
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
	
	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}

	@Override
	public void onOpenRequest(PullRequest request) {
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				Depot depot = request.getTargetDepot();
				batchWorkManager.submit(getBatchWorker(depot), new Prioritized(PRIORITY));
			}
			
		});
	}

	@Override
	public void onDeleteRequest(PullRequest request) {
	}

	@Override
	public void onReopenRequest(PullRequest request, Account user, String comment) {
	}

	@Override
	public void onUpdateRequest(PullRequestUpdate update) {
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				Depot depot = update.getRequest().getTargetDepot();
				batchWorkManager.submit(getBatchWorker(depot), new Prioritized(PRIORITY));
			}
			
		});
	}

	@Override
	public void onMentionAccount(PullRequest request, Account account) {
	}

	@Override
	public void onMentionAccount(PullRequestComment comment, Account account) {
	}

	@Override
	public void onCommentRequest(PullRequestComment comment) {
	}

	@Override
	public void onReviewRequest(Review review, String comment) {
	}

	@Override
	public void onAssignRequest(PullRequest request, Account user) {
	}

	@Override
	public void onVerifyRequest(PullRequest request) {
	}

	@Override
	public void onIntegrateRequest(PullRequest request, Account user, String comment) {
	}

	@Override
	public void onDiscardRequest(PullRequest request, Account user, String comment) {
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
	}

	@Override
	public void onInvitingReview(ReviewInvitation invitation) {
	}

	@Override
	public void pendingIntegration(PullRequest request) {
	}

	@Override
	public void pendingUpdate(PullRequest request) {
	}

	@Override
	public void pendingApproval(PullRequest request) {
	}

	@Override
	public void onRestoreSourceBranch(PullRequest request) {
	}

	@Override
	public void onDeleteSourceBranch(PullRequest request) {
	}

	@Override
	public void onWithdrawReview(Review review, Account user) {
	}

}
