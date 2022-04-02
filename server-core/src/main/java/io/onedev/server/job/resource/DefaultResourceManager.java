package io.onedev.server.job.resource;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.websocket.api.Session;
import org.hibernate.query.Query;

import io.onedev.agent.AgentData;
import io.onedev.commons.loader.Listen;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Setting;
import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.agent.AgentQuery;

@Singleton
public class DefaultResourceManager implements ResourceManager {

	private final SettingManager settingManager;
	
	private final AgentManager agentManager;
	
	private ResourceHolder serverResourceHolder;
	
	private final Map<Long, Boolean> agentPaused = new HashMap<>();
	
	private final Map<Long, ResourceHolder> agentResourceHolders = new HashMap<>();
	
	private final Map<String, QueryCache> queryCaches = new HashMap<>();
	
	private final SessionManager sessionManager;
	
	private final TransactionManager transactionManager;
	
	private final Dao dao;
	
	@Inject
	public DefaultResourceManager(Dao dao, SettingManager settingManager, AgentManager agentManager, 
			SessionManager sessionManager, TransactionManager transactionManager) {
		this.dao = dao;
		this.settingManager = settingManager;
		this.agentManager = agentManager;
		this.sessionManager = sessionManager;
		this.transactionManager = transactionManager;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	@Listen(10)
	public synchronized void on(SystemStarted event) {
		Map<String, Integer> resources = new HashMap<>();
		resources.put(ResourceHolder.CPU, 
				settingManager.getPerformanceSetting().getServerJobExecutorCpuQuota());
		resources.put(ResourceHolder.MEMORY, 
				settingManager.getPerformanceSetting().getServerJobExecutorMemoryQuota());
		serverResourceHolder = new ResourceHolder(resources);
		
		Query<?> query = dao.getSession().createQuery(String.format("select id, %s from Agent", Agent.PROP_PAUSED));
		for (Object[] fields: (List<Object[]>)query.list()) 
			agentPaused.put((Long)fields[0], (Boolean)fields[1]);
	}

	@Transactional
	@Listen
	public void on(AgentConnected event) {
		Long agentId = event.getAgent().getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				sessionManager.runAsync(new Runnable() {

					@Override
					public void run() {
						synchronized (DefaultResourceManager.this) {
							Agent agent = agentManager.load(agentId);
							agentResourceHolders.put(agentId, new ResourceHolder(agent.getResources()));
							for (QueryCache cache: queryCaches.values()) {
								if (cache.query.matches(agent))
									cache.result.add(agentId);
							}
							DefaultResourceManager.this.notifyAll();
						}
					}
					
				});
			}
			
		});
	}
	
	@Transactional
	@Listen
	public void on(AgentDisconnected event) {
		Long agentId = event.getAgent().getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				synchronized (DefaultResourceManager.this) {
					agentResourceHolders.remove(agentId);
					for (QueryCache cache: queryCaches.values())
						cache.result.remove(agentId);
				}
			}
		});
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (serverResourceHolder != null && event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == Setting.Key.PERFORMANCE) {
				PerformanceSetting performanceSetting = (PerformanceSetting) setting.getValue();
				transactionManager.runAfterCommit(new Runnable() {

					@Override
					public void run() {
						synchronized (DefaultResourceManager.this) {
							serverResourceHolder.updateTotalResource(ResourceHolder.CPU, 
									performanceSetting.getServerJobExecutorCpuQuota());
							serverResourceHolder.updateTotalResource(ResourceHolder.MEMORY, 
									performanceSetting.getServerJobExecutorMemoryQuota());
							DefaultResourceManager.this.notifyAll();
						}
					}
				});
			}
		} else if (event.getEntity() instanceof Agent) {
			Long agentId = event.getEntity().getId();
			boolean paused = ((Agent)event.getEntity()).isPaused();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					synchronized (DefaultResourceManager.this) {
						agentPaused.put(agentId, paused);
						DefaultResourceManager.this.notifyAll();
					}
				}
			});
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Agent) { 
			Long agentId = event.getEntity().getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					synchronized (DefaultResourceManager.this) {
						agentPaused.remove(agentId);
					}
				}
			});
		}
	}
	
	@Override
	public void run(Runnable runnable, Map<String, Integer> serverResourceRequirements, TaskLogger logger) {
		logger.log("Waiting for resources...");
		synchronized(this) {
			while (serverResourceHolder.getSpareResources(serverResourceRequirements) == 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			serverResourceHolder.acquireResources(serverResourceRequirements);
		}
		
		try {
			runnable.run();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		} finally {
			synchronized (this) {
				serverResourceHolder.releaseResources(serverResourceRequirements);
				notifyAll();
			}
		}	
	}
	
	@Sessional
	protected AgentData getAgentData(Long agentId) {
		return agentManager.load(agentId).getAgentData();
	}

	@Override
	public void run(AgentAwareRunnable runnable, Map<String, Integer> serverResourceRequirements, 
			AgentQuery agentQuery, Map<String, Integer> agentResourceRequirements, TaskLogger logger) {
		Set<Long> agentIds = agentManager.query(agentQuery, 0, Integer.MAX_VALUE)
				.stream().map(it->it.getId()).collect(Collectors.toSet());
		Long agentId = 0L;
		ResourceHolder agentResourceHolder = null;
		synchronized(this) {
			logger.log("Waiting for resources...");
			String uuid = UUID.randomUUID().toString();
			queryCaches.put(uuid, new QueryCache(agentQuery, agentIds));
			try {
				while (true) {
					if (serverResourceHolder != null && serverResourceHolder.getSpareResources(serverResourceRequirements) != 0) {
						int maxSpareResources = 0;
						for (Long each: agentIds) {
							ResourceHolder eachAgentResourceHolder = agentResourceHolders.get(each);
							Boolean paused = agentPaused.get(each);
							if (eachAgentResourceHolder != null && paused != null && !paused) {
								int spareResources = eachAgentResourceHolder.getSpareResources(agentResourceRequirements);
								if (spareResources > maxSpareResources) {
									agentId = each;
									maxSpareResources = spareResources;
									agentResourceHolder = eachAgentResourceHolder;
								}
							}
						}
						if (agentId != 0)
							break;
					} 
					try {
						wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				agentResourceHolder.acquireResources(agentResourceRequirements);
				serverResourceHolder.acquireResources(serverResourceRequirements);
			} finally {
				queryCaches.remove(uuid);
			}
		}

		try {
			updateLastUsedDate(agentId);
			Session agentSession = agentManager.getAgentSession(agentId);
			if (agentSession == null)
				throw new ExplicitException("Agent goes offline");
			runnable.runOn(agentId, agentSession, getAgentData(agentId));
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		} finally {
			synchronized(this) {
				serverResourceHolder.releaseResources(serverResourceRequirements);
				agentResourceHolder.releaseResources(agentResourceRequirements);
				notifyAll();
			}
		}	
	}
	
	@Transactional
	protected void updateLastUsedDate(Long agentId) {
		agentManager.load(agentId).setLastUsedDate(new Date());
	}

	private static class QueryCache {
		
		AgentQuery query;
		
		Collection<Long> result;
		
		QueryCache(AgentQuery query, Collection<Long> result) {
			this.query = query;
			this.result = result;
		}
		
	}

	@Override
	public void waitingForAgentResourceToBeReleased(Long agentId) {
		synchronized (this) {
			ResourceHolder agentResourceHolder = agentResourceHolders.remove(agentId);
			if (agentResourceHolder != null) {
				while (agentResourceHolder.hasUsedResources()) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
}
