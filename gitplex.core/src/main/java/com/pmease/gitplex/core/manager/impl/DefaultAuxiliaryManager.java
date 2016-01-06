package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.NameAndEmail;
import com.pmease.commons.git.command.CommitConsumer;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.listeners.LifecycleListener;
import com.pmease.gitplex.core.listeners.RefListener;
import com.pmease.gitplex.core.listeners.RepositoryListener;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.manager.SequentialWorkManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.WorkManager;
import com.pmease.gitplex.core.model.Repository;

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
public class DefaultAuxiliaryManager implements AuxiliaryManager, RepositoryListener, RefListener, LifecycleListener {

	private static final String AUXILIARY_DIR = "auxiliary";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String COMMITS_STORE = "commmits";
	
	private static final String CONTRIBUTIONS_STORE = "contributions";
	
	private static final ByteIterable LAST_COMMIT_KEY = new StringByteIterable("lastCommit");
	
	private static final ByteIterable CONTRIBUTORS_KEY = new StringByteIterable("contributors");
	
	private static final ByteIterable FILES_KEY = new StringByteIterable("files");
	
	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final WorkManager workManager;
	
	private final SequentialWorkManager sequentialWorkManager;
	
	private final Map<Long, Environment> envs = new HashMap<>();
	
	private final Map<Long, List<String>> files = new ConcurrentHashMap<>();
	
