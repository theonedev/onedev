package com.pmease.gitop.core.manager.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.gitop.core.event.BranchRefUpdateEvent;
import com.pmease.gitop.core.manager.AutoPullManager;
import com.pmease.gitop.model.AutoPull;
import com.pmease.gitop.model.Branch;

@Singleton
public class DefaultAutoPullManager extends AbstractGenericDao<AutoPull> 
		implements AutoPullManager {

	private final Logger logger = LoggerFactory.getLogger(DefaultAutoPullManager.class);
	
	private final EventBus eventBus;
	
	private final UnitOfWork unitOfWork;
	
	private final Executor executor;
	
	@Inject
	public DefaultAutoPullManager(GeneralDao generalDao, EventBus eventBus, 
			UnitOfWork unitOfWork, Executor executor) {
		super(generalDao);
		this.eventBus = eventBus;
		this.unitOfWork = unitOfWork;
		this.executor = executor;
	}

	@Sessional
	@Subscribe
	public void pullUpon(BranchRefUpdateEvent event) {
		for (final AutoPull autoPull: event.getBranch().getAutoPullTargets()) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						unitOfWork.call(new Callable<Void>() {
	
							@Override
							public Void call() throws Exception {
								// Reload to avoid Hibernate LazyInitializationException
								AutoPull reloaded = load(autoPull.getId());
								Branch source = reloaded.getSource();
								Branch target = reloaded.getTarget();
								Git git = target.getProject().code();
								String commit = source.getProject().code().parseRevision(source.getHeadRef(), true);
								git.updateRef(target.getHeadRef(), commit, null, "Auto pull");
								
								eventBus.post(new BranchRefUpdateEvent(target));
								
								return null;
							}
							
						});
					} catch(Exception e) {
						logger.error("Error performing auto-pull.", e);
						throw ExceptionUtils.unchecked(e);
					}
				}
				
			});
		}
	}
}
