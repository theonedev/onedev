package com.pmease.gitplex.core.manager.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.ObjectReference;
import com.pmease.gitplex.core.listeners.RepositoryListener;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;

import javassist.bytecode.ByteArray;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

public class DefaultAuxiliaryManager implements AuxiliaryManager, RepositoryListener {

	private static final String AUXILIARY_DIR = "auxiliary";
	
	private final StorageManager storageManager;
	
	private final ExecutorService executorService;
	
	private final Map<Long, ObjectReference<Environment>> envRefs = new HashMap<>();
	
	@Inject
	public DefaultAuxiliaryManager(StorageManager storageManager, ExecutorService executorService) {
		this.storageManager = storageManager;
		this.executorService = executorService;
	}
	
	@Override
	public void check(final Repository repository, final String refName) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				executeInEnv(repository, new EnvExecutable() {

					@Override
					public void execute(Environment env) {
						final Store lastCommitStore = getStore(env, "lastCommit");
						final Store commitsStore = getStore(env, "commits");
						env.executeInTransaction(new TransactionalExecutable() {
							
							@Override
							public void execute(Transaction txn) {
								byte[] value = getBytes(lastCommitStore.get(txn, new StringByteIterable("lastCommit")));
								String lastCommit = value!=null?new String(value):null;
								Git git = repository.git();
								for (Commit commit: git.log(lastCommit, refName, null, 0, 0, true)) {
									
								}
							}
						});
					}
					
				});
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
					return Environments.newInstance(getAuxiliaryDir(repository));
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
	public Map<String, Set<String>> getParents(Set<String> commitHashes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PersonIdent> getAuthors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PersonIdent> getCommitters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PersonIdent> getAuthorsModified(String file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PersonIdent> getCommittersModified(String file) {
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
