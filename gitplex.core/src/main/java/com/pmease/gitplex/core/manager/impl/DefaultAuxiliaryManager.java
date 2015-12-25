package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.CommitConsumer;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.ObjectReference;
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
public class DefaultAuxiliaryManager implements AuxiliaryManager, RepositoryListener {

	private static final String AUXILIARY_DIR = "auxiliary";
	
	private final StorageManager storageManager;
	
	private final WorkManager workManager;
	
	private final SequentialWorkManager sequentialWorkManager;
	
	private final Map<Long, ObjectReference<Environment>> envRefs = new HashMap<>();
	
	@Inject
	public DefaultAuxiliaryManager(StorageManager storageManager, WorkManager workManager, 
			SequentialWorkManager sequentialWorkManager) {
		this.storageManager = storageManager;
		this.workManager = workManager;
		this.sequentialWorkManager = sequentialWorkManager;
	}
	
	@Override
	public void check(final Repository repository, final String refName) {
		String sequentialKey = "repository-" + repository.getId() + "-checkAuxiliary";
		sequentialWorkManager.execute(sequentialKey, new Runnable() {

			@Override
			public void run() {
				try {
					workManager.submit(new Runnable() {

						@Override
						public void run() {
							executeInEnv(repository, new EnvExecutable() {

								@Override
								public void execute(Environment env) {
									final Store lastCommitStore = getStore(env, "lastCommit");
									final Store commitsStore = getStore(env, "commits");
									env.executeInTransaction(new TransactionalExecutable() {
										
										@Override
										public void execute(final Transaction txn) {
											ByteIterable lastCommitKey = new StringByteIterable("lastCommit");
											byte[] value = getBytes(lastCommitStore.get(txn, lastCommitKey));
											String lastCommit = value!=null?new String(value):null;
											Git git = repository.git();
											final AtomicLong count = new AtomicLong(0);

											LogCommand log = new LogCommand(git.repoDir());
											List<String> revisions = new ArrayList<>();
											if (lastCommit != null)
												revisions.add(lastCommit + ".." + refName);
											else 
												revisions.add(refName);
											
											final AtomicReference<Commit> lastCommitRef = new AtomicReference<>(null);
											log.revisions(revisions).listChangedFiles(true).run(new CommitConsumer() {

												@Override
												public void consume(Commit commit) {
													ByteIterable key = new StringByteIterable(commit.getHash());
													if (commitsStore.get(txn, key) == null) {
														byte[] commitBytes = SerializationUtils.serialize(commit);
														commitsStore.put(txn, key, new ArrayByteIterable(commitBytes));
													}
													if (count.incrementAndGet() % 100 == 0)
														txn.flush();
													if (lastCommitRef.get() == null)
														lastCommitRef.set(commit);
												}
												
											});
											
											if (lastCommitRef.get() != null) {
												value = lastCommitRef.get().getHash().getBytes();
												lastCommitStore.put(txn, lastCommitKey, new ArrayByteIterable(value));
											}
										}
									});
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
	
	private void executeInEnv(Repository repository, EnvExecutable executable) {
		ObjectReference<Environment> ref = getEnvRef(repository);
		Environment env = ref.open();
		try {
			executable.execute(env);
		} finally {
			ref.close();
		}
	}
	
	private <T> T computeInEnv(Repository repository, EnvComputable<T> computable) {
		ObjectReference<Environment> ref = getEnvRef(repository);
		Environment env = ref.open();
		try {
			return computable.compute(env);
		} finally {
			ref.close();
		}
	}
	
	private synchronized ObjectReference<Environment> getEnvRef(final Repository repository) {
		ObjectReference<Environment> ref = envRefs.get(repository.getId());
		if (ref == null) {
			ref = new ObjectReference<Environment>() {

				@Override
				protected Environment openObject() {
					EnvironmentConfig config = new EnvironmentConfig();
					config.setLogCacheShared(false);
					return Environments.newInstance(getAuxiliaryDir(repository), config);
				}

				@Override
				protected void closeObject(Environment object) {
					object.close();
				}
				
			};
			envRefs.put(repository.getId(), ref);
		}
		return ref;
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
	public Map<String, Commit> getCommits(final Repository repository, final Set<String> commitHashes) {
		return computeInEnv(repository, new EnvComputable<Map<String, Commit>>() {

			@Override
			public Map<String, Commit> compute(Environment env) {
				final Store commitsStore = getStore(env, "commits");

				return env.computeInReadonlyTransaction(new TransactionalComputable<Map<String, Commit>>() {

					@Override
					public Map<String, Commit> compute(Transaction txn) {
						Map<String, Commit> commits = new HashMap<>();
						for (String commitHash: commitHashes) {
							byte[] value = getBytes(commitsStore.get(txn, new StringByteIterable(commitHash)));
							if (value != null) 
								commits.put(commitHash, (Commit) SerializationUtils.deserialize(value));
						}
						return commits;
					}
				});
			}
			
		});
	}

	@Override
	public Set<PersonIdent> getAuthors(Repository repository) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PersonIdent> getCommitters(Repository repository) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PersonIdent> getAuthorsModified(Repository repository, String file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PersonIdent> getCommittersModified(Repository repository, String file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeDelete(Repository repository) {
	}

	@Override
	public void afterDelete(Repository repository) {
		FileUtils.deleteDir(getAuxiliaryDir(repository));
	}
	
	private byte[] getBytes(@Nullable ByteIterable byteIterable) {
		if (byteIterable != null)
			return Arrays.copyOf(byteIterable.getBytesUnsafe(), byteIterable.getLength());
		else
			return null;
	}
	
	static interface EnvExecutable {
		void execute(Environment env);
	}
	
	static interface EnvComputable<T> {
		T compute(Environment env);
	}

	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}
}
