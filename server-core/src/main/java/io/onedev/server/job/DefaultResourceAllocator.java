package io.onedev.server.job;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.websocket.api.Session;
import org.hibernate.query.Query;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapEvent;
import com.hazelcast.replicatedmap.ReplicatedMap;

import io.onedev.agent.AgentData;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Agent;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.agent.AgentQuery;

@Singleton
public class DefaultResourceAllocator implements ResourceAllocator, Serializable {

	private final AgentManager agentManager;
	
	private final ClusterManager clusterManager;
	
	private final ExecutorService executorService;
	
	private final ServerConfig serverConfig;
	
	private final Map<String, QueryCache> queryCaches = new HashMap<>();
	
	private final SessionManager sessionManager;
	
	private final TransactionManager transactionManager;
	
	private final Dao dao;
	
	private volatile ReplicatedMap<UUID, Map<String, Integer>> serverResourceQuotas;
	
	private volatile IMap<UUID, Map<String, Integer>> serverResourceUsages;
	
	private volatile ReplicatedMap<Long, Map<String, Integer>> agentResourceQuotas;
	
	private volatile IMap<Long, Map<String, Integer>> agentResourceUsages;
	
	private volatile IMap<Long, Boolean> agentPaused;
	
	@Inject
	public DefaultResourceAllocator(Dao dao, AgentManager agentManager, 
			SessionManager sessionManager, TransactionManager transactionManager, 
			ClusterManager clusterManager, ServerConfig serverConfig, 
			ExecutorService executorService) {
		this.dao = dao;
		this.agentManager = agentManager;
		this.sessionManager = sessionManager;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
		this.serverConfig = serverConfig;
		this.executorService = executorService;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ResourceAllocator.class);
	}
	
	private <K, V> EntryListener<K, V> newResourceChangeListener() {
		return new EntryListener<K, V>() {

			private void notifyResourceChange() {
				synchronized (DefaultResourceAllocator.this) {
					DefaultResourceAllocator.this.notifyAll();
				}
			}
			
			@Override
			public void entryAdded(EntryEvent<K, V> event) {
				notifyResourceChange();
			}

			@Override
			public void entryUpdated(EntryEvent<K, V> event) {
				notifyResourceChange();
			}

			@Override
			public void entryRemoved(EntryEvent<K, V> event) {
				notifyResourceChange();
			}

			@Override
			public void entryEvicted(EntryEvent<K, V> event) {
				notifyResourceChange();
			}

			@Override
			public void entryExpired(EntryEvent<K, V> event) {
				notifyResourceChange();
			}

			@Override
			public void mapCleared(MapEvent event) {
				notifyResourceChange();
			}

			@Override
			public void mapEvicted(MapEvent event) {
				notifyResourceChange();
			}
			
		};
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	@Listen(10)
	public void on(SystemStarted event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		
		serverResourceQuotas = hazelcastInstance.getReplicatedMap("serverResourceQuotas"); 
		serverResourceQuotas.addEntryListener(newResourceChangeListener());
		
		Map<String, Integer> resourceCounts = new HashMap<>();
		resourceCounts.put(CPU, serverConfig.getServerCpu());
		resourceCounts.put(MEMORY, serverConfig.getServerMemory());
		
		UUID localServerUUID = clusterManager.getLocalServerUUID();
		serverResourceQuotas.put(localServerUUID, resourceCounts);
		
		serverResourceUsages = hazelcastInstance.getMap("serverResourceUsages");
		serverResourceUsages.put(localServerUUID, new HashMap<>());
		serverResourceUsages.addEntryListener(newResourceChangeListener(), false);
		
		agentResourceQuotas = hazelcastInstance.getReplicatedMap("agentResourceQuotas");
		agentResourceQuotas.addEntryListener(newResourceChangeListener());
		
		agentResourceUsages = hazelcastInstance.getMap("agentResourceUsages");
		agentResourceUsages.addEntryListener(newResourceChangeListener(), false);
		
		agentPaused = hazelcastInstance.getMap("agentPaused");
		agentPaused.addEntryListener(newResourceChangeListener(), false);

		if (clusterManager.isLeaderServer()) {
			Query<?> query = dao.getSession().createQuery(String.format("select id, %s from Agent", Agent.PROP_PAUSED));
			for (Object[] fields: (List<Object[]>)query.list()) 
				agentPaused.put((Long)fields[0], (Boolean)fields[1]);
		}
		
		hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
			
			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				if (clusterManager.isLeaderServer()) {
					serverResourceQuotas.remove(membershipEvent.getMember().getUuid());
					serverResourceUsages.remove(membershipEvent.getMember().getUuid());
					
					Set<Long> agentIdsToRemove = new HashSet<>();
					for (var entry: agentManager.getAgentServers().entrySet()) {
						if (entry.getValue().equals(membershipEvent.getMember().getUuid()))
							agentIdsToRemove.add(entry.getKey());
					}
					for (Long agentId: agentIdsToRemove) {
						agentResourceQuotas.remove(agentId);
						agentResourceUsages.remove(agentId);
					}
				}
			}
			
			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
				
			}
			
		});
		
	}
	
	@Transactional
	@Listen
	public void on(AgentConnected event) {
		Long agentId = event.getAgent().getId();
		sessionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				// Synchronize at very start of the method to make sure it is not possible for db connection 
				// to wait for synchronization block
				synchronized (DefaultResourceAllocator.this) {
					Agent agent = agentManager.load(agentId);
					agentResourceQuotas.put(agentId, agent.getResources());
					agentResourceUsages.put(agentId, new HashMap<>());
				
					for (QueryCache cache: queryCaches.values()) {
						if (cache.query.matches(agent))
							cache.result.add(agentId);
					}
				}
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
				// Run in a separate thread to make sure it is not possible for db connection to 
				// wait for synchronization block
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						agentResourceQuotas.remove(agentId);
						agentResourceUsages.remove(agentId);
						
						synchronized (DefaultResourceAllocator.this) {
							for (QueryCache cache: queryCaches.values())
								cache.result.remove(agentId);
						}
					}
					
				});
			}
			
		});
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Agent) {
			Long agentId = event.getEntity().getId();
			boolean paused = ((Agent)event.getEntity()).isPaused();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					agentPaused.put(agentId, paused);
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
					synchronized (DefaultResourceAllocator.this) {
						agentPaused.remove(agentId);
					}
				}
			});
		}
	}
	
	private int getAllocationScore(Map<String, Integer> resourceQuotas, Map<String, Integer> resourceUsages, 
			Map<String, Integer> resourceRequirements) {
		for (Map.Entry<String, Integer> entry: resourceRequirements.entrySet()) {
			Integer totalCount = resourceQuotas.get(entry.getKey());
			if (totalCount == null)
				totalCount = 0;
			Integer usedCount = resourceUsages.get(entry.getKey());
			if (usedCount == null)
				usedCount = 0;
			if (usedCount + entry.getValue() > totalCount)
				return 0;
		}
		
		Integer cpuTotal = resourceQuotas.get(CPU);
		if (cpuTotal == null)
			cpuTotal = 0;
		Integer memoryTotal = resourceQuotas.get(MEMORY);
		if (memoryTotal == null)
			memoryTotal = 0;
		
		Integer cpuUsed = resourceUsages.get(CPU);
		if (cpuUsed == null)
			cpuUsed = 0;
		Integer cpuRequired = resourceRequirements.get(CPU);
		if (cpuRequired == null)
			cpuRequired = 0;
		cpuUsed += cpuRequired;
		if (cpuUsed == 0)
			cpuUsed = 1;
		
		Integer memoryUsed = resourceUsages.get(CPU);
		if (memoryUsed == null)
			memoryUsed = 0;
		Integer memoryRequired = resourceRequirements.get(CPU);
		if (memoryRequired == null)
			memoryRequired = 0;
		memoryUsed += memoryRequired;
		if (memoryUsed == 0)
			memoryUsed = 1;
		
		int score = cpuTotal*400/cpuUsed + memoryTotal*100/memoryUsed;
		if (score <= 0)
			score = 1;
		return score;
	}
	
	private UUID allocateServer(Map<String, Integer> resourceRequirements) {
		UUID allocatedServerUUID = null;
		
		synchronized(this) {
			while (true) {
				int maxScore = 0;
				for (Member server: clusterManager.getHazelcastInstance().getCluster().getMembers()) {
					var totalResourceCounts = serverResourceQuotas.get(server.getUuid());
					if (totalResourceCounts != null) {
						var usedResourceCounts = serverResourceUsages.get(server.getUuid());
						if (usedResourceCounts == null)
							usedResourceCounts = new HashMap<>();
						int score = getAllocationScore(totalResourceCounts, usedResourceCounts, resourceRequirements);
						if (score > maxScore) {
							allocatedServerUUID = server.getUuid();
							maxScore = score;
						}
					}
				}
				if (allocatedServerUUID != null)
					break;
				try {
					wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			} 
		}
		
		return allocatedServerUUID;
	}
	
	private Long allocateAgent(AgentQuery agentQuery, Map<String, Integer> resourceRequirements) {
		Set<Long> agentIds = agentManager.query(agentQuery, 0, Integer.MAX_VALUE)
				.stream().map(it->it.getId()).collect(Collectors.toSet());
		Long allocatedAgentId = 0L;
		
		synchronized(this) {
			String uuid = UUID.randomUUID().toString();
			queryCaches.put(uuid, new QueryCache(agentQuery, agentIds));
			try {
				while (true) {
					int maxScore = 0;
					for (Long agentId: agentIds) {
						Map<String, Integer> totalResourceCounts = agentResourceQuotas.get(agentId);
						Boolean paused = agentPaused.get(agentId);
						if (totalResourceCounts != null && paused != null && !paused) {
							var usedResourceCounts = agentResourceUsages.get(agentId);
							if (usedResourceCounts == null)
								usedResourceCounts = new HashMap<>();
							
							int score = getAllocationScore(totalResourceCounts, usedResourceCounts, resourceRequirements);
							if (score > maxScore) {
								allocatedAgentId = agentId;
								maxScore = score;
							}
						}
					}
					if (allocatedAgentId != 0)
						break;
					try {
						wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				queryCaches.remove(uuid);
			}
		}
		return allocatedAgentId;
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
			Map<String, Integer> usedResourceCounts = agentResourceUsages.remove(agentId);
			if (usedResourceCounts != null) {
				while (usedResourceCounts.values().stream().anyMatch(it->it>0)) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private void increaseResourceCounts(Map<String, Integer> resourceCounts, Map<String, Integer> increment) {
		for (Map.Entry<String, Integer> entry: increment.entrySet()) {
			Integer count = resourceCounts.get(entry.getKey());
			Integer newCount = (count != null? count + entry.getValue(): entry.getValue());
			resourceCounts.put(entry.getKey(), newCount >= 0? newCount: 0);
		}
	}
	
	private synchronized void increaseResourceUsages(UUID serverUUID, Map<String, Integer> increment) {
		Map<String, Integer> resourceCounts = serverResourceUsages.get(serverUUID);
		if (resourceCounts == null)
			resourceCounts = new HashMap<>();
		increaseResourceCounts(resourceCounts, increment);
		serverResourceUsages.put(serverUUID, resourceCounts);
	}

	private synchronized void increaseResourceUsages(Long agentId, Map<String, Integer> increment) {
		Map<String, Integer> resourceCounts = agentResourceUsages.get(agentId);
		if (resourceCounts == null)
			resourceCounts = new HashMap<>();
		increaseResourceCounts(resourceCounts, increment);
		agentResourceUsages.put(agentId, resourceCounts);
	}
	
	private Map<String, Integer> makeNegative(Map<String, Integer> map) {
		Map<String, Integer> negative = new HashMap<>();
		for (Map.Entry<String, Integer> entry: map.entrySet())
			negative.put(entry.getKey(), entry.getValue() * -1);
		return negative;
	}

	private AgentManager getAgentManager() {
		return OneDev.getInstance(AgentManager.class);
	}
	
	@Sessional
	protected AgentData getAgentData(Long agentId) {
		return getAgentManager().load(agentId).getAgentData();
	}
	
	@Override
	public void run(ResourceRunnable runnable, AgentQuery agentQuery, Map<String, Integer> resourceRequirements) {
		Future<?> future = null;
		try {
			if (agentQuery != null) {
				Long agentId = allocateAgent(agentQuery, resourceRequirements);
				UUID serverUUID = getAgentManager().getAgentServers().get(agentId);
				if (serverUUID == null)	
					throw new ExplicitException("Can not find server managing allocated agent, please retry later");

				future = clusterManager.submitToServer(serverUUID, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() throws Exception {
						updateLastUsedDate(agentId);
						
						AgentData agentData = getAgentData(agentId);
						Session agentSession = getAgentManager().getAgentSession(agentId);
						if (agentSession == null)
							throw new ExplicitException("Allocated agent not connected to current server, please retry later");
						  
						increaseResourceUsages(agentId, resourceRequirements);
						try {
							runnable.run(new AgentInfo(agentId, agentData, agentSession));
						} finally {
							increaseResourceUsages(agentId, makeNegative(resourceRequirements));
						}
						return null;
					}
					
				});
			} else {
				UUID serverUUID = allocateServer(resourceRequirements);
				future = clusterManager.submitToServer(serverUUID, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() throws Exception {
						UUID localServerUUID = clusterManager.getLocalServerUUID();
						increaseResourceUsages(localServerUUID, resourceRequirements);
						try {
							runnable.run(null);
						} finally {
							increaseResourceUsages(localServerUUID, makeNegative(resourceRequirements));
						}
						return null;
					}
					
				});
			}
			
			// future.get() here does not respond to thread interruption
			while (!future.isDone()) 
				Thread.sleep(1000);
			future.get(); // call get() to throw possible execution exceptions
		} catch (InterruptedException e) {
			if (future != null)
				future.cancel(true);
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
}
