package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

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

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.concurrent.Prioritized;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.component.CompareContext;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.manager.BatchWorkManager;
import com.pmease.gitplex.core.manager.CodeCommentInfoManager;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.support.BatchWorker;

@Singleton
public class DefaultCodeCommentInfoManager implements CodeCommentInfoManager, DepotListener, 
		LifecycleListener, CodeCommentListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultCodeCommentInfoManager.class);
	
	private static final String INFO_DIR = "codeComment";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final ByteIterable LAST_COMMENT_KEY = new StringByteIterable("lastComment");
	
	private static final ByteIterable FILES_KEY = new StringByteIterable("files");

	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final DepotManager depotManager;
	
	private final CodeCommentManager codeCommentManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Dao dao;
	
	private final Map<Long, Environment> envs = new HashMap<>();
	
	private final Map<Long, List<String>> filesCache = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultCodeCommentInfoManager(Dao dao, DepotManager depotManager, 
			StorageManager storageManager, CodeCommentManager codeCommentManager, 
			BatchWorkManager batchWorkManager, UnitOfWork unitOfWork) {
		this.dao = dao;
		this.depotManager = depotManager;
		this.storageManager = storageManager;
		this.codeCommentManager = codeCommentManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
	}
	
	private BatchWorker getBatchWorker(Depot depot) {
		Long depotId = depot.getId();
		return new BatchWorker("repository-" + depotId + "-collectCodeCommentInfo") {

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
		filesCache.remove(depot.getId());
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
		logger.debug("Collecting code comment info (depot: {})", depot);
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);

		String lastComment = env.computeInTransaction(new TransactionalComputable<String>() {
			
			@Override
			public String compute(final Transaction txn) {
				byte[] value = getBytes(store.get(txn, LAST_COMMENT_KEY));
				return value!=null?new String(value):null;									
			}
			
		});
		
		for (CodeComment comment: codeCommentManager.queryAfter(depot, lastComment)) {
			env.executeInTransaction(new TransactionalExecutable() {

				@SuppressWarnings("unchecked")
				@Override
				public void execute(Transaction txn) {
					ByteIterable key = new StringByteIterable(comment.getUUID());
					
					byte[] keyBytes = new byte[20];
					ObjectId.fromString(comment.getCommentPos().getCommit()).copyRawTo(keyBytes, 0);
					ByteIterable commitKey = new ArrayByteIterable(keyBytes);
					byte[] valueBytes = getBytes(store.get(txn, commitKey));
					Map<String, CompareContext> comments;
					if (valueBytes != null)
						comments = (Map<String, CompareContext>) SerializationUtils.deserialize(valueBytes);
					else
						comments = new HashMap<>();
					comments.put(comment.getUUID(), comment.getCompareContext());
					store.put(txn, commitKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) comments)));
					
					if (comment.getCommentPos().getPath() != null) {
						Set<String> files;
						valueBytes = getBytes(store.get(txn, FILES_KEY));
						if (valueBytes != null)
							files = (Set<String>) SerializationUtils.deserialize(valueBytes);
						else
							files = new HashSet<String>();
						files.add(comment.getCommentPos().getPath());
						store.put(txn, FILES_KEY, new ArrayByteIterable(SerializationUtils.serialize((Serializable) files)));
						
						filesCache.remove(depot.getId());
					}
					
					store.put(txn, LAST_COMMENT_KEY, key);
				}
				
			});
		}
		logger.debug("Code comment info collected (depot: {})", depot);
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
	public void onSaveDepot(Depot depot) {
	}

	@Override
	public Map<String, CompareContext> getComments(Depot depot, ObjectId commit) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Map<String, CompareContext>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Map<String, CompareContext> compute(Transaction txn) {
				byte[] keyBytes = new byte[20];
				commit.copyRawTo(keyBytes, 0);
				ByteIterable commitKey = new ArrayByteIterable(keyBytes);
				byte[] valueBytes = getBytes(store.get(txn, commitKey));
				if (valueBytes != null) {
					return (Map<String, CompareContext>) SerializationUtils.deserialize(valueBytes);
				} else {
					return new HashMap<>();
				}
			}
		});
	}

	@Sessional
	@Override
	public void onSaveComment(CodeComment comment) {
		if (comment.isNew()) {
			dao.afterCommit(new Runnable() {
	
				@Override
				public void run() {
					batchWorkManager.submit(getBatchWorker(comment.getDepot()), new Prioritized(PRIORITY));
				}
				
			});
		}
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
				Map<String, CompareContext> storedComments;
				if (valueBytes != null)
					storedComments = (Map<String, CompareContext>) SerializationUtils.deserialize(valueBytes);
				else
					storedComments = new HashMap<>();
				storedComments.remove(comment);
				store.put(txn, commitKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) storedComments)));
			}
			
		});
	}

	@Override
	public List<String> getCommentedFiles(Depot depot) {
		List<String> files = filesCache.get(depot.getId());
		if (files == null) {
			Environment env = getEnv(depot);
			final Store store = getStore(env, DEFAULT_STORE);

			files = env.computeInReadonlyTransaction(new TransactionalComputable<List<String>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<String> compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, FILES_KEY));
					if (bytes != null) {
						List<String> files = new ArrayList<>((Set<String>)SerializationUtils.deserialize(bytes));
						files.sort((file1, file2)->Paths.get(file1).compareTo(Paths.get(file2)));
						return files;
					} else {
						return new ArrayList<>();
					}
				}
			});
			filesCache.put(depot.getId(), files);
		}
		return files;
	}

	@Override
	public void onSaveCommentAndReply(CodeComment comment, CodeCommentReply reply) {
		if (comment.isNew()) {
			dao.afterCommit(new Runnable() {
				
				@Override
				public void run() {
					collect(comment.getDepot());
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
