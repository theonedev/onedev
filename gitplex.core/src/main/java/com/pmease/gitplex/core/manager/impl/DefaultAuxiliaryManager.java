package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.NameAndEmail;
import com.pmease.commons.git.command.CommitConsumer;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.listeners.LifecycleListener;
import com.pmease.gitplex.core.listeners.RepositoryListener;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.manager.SequentialWorkManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.WorkManager;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultAuxiliaryManager implements AuxiliaryManager, RepositoryListener, LifecycleListener {

	private static final String AUXILIARY_DIR = "auxiliary";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String COMMITS_STORE = "commmits";
	
	private static final String FILES_STORE = "files";
	
	private static final ByteIterable LAST_COMMIT_KEY = new StringByteIterable("lastCommit");
	
	private static final ByteIterable CONTRIBUTORS_KEY = new StringByteIterable("contributors");
	
	private final StorageManager storageManager;
	
	private final WorkManager workManager;
	
	private final SequentialWorkManager sequentialWorkManager;
	
	private final Map<Long, Environment> envs = new HashMap<>();
	
	@Inject
	public DefaultAuxiliaryManager(StorageManager storageManager, WorkManager workManager, 
			SequentialWorkManager sequentialWorkManager) {
		this.storageManager = storageManager;
		this.workManager = workManager;
		this.sequentialWorkManager = sequentialWorkManager;
	}
	
	private String getSequentialExecutorKey(Repository repository) {
		return "repository-" + repository.getId() + "-checkAuxiliary";
	}
	
	@Override
	public void check(final Repository repository, final String refName) {
		sequentialWorkManager.execute(getSequentialExecutorKey(repository), new Runnable() {

			@Override
			public void run() {
				try {
					workManager.submit(new Runnable() {

						@Override
						public void run() {
							Environment env = getEnv(repository);
							final Store defaultStore = getStore(env, DEFAULT_STORE);
							final Store commitsStore = getStore(env, COMMITS_STORE);
							final Store filesStore = getStore(env, FILES_STORE);

							final AtomicReference<String> lastCommit = new AtomicReference<>();
							env.executeInTransaction(new TransactionalExecutable() {
								
								@Override
								public void execute(final Transaction txn) {
									byte[] value = getBytes(defaultStore.get(txn, LAST_COMMIT_KEY));
									lastCommit.set(value!=null?new String(value):null);									
								}
							});
							
							env.executeInTransaction(new TransactionalExecutable() {
								
								@Override
								public void execute(final Transaction txn) {
									byte[] bytes = getBytes(defaultStore.get(txn, LAST_COMMIT_KEY));
									final AtomicReference<String> lastCommit;
									if (bytes != null)
										lastCommit = new AtomicReference<>(new String(bytes));
									else
										lastCommit = new AtomicReference<>(null);
									Git git = repository.git();

									LogCommand log = new LogCommand(git.repoDir());
									List<String> revisions = new ArrayList<>();
									if (lastCommit.get() != null) {
										revisions.add(lastCommit.get() + ".." + refName);
										lastCommit.set(null);
									} else { 
										revisions.add(refName);
									}
									
									final AtomicReference<Set<NameAndEmail>> contributors = new AtomicReference<>(null);
									final AtomicBoolean contributorsChanged = new AtomicBoolean(false);
									
									log.revisions(revisions).listChangedFiles(true).run(new CommitConsumer() {

										@SuppressWarnings("unchecked")
										@Override
										public void consume(Commit commit) {
											ByteIterable key = new StringByteIterable(commit.getHash());
											ByteIterable value = new ArrayByteIterable(new byte[0]);
											if (!commitsStore.exists(txn, key, value)) {
												commitsStore.put(txn, key, value);
												if (contributors.get() == null) {
													byte[] bytes = getBytes(defaultStore.get(txn, CONTRIBUTORS_KEY));
													if (bytes != null)
														contributors.set((Set<NameAndEmail>) SerializationUtils.deserialize(bytes));
													else
														contributors.set(new HashSet<NameAndEmail>());
												}
												NameAndEmail contributor = new NameAndEmail(commit.getAuthor());
												if (!contributors.get().contains(contributor)) {
													contributors.get().add(contributor);
													contributorsChanged.set(true);
												}
												contributor = new NameAndEmail(commit.getCommitter());
												if (!contributors.get().contains(contributor)) {
													contributors.get().add(contributor);
													contributorsChanged.set(true);
												}
												
												for (String file: commit.getChangedFiles()) {
													ByteIterable fileKey = new StringByteIterable(file);
													byte[] bytes = getBytes(filesStore.get(txn, fileKey));
													Map<NameAndEmail, Long> contributors;
													if (bytes != null)
														contributors = (Map<NameAndEmail, Long>) SerializationUtils.deserialize(bytes);
													else
														contributors = new HashMap<>();
													contributor = new NameAndEmail(commit.getAuthor());
													long authorTime = commit.getAuthor().getWhen().getTime();
													Long when = contributors.get(contributor);
													if (when == null || when.longValue() < authorTime)
														contributors.put(contributor, authorTime);
														
													contributor = new NameAndEmail(commit.getCommitter());
													long committerTime = commit.getCommitter().getWhen().getTime();
													when = contributors.get(contributor);
													if (when == null || when.longValue() < committerTime)
														contributors.put(contributor, committerTime);
													
													bytes = SerializationUtils.serialize((Serializable) contributors);
													filesStore.put(txn, fileKey, new ArrayByteIterable(bytes));
												}
												
												if (lastCommit.get() == null)
													lastCommit.set(commit.getHash());
											}
										}
										
									});
									
									if (contributorsChanged.get()) {
										bytes = SerializationUtils.serialize((Serializable) contributors.get());
										defaultStore.put(txn, CONTRIBUTORS_KEY, new ArrayByteIterable(bytes));
									}
									if (lastCommit.get() != null) {
										bytes = lastCommit.get().getBytes();
										defaultStore.put(txn, LAST_COMMIT_KEY, new ArrayByteIterable(bytes));
									}
									
									System.out.println("***************");
								}
							});
						}
						
					}).get();
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
	}
	
	private synchronized Environment getEnv(final Repository repository) {
		Environment env = envs.get(repository.getId());
		if (env == null) {
			EnvironmentConfig config = new EnvironmentConfig();
			config.setLogCacheShared(false);
			config.setMemoryUsage(1024*1024*10);
			env = Environments.newInstance(getAuxiliaryDir(repository), config);
			envs.put(repository.getId(), env);
		}
		return env;
	}
	
	private File getAuxiliaryDir(Repository repository) {
		File auxiliaryDir = new File(storageManager.getCacheDir(repository), AUXILIARY_DIR);
		if (!auxiliaryDir.exists()) 
			FileUtils.createDir(auxiliaryDir);
		return auxiliaryDir;
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
	public Set<NameAndEmail> getContributors(Repository repository) {
		Environment env = getEnv(repository);
		final Store defaultStore = getStore(env, DEFAULT_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Set<NameAndEmail>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Set<NameAndEmail> compute(Transaction txn) {
				byte[] value = getBytes(defaultStore.get(txn, CONTRIBUTORS_KEY));
				if (value != null)
					return (Set<NameAndEmail>) SerializationUtils.deserialize(value);
				else
					return new HashSet<>();
			}
		});
	}

	@Override
	public Map<String, Map<NameAndEmail, Long>> getContributors(Repository repository, final Set<String> files) {
		Environment env = getEnv(repository);
		final Store filesStore = getStore(env, FILES_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Map<String, Map<NameAndEmail, Long>>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Map<String, Map<NameAndEmail, Long>> compute(Transaction txn) {
				Map<String, Map<NameAndEmail, Long>> file2authors = new HashMap<>();
				for (String file: files) {
					ByteIterable fileKey = new StringByteIterable(file);
					Map<NameAndEmail, Long> authors;
					byte[] value = getBytes(filesStore.get(txn, fileKey));
					if (value != null)
						authors = (Map<NameAndEmail, Long>) SerializationUtils.deserialize(value);
					else
						authors = new HashMap<>();
					file2authors.put(file, authors);
				}
				return file2authors;
			}
		});
	}

	@Override
	public void beforeDelete(Repository repository) {
	}

	@Override
	public synchronized void afterDelete(Repository repository) {
		sequentialWorkManager.removeExecutor(getSequentialExecutorKey(repository));
		Environment env = envs.remove(repository.getId());
		if (env != null)
			env.close();
		FileUtils.deleteDir(getAuxiliaryDir(repository));
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
	public synchronized void systemStopping() {
		for (Environment env: envs.values())
			env.close();
	}

	@Override
	public void systemStopped() {
	}
	
}