	private final Map<Long, List<NameAndEmail>> contributors = new ConcurrentHashMap<>();
	
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
		sequentialWorkManager.execute(getSequentialExecutorKey(repository), new PrioritizedRunnable(PRIORITY) {

			@Override
			public void run() {
				try {
					workManager.submit(new PrioritizedRunnable(PRIORITY) {

						@Override
						public void run() {
							Environment env = getEnv(repository);
							final Store defaultStore = getStore(env, DEFAULT_STORE);
							final Store commitsStore = getStore(env, COMMITS_STORE);
							final Store contributionsStore = getStore(env, CONTRIBUTIONS_STORE);

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
									
									final AtomicReference<Set<String>> files = new AtomicReference<>(null);
									final AtomicBoolean filesChanged = new AtomicBoolean(false);
									
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
												if (StringUtils.isNotBlank(commit.getAuthor().getName()) 
														|| StringUtils.isNotBlank(commit.getAuthor().getEmailAddress())) {
													NameAndEmail contributor = new NameAndEmail(commit.getAuthor());
													if (!contributors.get().contains(contributor)) {
														contributors.get().add(contributor);
														contributorsChanged.set(true);
													}
												}
												if (StringUtils.isNotBlank(commit.getCommitter().getName()) 
														|| StringUtils.isNotBlank(commit.getCommitter().getEmailAddress())) {
													NameAndEmail contributor = new NameAndEmail(commit.getCommitter());
													if (!contributors.get().contains(contributor)) {
														contributors.get().add(contributor);
														contributorsChanged.set(true);
													}
												}
												
												if (files.get() == null) {
													byte[] bytes = getBytes(defaultStore.get(txn, FILES_KEY));
													if (bytes != null)
														files.set((Set<String>) SerializationUtils.deserialize(bytes));
													else
														files.set(new HashSet<String>());
												}
												
												for (String file: commit.getChangedFiles()) {
													ByteIterable fileKey = new StringByteIterable(file);
													byte[] bytes = getBytes(contributionsStore.get(txn, fileKey));
													Map<NameAndEmail, Long> fileContributions;
													if (bytes != null)
														fileContributions = (Map<NameAndEmail, Long>) SerializationUtils.deserialize(bytes);
													else
														fileContributions = new HashMap<>();
													if (StringUtils.isNotBlank(commit.getAuthor().getName()) 
															|| StringUtils.isNotBlank(commit.getAuthor().getEmailAddress())) {
														NameAndEmail contributor = new NameAndEmail(commit.getAuthor());
														long contributionTime = commit.getAuthor().getWhen().getTime();
														Long lastContributionTime = fileContributions.get(contributor);
														if (lastContributionTime == null || lastContributionTime.longValue() < contributionTime)
															fileContributions.put(contributor, contributionTime);
													}													

													if (StringUtils.isNotBlank(commit.getCommitter().getName()) 
															|| StringUtils.isNotBlank(commit.getCommitter().getEmailAddress())) {
														NameAndEmail contributor = new NameAndEmail(commit.getCommitter());
														long contributionTime = commit.getCommitter().getWhen().getTime();
														Long lastContributionTime = fileContributions.get(contributor);
														if (lastContributionTime == null || lastContributionTime.longValue() < contributionTime)
															fileContributions.put(contributor, contributionTime);
													}
													
													bytes = SerializationUtils.serialize((Serializable) fileContributions);
													contributionsStore.put(txn, fileKey, new ArrayByteIterable(bytes));
													
													if (!files.get().contains(file)) {
														files.get().add(file);
														filesChanged.set(true);
													}
												}
												
												if (lastCommit.get() == null)
													lastCommit.set(commit.getHash());
											}
										}
										
									});
									
									if (contributorsChanged.get()) {
										bytes = SerializationUtils.serialize((Serializable) contributors.get());
										defaultStore.put(txn, CONTRIBUTORS_KEY, new ArrayByteIterable(bytes));
										DefaultAuxiliaryManager.this.contributors.remove(repository.getId());
									}
									if (filesChanged.get()) {
										bytes = SerializationUtils.serialize((Serializable) files.get());
										defaultStore.put(txn, FILES_KEY, new ArrayByteIterable(bytes));
										DefaultAuxiliaryManager.this.files.remove(repository.getId());
									}
									if (lastCommit.get() != null) {
										bytes = lastCommit.get().getBytes();
										defaultStore.put(txn, LAST_COMMIT_KEY, new ArrayByteIterable(bytes));
									}
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
			config.setMemoryUsage(1024*1024*64);
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
	public List<NameAndEmail> getContributors(Repository repository) {
		List<NameAndEmail> repoContributors = contributors.get(repository.getId());
		if (repoContributors == null) {
			Environment env = getEnv(repository);
			final Store store = getStore(env, DEFAULT_STORE);

			repoContributors = env.computeInReadonlyTransaction(new TransactionalComputable<List<NameAndEmail>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<NameAndEmail> compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, CONTRIBUTORS_KEY));
					if (bytes != null) { 
						List<NameAndEmail> repoContributors = 
								new ArrayList<>((Set<NameAndEmail>) SerializationUtils.deserialize(bytes));
						Collections.sort(repoContributors);
						return repoContributors;
					} else { 
						return new ArrayList<>();
					}
				}
			});
			contributors.put(repository.getId(), repoContributors);
		}
		return repoContributors;	
	}

	@Override
	public List<String> getFiles(Repository repository) {
		List<String> repoFiles = files.get(repository.getId());
		if (repoFiles == null) {
			Environment env = getEnv(repository);
			final Store store = getStore(env, DEFAULT_STORE);

			repoFiles = env.computeInReadonlyTransaction(new TransactionalComputable<List<String>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<String> compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, FILES_KEY));
					if (bytes != null) {
						List<Path> paths = new ArrayList<>();
						for (String file: (Set<String>)SerializationUtils.deserialize(bytes))
							paths.add(Paths.get(file));
						Collections.sort(paths, new Comparator<Path>() {

							@Override
							public int compare(Path path1, Path path2) {
								return path1.compareTo(path2);
							}
							
						});
						List<String> files = new ArrayList<>();
						for (Path path: paths)
							files.add(path.toString().replace('\\', '/'));
						return files;
					} else {
						return new ArrayList<>();
					}
				}
			});
			files.put(repository.getId(), repoFiles);
		}
		return repoFiles;
	}
	
	@Override
	public Map<String, Map<NameAndEmail, Long>> getContributions(Repository repository, final Set<String> files) {
		Environment env = getEnv(repository);
		final Store store = getStore(env, CONTRIBUTIONS_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Map<String, Map<NameAndEmail, Long>>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Map<String, Map<NameAndEmail, Long>> compute(Transaction txn) {
				Map<String, Map<NameAndEmail, Long>> fileContributors = new HashMap<>();
				for (String file: files) {
					ByteIterable fileKey = new StringByteIterable(file);
					Map<NameAndEmail, Long> contributions;
					byte[] value = getBytes(store.get(txn, fileKey));
					if (value != null)
						contributions = (Map<NameAndEmail, Long>) SerializationUtils.deserialize(value);
					else
						contributions = new HashMap<>();
					fileContributors.put(file, contributions);
				}
				return fileContributors;
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
		files.remove(repository.getId());
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

	@Override
	public void onRefUpdate(Repository repository, String refName, String newCommitHash) {
		if (refName.startsWith(Git.REFS_HEADS))
			check(repository, refName);
	}

}
