package com.turbodev.server.manager.impl;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.launcher.loader.Listen;
import com.turbodev.server.manager.StorageManager;
import com.turbodev.server.manager.UserInfoManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.EntityRemoved;
import com.turbodev.server.util.facade.ProjectFacade;
import com.turbodev.server.util.facade.UserFacade;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
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
public class DefaultUserInfoManager extends AbstractEnvironmentManager implements UserInfoManager {

	private static final int INFO_VERSION = 2;
	
	private static final String VISIT_STORE = "visit";
	
	private final StorageManager storageManager;
	
	@Inject
	public DefaultUserInfoManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}
	
	@Override
	public void visit(User user, Project project) {
		Environment env = getEnv(user.getId().toString());
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
		Environment env = getEnv(user.getId().toString());
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

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof User)
			removeEnv(event.getEntity().getId().toString());
	}

	@Override
	protected File getEnvDir(String envKey) {
		return storageManager.getUserInfoDir(Long.valueOf(envKey));
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

}
