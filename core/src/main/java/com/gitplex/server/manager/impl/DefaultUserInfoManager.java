package com.gitplex.server.manager.impl;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.UserInfoManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserFacade;

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

	private static final int INFO_VERSION = 1;
	
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
				writeLong(store, txn, new StringByteIterable(project.getUUID()), System.currentTimeMillis()+1000L);
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
				long millis = readLong(store, txn, new StringByteIterable(project.getUUID()), -1);
				if (millis != -1)
					return new Date(millis);
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
