package com.gitplex.server.manager.impl;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.lifecycle.SystemStopping;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.UserInfoManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.util.VersionUtils;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserFacade;

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

/**
 * Store project visit information here as we only need to load a single database to sort projects based on user 
 * visit information
 * 
 * @author robin
 *
 */
@Singleton
public class DefaultUserInfoManager implements UserInfoManager {

	private static final int INFO_VERSION = 1;
	
	private static final String VISIT_STORE = "visit";
	
	private final Map<Long, Environment> envs = new HashMap<>();
	
	private final StorageManager storageManager;
	
	private final Dao dao;
	
	@Inject
	public DefaultUserInfoManager(Dao dao, StorageManager storageManager) {
		this.dao = dao;
		this.storageManager = storageManager;
	}
	
	private synchronized Environment getEnv(Long userId) {
		Environment env = envs.get(userId);
		if (env == null) {
			EnvironmentConfig config = new EnvironmentConfig();
			config.setEnvCloseForcedly(true);

			File infoDir = storageManager.getUserInfoDir(userId);
			VersionUtils.checkInfoVersion(infoDir, INFO_VERSION);
			env = Environments.newInstance(infoDir, config);
			envs.put(userId, env);
		}
		return env;
	}
	
	private Store getStore(Environment env, String storeName) {
		return env.computeInTransaction(new TransactionalComputable<Store>() {
		    @Override
		    public Store compute(Transaction txn) {
		        return env.openStore(storeName, StoreConfig.WITHOUT_DUPLICATES, txn);
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
	public void visit(User user, Project project) {
		Environment env = getEnv(user.getId());
		Store store = getStore(env, VISIT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				store.put(txn, new StringByteIterable(project.getUUID()), 
						new ArrayByteIterable(longToBytes(System.currentTimeMillis()+1000L)));
			}
			
		});
	}

	@Override
	public Date getVisitDate(UserFacade user, ProjectFacade project) {
		Environment env = getEnv(user.getId());
		Store store = getStore(env, VISIT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				byte[] bytes = getBytes(store.get(txn, new StringByteIterable(project.getUUID())));
				if (bytes != null)
					return new Date(bytesToLong(bytes));
				else
					return null;
			}
			
		});
	}

	private byte[] longToBytes(long value) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(value);
	    return buffer.array();
	}

	private long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip(); 
	    return buffer.getLong();
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			dao.doAfterCommit(new Runnable() {

				@Override
				public void run() {
					synchronized (envs) {
						Environment env = envs.remove(projectId);
						if (env != null)
							env.close();
					}
				}
				
			});
		}
	}

	@Listen
	public void on(SystemStopping event) {
		synchronized (envs) {
			for (Environment env: envs.values())
				env.close();
		}
	}

	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}

}
