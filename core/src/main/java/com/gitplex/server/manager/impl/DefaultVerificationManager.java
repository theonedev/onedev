package com.gitplex.server.manager.impl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.SerializationUtils;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.VerificationManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.Verification;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultVerificationManager extends AbstractEnvironmentManager implements VerificationManager {

	private static final int INFO_VERSION = 1;
	
	private static final String INFO_DIR = "verification";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String VERIFICATIONS_STORE = "verifications";
	
	private static final String VERIFICATION_CONTEXTS_KEY = "verificationContexts";
	
	private static final long MAX_VERIFICATION_CONTEXTS = 1000;
	
	private final StorageManager storageManager;
	
	private final Dao dao;
	
	@Inject
	public DefaultVerificationManager(Dao dao, StorageManager storageManager) {
		this.dao = dao;
		this.storageManager = storageManager;
	}
	
	@Override
	public void saveVerification(Project project, String commit, String context, Verification verification) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store verificationsStore = getStore(env, VERIFICATIONS_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void execute(Transaction txn) {
				ByteIterable key = new StringByteIterable(VERIFICATION_CONTEXTS_KEY);
				byte[] bytes = getBytes(defaultStore.get(txn, key));
				Map<String, Date> verificationContexts;
				if (bytes != null)
					verificationContexts = (Map<String, Date>) SerializationUtils.deserialize(bytes);
				else
					verificationContexts = new HashMap<>();
				
				verificationContexts.put(context, verification.getDate());

				if (verificationContexts.size() > MAX_VERIFICATION_CONTEXTS) {
					List<String> contextList = new ArrayList<>(verificationContexts.keySet());
					contextList.sort((o1, o2)->verificationContexts.get(o1).compareTo(verificationContexts.get(o2)));
					for (int i=0; i<contextList.size()-MAX_VERIFICATION_CONTEXTS; i++)
						verificationContexts.remove(contextList.get(i));
				}
				
				defaultStore.put(txn, key, 
						new ArrayByteIterable(SerializationUtils.serialize((Serializable) verificationContexts)));
				
				key = new StringByteIterable(commit);
				bytes = getBytes(verificationsStore.get(txn, key));
				Map<String, Verification> verifications;
				if (bytes != null)
					verifications = (Map<String, Verification>) SerializationUtils.deserialize(bytes);
				else
					verifications = new HashMap<>();

				verifications.put(context, verification);
				
				verificationsStore.put(txn, key, new ArrayByteIterable(SerializationUtils.serialize((Serializable) verifications)));
			}
			
		});
	}

	@Override
	public Map<String, Verification> getVerifications(Project project, String commit) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, VERIFICATIONS_STORE);
		return env.computeInTransaction(new TransactionalComputable<Map<String, Verification>>() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Map<String, Verification> compute(Transaction txn) {
				byte[] bytes = getBytes(store.get(txn, new StringByteIterable(commit)));
				if (bytes != null)
					return (Map<String, Verification>) SerializationUtils.deserialize(bytes);
				else
					return null;
			}
			
		});
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			dao.doAfterCommit(new Runnable() {

				@Override
				public void run() {
					removeEnv(event.getEntity().getId().toString());
				}
				
			});
		}
	}
	
	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(storageManager.getProjectInfoDir(Long.valueOf(envKey)), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

	@Override
	public Collection<String> getVerificationContexts(Project project) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, DEFAULT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Collection<String>>() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Collection<String> compute(Transaction txn) {
				byte[] bytes = getBytes(store.get(txn, new StringByteIterable(VERIFICATION_CONTEXTS_KEY)));
				if (bytes != null)
					return ((Map<String, Date>) SerializationUtils.deserialize(bytes)).keySet();
				else
					return null;
			}
			
		});
	}

}
