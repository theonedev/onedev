package io.onedev.server.job;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.replicatedmap.ReplicatedMap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Agent;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.agent.AgentQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class DefaultResourceAllocator implements ResourceAllocator, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultResourceAllocator.class);
	
	private final AgentManager agentManager;
	
	private final ClusterManager clusterManager;
	
	private final TransactionManager transactionManager;
	
	private final JobManager jobManager;
	
	private final Object serverAllocSync = new Object();
	
	private final Object agentAllocSync = new Object();
	
	private volatile ReplicatedMap<UUID, Integer> serverCpus;

	private volatile ReplicatedMap<Long, Integer> agentCpus;
	
	private volatile IMap<String, Integer> serverUsed;
	
	private volatile IMap<String, Integer> agentUsed;
	
	private volatile IMap<Long, Long> agentDisconnecting;
	
	@Inject
	public DefaultResourceAllocator(AgentManager agentManager, TransactionManager transactionManager, 
									ClusterManager clusterManager, JobManager jobManager) {
		this.agentManager = agentManager;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
		this.jobManager = jobManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ResourceAllocator.class);
	}
	
	@Transactional
	@Listen(10)
	public void on(SystemStarted event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		
		serverCpus = hazelcastInstance.getReplicatedMap("serverCpus"); 
		
		UUID localServerUUID = clusterManager.getLocalServerUUID();
		try {
			serverCpus.put(
					localServerUUID, 
					new SystemInfo().getHardware().getProcessor().getLogicalProcessorCount());
		} catch (Exception e) {
			logger.debug("Error calling oshi", e);
			serverCpus.put(localServerUUID, 4);
		}

		agentCpus = hazelcastInstance.getReplicatedMap("agentCpus");
		serverUsed = hazelcastInstance.getMap("serverUsed");
		agentUsed = hazelcastInstance.getMap("agentUsed");
		agentDisconnecting = hazelcastInstance.getMap("agentDisconnecting");
		
		hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
			
			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				if (clusterManager.isLeaderServer()) {
					UUID removedServerUUID = membershipEvent.getMember().getUuid();
					serverCpus.remove(removedServerUUID);
					
					Set<String> keysToRemove = new HashSet<>();
					for (var key: serverUsed.keySet()) {	
						if (key.startsWith(removedServerUUID.toString() + ":"))
							keysToRemove.add(key);
					}
					for (var keyToRemove: keysToRemove)
						serverUsed.remove(keyToRemove);
					
					Set<Long> agentIdsToRemove = new HashSet<>();
					for (var entry: agentManager.getAgentServers().entrySet()) {
						if (entry.getValue().equals(removedServerUUID))
							agentIdsToRemove.add(entry.getKey());
					}
					
					keysToRemove.clear();
					for (var agentId: agentIdsToRemove) {
						agentCpus.remove(agentId);
						for (var key: agentUsed.keySet()) {
							if (key.startsWith(agentId + ":"))
								keysToRemove.add(key);
						}
					}
					for (var keyToRemove: keysToRemove)
						agentUsed.remove(keyToRemove);
				}
			}
			
			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
				notifyServerAlloc();
			}
			
		});
		
	}
	
	@Transactional
	@Listen
	public void on(AgentConnected event) {
		Long agentId = event.getAgent().getId();
		Integer agentCpus = event.getAgent().getCpus();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				DefaultResourceAllocator.this.agentCpus.put(agentId, agentCpus);
				agentDisconnecting.remove(agentId);
				notifyAgentAlloc();
			}
			
		});
	}
	
	private void notifyServerAlloc() {
		clusterManager.submitToAllServers((ClusterTask<Void>) () -> {
			synchronized (serverAllocSync) {
				serverAllocSync.notifyAll();
			}
			return null;
		});
	}

	private void notifyAgentAlloc() {
		clusterManager.submitToAllServers((ClusterTask<Void>) () -> {
			synchronized (agentAllocSync) {
				agentAllocSync.notifyAll();
			}
			return null;
		});
	}
	
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Agent) {
			Agent agent = (Agent) event.getEntity();
			if (!agent.isPaused()) 
				notifyAgentAlloc();
		}
	}
	
	@Transactional
	@Listen
	public void on(AgentDisconnected event) {
		Long agentId = event.getAgent().getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				agentCpus.remove(agentId);
				
				Set<String> keysToRemove = new HashSet<>();
				for (var key: agentUsed.keySet()) {
					if (key.startsWith(agentId + ":"))
						keysToRemove.add(key);
				}
				for (var keyToRemove: keysToRemove)
					agentUsed.remove(keyToRemove);
			}
			
		});
	}
	
	private int getAllocationScore(int total, int used, int required) {
		if (used + required <= total) 
			return total * 100 / (used + required);			
		else 
			return 0;
	}
	
	@Transactional
	protected void updateLastUsedDate(Long agentId) {
		agentManager.load(agentId).setLastUsedDate(new Date());
	}

	@Override
	public void wantToDisconnectAgent(Long agentId) {
		agentDisconnecting.put(agentId, agentId);		
		while (true) {
			boolean idle = true;
			for (var entry : agentUsed.entrySet()) {
				if (entry.getKey().startsWith(agentId + ":") && entry.getValue() > 0) {
					idle = false;
					break;
				}
			}
			if (idle) {
				break;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private AgentManager getAgentManager() {
		return OneDev.getInstance(AgentManager.class);
	}

	private <T> int getEffectiveTotal(Map<T, Integer> cpuMap, T key, int total) {
		Integer effectiveTotal = total;
		if (effectiveTotal == 0)
			effectiveTotal = cpuMap.get(key);
		if (effectiveTotal == null)
			effectiveTotal = 0;
		return effectiveTotal;
	}
	
	private <T> T allocate(Collection<T> pool, Map<T, Integer> cpuMap, Map<String, Integer> usedMap,
						   String resourceHolder, int total, int required) {
		T allocated = null;
		int maxScore = 0;
		for (var each: pool) {
			int effectiveTotal = getEffectiveTotal(cpuMap, each, total);
			Integer used = usedMap.get(each + ":" + resourceHolder);
			if (used == null)
				used = 0;
			int score = getAllocationScore(effectiveTotal, used, required);

			if (score > maxScore) {
				allocated = each;
				maxScore = score;
			}
		}
		return allocated;
	}

	private boolean acquire(IMap<String, Integer> used, String key, int total, int required) {
		while (true) {
			Integer prevValue = used.get(key);
			if (prevValue != null) {
				if (prevValue + required <= total) {
					if (used.replace(key, prevValue, prevValue + required))
						return true;
				} else {
					return false;
				}
			} else {
				if (required <= total) {
					if (used.putIfAbsent(key, required) == null)
						return true;
				} else {
					return false;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void release(IMap<String, Integer> used, String key, int required) {
		while (true) {
			int prevValue = used.get(key);
			if (used.replace(key, prevValue, prevValue - required))
				break;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public void runServerJob(String resourceHolder, int total, int required,
							 ClusterRunnable runnable) {
		UUID serverUUID;
		synchronized (serverAllocSync) {
			while (true) {
				Collection<UUID> serverUUIDs = clusterManager.getHazelcastInstance().getCluster().getMembers()
						.stream().map(Member::getUuid).collect(Collectors.toSet());
				serverUUID = allocate(serverUUIDs, serverCpus, serverUsed,
						resourceHolder, total, required);
				if (serverUUID != null) {
					int effectiveTotal = getEffectiveTotal(serverCpus, serverUUID, total);
					if (acquire(serverUsed, serverUUID + ":" + resourceHolder, effectiveTotal, required)) 
						break;
				}
				try {
					serverAllocSync.wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
		try {
			jobManager.runJob(serverUUID, runnable);
		} finally {
			release(serverUsed, serverUUID + ":" + resourceHolder, required);
			notifyServerAlloc();
		}
	}

	@Override
	public void runAgentJob(AgentQuery agentQuery, String resourceHolder, 
							int total, int required, AgentRunnable runnable) {
		Long agentId;
		synchronized (agentAllocSync) {
			while (true) {
				Collection<Long> agentIds = agentManager.query(agentQuery, 0, Integer.MAX_VALUE)
						.stream().filter(it-> it.isOnline() && !it.isPaused())
						.map(AbstractEntity::getId)
						.collect(Collectors.toSet());
				agentIds.removeAll(agentDisconnecting.keySet());
				agentId = allocate(agentIds, agentCpus, agentUsed, resourceHolder, total, required);
				if (agentId != null) {
					int effectiveTotal = getEffectiveTotal(agentCpus, agentId, total);
					if (acquire(agentUsed, agentId + ":" + resourceHolder, effectiveTotal, required)) 
						break;
				}
				try {
					agentAllocSync.wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}

		try {
			updateLastUsedDate(agentId);
			UUID serverUUID = getAgentManager().getAgentServers().get(agentId);
			if (serverUUID == null)
				throw new ExplicitException("Can not find server managing allocated agent, please retry later");

			Long finalAgentId = agentId;
			jobManager.runJob(serverUUID, () -> runnable.run(finalAgentId));
		} finally {
			release(agentUsed, agentId + ":" + resourceHolder, required);
			notifyAgentAlloc();
		}
	}

}
