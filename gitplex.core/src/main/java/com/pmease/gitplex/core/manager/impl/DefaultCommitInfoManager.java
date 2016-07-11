package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.NameAndEmail;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.concurrent.Prioritized;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.listener.RefListener;
import com.pmease.gitplex.core.manager.BatchWorkManager;
import com.pmease.gitplex.core.manager.CommitInfoManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.support.BatchWorker;

@Singleton
public class DefaultCommitInfoManager implements CommitInfoManager, DepotListener, 
		RefListener, LifecycleListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultCommitInfoManager.class);
	
	private static final String INFO_DIR = "commit";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String COMMITS_STORE = "commits";
	
	private static final ByteIterable LAST_COMMIT_KEY = new StringByteIterable("lastCommit");
	
	private static final ByteIterable AUTHORS_KEY = new StringByteIterable("authors");
	
	private static final ByteIterable COMMITTERS_KEY = new StringByteIterable("committers");
	
	private static final ByteIterable FILES_KEY = new StringByteIterable("files");
	
	private static final ByteIterable COMMIT_COUNT_KEY = new StringByteIterable("commitCount");
	
	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final DepotManager depotManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Map<Long, Environment> envs = new HashMap<>();
	
	private final Map<Long, List<String>> filesCache = new ConcurrentHashMap<>();
	
	private final Map<Long, Integer> commitCountCache = new ConcurrentHashMap<>();
	
	private final Map<Long, List<NameAndEmail>> authorsCache = new ConcurrentHashMap<>();
	
	private final Map<Long, List<NameAndEmail>> committersCache = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultCommitInfoManager(DepotManager depotManager, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, UnitOfWork unitOfWork) {
		this.depotManager = depotManager;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
	}
	
	private void doCollect(Depot depot, List<RevCommit> commits) {
		logger.debug("Collecting commits info (repository: {}, commits: {})...", depot, commits.size());
		
		Environment env = getEnv(depot);
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store commitsStore = getStore(env, COMMITS_STORE);

		commits.sort(Comparator.comparing(RevCommit::getCommitTime));
		
		env.executeInTransaction(new TransactionalExecutable() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void execute(Transaction txn) {
				byte[] bytes = getBytes(defaultStore.get(txn, LAST_COMMIT_KEY));
				ObjectId lastCommitId;
				if (bytes != null)
					lastCommitId = ObjectId.fromRaw(bytes);
				else
					lastCommitId = null;

				bytes = getBytes(defaultStore.get(txn, COMMIT_COUNT_KEY));
				int prevCommitCount;
				if (bytes != null)
					prevCommitCount = (int) SerializationUtils.deserialize(bytes);
				else
					prevCommitCount = 0;
				int newCommitCount = prevCommitCount;
				
				Set<NameAndEmail> prevAuthors;
				bytes = getBytes(defaultStore.get(txn, AUTHORS_KEY));
				if (bytes != null)
					prevAuthors = (Set<NameAndEmail>) SerializationUtils.deserialize(bytes);
				else
					prevAuthors = new HashSet<>();
				Set<NameAndEmail> authors = new HashSet<>(prevAuthors);			
				
				Set<NameAndEmail> prevCommitters;
				bytes = getBytes(defaultStore.get(txn, COMMITTERS_KEY));
				if (bytes != null)
					prevCommitters = (Set<NameAndEmail>) SerializationUtils.deserialize(bytes);
				else
					prevCommitters = new HashSet<>();
				Set<NameAndEmail> committers = new HashSet<>(prevCommitters);

				Set<String> prevFiles;
				bytes = getBytes(defaultStore.get(txn, FILES_KEY));
				if (bytes != null)
					prevFiles = (Set<String>) SerializationUtils.deserialize(bytes);
				else
					prevFiles = new HashSet<>();
				Set<String> files = new HashSet<>(prevFiles);
				
				RevCommit latestCommit = commits.get(commits.size()-1);
				try (	RevWalk revWalk = new RevWalk(depot.getRepository());
						TreeWalk treeWalk = new TreeWalk(depot.getRepository());) {
					revWalk.markStart(commits);
					if (lastCommitId != null) {
						RevCommit lastCommit = GitUtils.parseCommit(revWalk, lastCommitId);
						if (lastCommit != null) {
							revWalk.markUninteresting(lastCommit);
							treeWalk.addTree(lastCommit.getTree());
						}
					}
					treeWalk.addTree(latestCommit.getTree());
					treeWalk.setRecursive(true);
						
					RevCommit commit = revWalk.next();
					while (commit != null) {
						byte[] keyBytes = new byte[20];
						commit.copyRawTo(keyBytes, 0);
						ByteIterable key = new ArrayByteIterable(keyBytes);
						byte[] valueBytes = getBytes(commitsStore.get(txn, key));
						
						if (valueBytes == null || valueBytes.length%2 == 0) {
							/*
							 * Length of stored bytes of a commit is either 20*nChild
							 * (20 is length of ObjectId), or 1+20*nChild, as we need 
							 * an extra leading byte to differentiate commits being 
							 * processed and commits with child information attached 
							 * but not processed. 
							 */
							byte[] newValueBytes;
							if (valueBytes == null) {
								newValueBytes = new byte[1];
							} else {
								newValueBytes = new byte[valueBytes.length+1];
								System.arraycopy(valueBytes, 0, newValueBytes, 1, valueBytes.length);
							}
							commitsStore.put(txn, key, new ArrayByteIterable(newValueBytes));
							
							newCommitCount++;
							
							for (RevCommit parent: commit.getParents()) {
								keyBytes = new byte[20];
								parent.copyRawTo(keyBytes, 0);
								key = new ArrayByteIterable(keyBytes);
								valueBytes = getBytes(commitsStore.get(txn, key));
								if (valueBytes != null) {
									newValueBytes = new byte[valueBytes.length+20];
									System.arraycopy(valueBytes, 0, newValueBytes, 0, valueBytes.length);
								} else {
									newValueBytes = new byte[20];
								}
								commit.copyRawTo(newValueBytes, newValueBytes.length-20);
								commitsStore.put(txn, key, new ArrayByteIterable(newValueBytes));
							}
							
							if (StringUtils.isNotBlank(commit.getAuthorIdent().getName()) 
									|| StringUtils.isNotBlank(commit.getAuthorIdent().getEmailAddress())) {
								authors.add(new NameAndEmail(commit.getAuthorIdent()));
							}

							if (StringUtils.isNotBlank(commit.getCommitterIdent().getName()) 
									|| StringUtils.isNotBlank(commit.getCommitterIdent().getEmailAddress())) {
								committers.add(new NameAndEmail(commit.getCommitterIdent()));
							}
						}		
						commit = revWalk.next();
					}
					
					while (treeWalk.next()) {
						files.add(treeWalk.getPathString());
					}
					
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				if (newCommitCount != prevCommitCount) {
					bytes = SerializationUtils.serialize(newCommitCount);
					defaultStore.put(txn, COMMIT_COUNT_KEY, new ArrayByteIterable(bytes));
					commitCountCache.put(depot.getId(), newCommitCount);
				}
				
				if (!authors.equals(prevAuthors)) {
					bytes = SerializationUtils.serialize((Serializable) authors);
					defaultStore.put(txn, AUTHORS_KEY, new ArrayByteIterable(bytes));
					authorsCache.remove(depot.getId());
				} 
				
				if (!committers.equals(prevCommitters)) {
					bytes = SerializationUtils.serialize((Serializable) committers);
					defaultStore.put(txn, COMMITTERS_KEY, new ArrayByteIterable(bytes));
					committersCache.remove(depot.getId());
				}
				
				if (!files.equals(prevFiles)) {
					bytes = SerializationUtils.serialize((Serializable) files);
					defaultStore.put(txn, FILES_KEY, new ArrayByteIterable(bytes));
					filesCache.remove(depot.getId());
				}
				
				bytes = new byte[20];
				latestCommit.copyRawTo(bytes, 0);
				defaultStore.put(txn, LAST_COMMIT_KEY, new ArrayByteIterable(bytes));
			}
			
		});
		logger.debug("Commits info collected (repository: {}, commits: {})...", depot, commits.size());
	}
	
	private Environment getEnv(final Depot depot) {
		synchronized (envs) {
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
	public List<NameAndEmail> getAuthors(Depot depot) {
		List<NameAndEmail> authors = authorsCache.get(depot.getId());
		if (authors == null) {
			Environment env = getEnv(depot);
			Store store = getStore(env, DEFAULT_STORE);

			authors = env.computeInReadonlyTransaction(new TransactionalComputable<List<NameAndEmail>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<NameAndEmail> compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, AUTHORS_KEY));
					if (bytes != null) { 
						List<NameAndEmail> authors = 
								new ArrayList<>((Set<NameAndEmail>) SerializationUtils.deserialize(bytes));
						Collections.sort(authors);
						return authors;
					} else { 
						return new ArrayList<>();
					}
				}
			});
			authorsCache.put(depot.getId(), authors);
		}
		return authors;	
	}

	@Override
	public List<NameAndEmail> getCommitters(Depot depot) {
		List<NameAndEmail> committers = committersCache.get(depot.getId());
		if (committers == null) {
			Environment env = getEnv(depot);
			Store store = getStore(env, DEFAULT_STORE);

			committers = env.computeInReadonlyTransaction(new TransactionalComputable<List<NameAndEmail>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<NameAndEmail> compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, COMMITTERS_KEY));
					if (bytes != null) { 
						List<NameAndEmail> committers = 
								new ArrayList<>((Set<NameAndEmail>) SerializationUtils.deserialize(bytes));
						Collections.sort(committers);
						return committers;
					} else { 
						return new ArrayList<>();
					}
				}
			});
			committersCache.put(depot.getId(), committers);
		}
		return committers;	
	}
	
	@Override
	public List<String> getFiles(Depot depot) {
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
	public Set<ObjectId> getDescendants(Depot depot, final ObjectId ancestor) {
		Environment env = getEnv(depot);
		final Store store = getStore(env, COMMITS_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Set<ObjectId>>() {

			@Override
			public Set<ObjectId> compute(Transaction txn) {
				Set<ObjectId> descendants = new HashSet<>();
				
				// use stack instead of recursion to avoid StackOverflowException
				Stack<ObjectId> stack = new Stack<>();
				descendants.add(ancestor);
				stack.add(ancestor);
				while (!stack.isEmpty()) {
					ObjectId current = stack.pop();
					byte[] keyBytes = new byte[20];
					current.copyRawTo(keyBytes, 0);
					byte[] valueBytes = getBytes(store.get(txn, new ArrayByteIterable(keyBytes)));
					if (valueBytes != null) {
						if (valueBytes.length % 2 == 0) {
							for (int i=0; i<valueBytes.length/20; i++) {
								ObjectId child = ObjectId.fromRaw(valueBytes, i*20);
								if (!descendants.contains(child)) {
									descendants.add(child);
									stack.push(child);
								}
							}
						} else { 
							/*
							 * skip the leading byte, which tells whether or not the commit 
							 * has been processed
							 */
							for (int i=0; i<(valueBytes.length-1)/20; i++) {
								ObjectId child = ObjectId.fromRaw(valueBytes, i*20+1);
								if (!descendants.contains(child)) {
									descendants.add(child);
									stack.push(child);
								}
							}
						}
					}
				}
				
				return descendants;
			}
			
		});
	}

	@Override
	public Set<ObjectId> getChildren(Depot depot, final ObjectId parent) {
		Environment env = getEnv(depot);
		final Store store = getStore(env, COMMITS_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Set<ObjectId>>() {

			@Override
			public Set<ObjectId> compute(Transaction txn) {
				Set<ObjectId> children = new HashSet<>();
				
				byte[] keyBytes = new byte[20];
				parent.copyRawTo(keyBytes, 0);
				byte[] valueBytes = getBytes(store.get(txn, new ArrayByteIterable(keyBytes)));
				if (valueBytes != null) {
					if (valueBytes.length % 2 == 0) {
						for (int i=0; i<valueBytes.length/20; i++) {
							children.add(ObjectId.fromRaw(valueBytes, i*20));
						}
					} else { 
						/*
						 * skip the leading byte, which tells whether or not the commit 
						 * has been processed
						 */
						for (int i=0; i<(valueBytes.length-1)/20; i++) {
							children.add(ObjectId.fromRaw(valueBytes, i*20+1));
						}
					}
				}
				return children;
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
		commitCountCache.remove(depot.getId());
		authorsCache.remove(depot.getId());
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
	
	private BatchWorker getBatchWorker(Depot depot) {
		Long depotId = depot.getId();
		return new BatchWorker("repository-" + depotId + "-collectCommitInfo") {

			@Override
			public void doWork(Collection<Prioritized> works) {
				unitOfWork.call(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						Depot depot = depotManager.load(depotId);
						List<RevCommit> commits = new ArrayList<>();
						try (RevWalk revWalk = new RevWalk(depot.getRepository())) {
							for (Prioritized work: works) {
								CollectingWork collectingWork = (CollectingWork) work;
								commits.add(revWalk.parseCommit(collectingWork.getCommitId()));
							}
						}
						doCollect(depot, commits);
						return null;
					}
					
				});
			}
			
		};		
	}
	
	@Override
	public void collect(Depot depot) {
		List<RevCommit> commits = new ArrayList<>();
		try (RevWalk revWalk = new RevWalk(depot.getRepository())) {
			Collection<Ref> refs = new ArrayList<>();
			refs.addAll(depot.getRepository().getRefDatabase().getRefs(Constants.R_HEADS).values());
			refs.addAll(depot.getRepository().getRefDatabase().getRefs(Constants.R_TAGS).values());

			for (Ref ref: refs) {
				RevObject revObj = revWalk.peel(revWalk.parseAny(ref.getObjectId()));
				if (revObj instanceof RevCommit) {
					RevCommit commit = (RevCommit) revObj;
					Environment env = getEnv(depot);
					Store commitsStore = getStore(env, COMMITS_STORE);
					boolean collected = env.computeInReadonlyTransaction(new TransactionalComputable<Boolean>() {
						
						@Override
						public Boolean compute(Transaction txn) {
							byte[] keyBytes = new byte[20];
							commit.copyRawTo(keyBytes, 0);
							ByteIterable key = new ArrayByteIterable(keyBytes);
							byte[] valueBytes = getBytes(commitsStore.get(txn, key));
							return valueBytes != null && valueBytes.length%2 != 0;
						}
						
					});
					if (!collected) 
						commits.add(commit);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		if (!commits.isEmpty()) {
			doCollect(depot, commits);
		}
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
	public void onRefUpdate(Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (!newCommit.equals(ObjectId.zeroId())) {
			CollectingWork refUpdate = new CollectingWork(PRIORITY, newCommit);
			batchWorkManager.submit(getBatchWorker(depot), refUpdate);
		}
	}

	@Override
	public void onTransferDepot(Depot depot, Account oldAccount) {
	}

	@Override
	public int getCommitCount(Depot depot) {
		Integer commitCount = commitCountCache.get(depot.getId());
		if (commitCount == null) {
			Environment env = getEnv(depot);
			Store store = getStore(env, DEFAULT_STORE);

			commitCount = env.computeInReadonlyTransaction(new TransactionalComputable<Integer>() {

				@Override
				public Integer compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, COMMIT_COUNT_KEY));
					if (bytes != null) {
						return (Integer) SerializationUtils.deserialize(bytes);
					} else {
						return 0;
					}
				}
			});
			commitCountCache.put(depot.getId(), commitCount);
		}
		return commitCount;
	}

	@Override
	public void onSaveDepot(Depot depot) {
	}

	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}

	static class CollectingWork extends Prioritized {
		
		private final ObjectId commitId;
		
		public CollectingWork(int priority, ObjectId commitId) {
			super(priority);
			this.commitId = commitId;
		}

		public ObjectId getCommitId() {
			return commitId;
		}
		
	}

}
