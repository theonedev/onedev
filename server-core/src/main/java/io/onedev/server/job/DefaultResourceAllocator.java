package io.onedev.server.job;

import com.hazelcast.core.HazelcastInstance;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.cluster.ConnectionRestored;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.MAX_VALUE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Singleton
public class DefaultResourceAllocator implements ResourceAllocator, Serializable, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultResourceAllocator.class);

	private final AgentManager agentManager;

	private final ClusterManager clusterManager;

	private final TransactionManager transactionManager;

	private final JobManager jobManager;
	
	private final TaskScheduler taskScheduler;

	private volatile Map<String, Integer> nodeCpus;

	private final Map<String, Integer> resourceUsages = new HashMap<>();
	
	private volatile Map<String, Integer> resourceUsagesCache;

	private volatile Map<Long, Long> disconnectingAgents;
	
	private volatile String taskId;
	
	@Inject
	public DefaultResourceAllocator(AgentManager agentManager, TransactionManager transactionManager,
									ClusterManager clusterManager, JobManager jobManager, 
									TaskScheduler taskScheduler) {
		this.agentManager = agentManager;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
		this.jobManager = jobManager;
		this.taskScheduler = taskScheduler;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ResourceAllocator.class);
	}

	@Listen
	public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();

		nodeCpus = hazelcastInstance.getReplicatedMap("nodeCpus");
		var localServer = clusterManager.getLocalServerAddress();
		try {
			nodeCpus.put(
					localServer,
					new SystemInfo().getHardware().getProcessor().getLogicalProcessorCount());
		} catch (Exception e) {
			logger.debug("Error calling oshi", e);
			nodeCpus.put(localServer, 4);
		}
		resourceUsagesCache = hazelcastInstance.getReplicatedMap("resourceUsagesCache");
		disconnectingAgents = hazelcastInstance.getReplicatedMap("disconnectingAgents");
		removeNodeFromResourceUsagesCache(localServer);		
	}

	@Listen
	public void on(SystemStopped event) {
		if (nodeCpus != null)
			nodeCpus.remove(clusterManager.getLocalServerAddress());
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}
	
	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}
	
	@Listen
	public void on(ConnectionRestored event) {
		if (clusterManager.isLeaderServer()) {
			clusterManager.submitToServer(event.getServer(), () -> {
				try {
					while (true) {
						synchronized (resourceUsages) {
							if (resourceUsagesCache != null) {
								resourceUsagesCache.putAll(resourceUsages);
								break;
							}
						}
						Thread.sleep(100);
					}
				} catch (Exception e) {
					logger.error("Error syncing resource usages cache", e);
				}
				return null;
			});
		}
	}

	private void removeNodeFromResourceUsagesCache(String resourceNode) {
		var keysToRemove = resourceUsagesCache.keySet().stream()
				.filter(it -> it.startsWith(resourceNode + ":"))
				.collect(toList());
		for (var key: keysToRemove)
			resourceUsagesCache.remove(key);
	}

	@Transactional
	@Listen
	public void on(AgentConnected event) {
		var agentId = event.getAgent().getId();
		var agentCpus = event.getAgent().getCpus();
		transactionManager.runAfterCommit(() -> {
			nodeCpus.put(String.valueOf(agentId), agentCpus);
			removeNodeFromResourceUsagesCache(agentId.toString());
			synchronized (resourceUsages) {
				for (var entry : resourceUsages.entrySet()) {
					if (entry.getKey().startsWith(agentId + ":"))
						resourceUsagesCache.put(entry.getKey(), entry.getValue());
				}
			}
			disconnectingAgents.remove(agentId);
		});
	}

	@Transactional
	@Listen
	public void on(AgentDisconnected event) {
		var agentId = event.getAgent().getId();
		transactionManager.runAfterCommit(() -> {
			nodeCpus.remove(String.valueOf(agentId));
		});
	}

	private int getAllocationScore(int totalResources, int usedResources, int requiredResources) {
		if (usedResources + requiredResources <= totalResources)
			return totalResources * 100 / (usedResources + requiredResources);
		else
			return 0;
	}

	@Transactional
	protected void updateLastUsedDate(Long agentId) {
		agentManager.load(agentId).getLastUsedDate().setValue(new Date());
	}

	@Override
	public void agentDisconnecting(Long agentId) {
		disconnectingAgents.put(agentId, agentId);
		while (true) {
			boolean idle = true;
			synchronized (resourceUsages) {
				for (var entry : resourceUsages.entrySet()) {
					if (entry.getKey().startsWith(agentId + ":") && entry.getValue() > 0) {
						idle = false;
						break;
					}
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

	private int getEffectiveTotalResources(String resourceNode, int totalResources) {
		if (totalResources != 0) {
			return totalResources;
		} else {
			var cpu = nodeCpus.get(resourceNode);
			if (cpu != null)
				return cpu;
			else 
				return 0;
		}
	}

	@Nullable
	private String allocateResource(Collection<String> resourceNodes, String resourceType, 
									int totalResources, int requiredResources) {
		String allocated = null;
		var maxScore = 0;
		for (var resourceNode: resourceNodes) {
			var effectiveTotalResources = getEffectiveTotalResources(resourceNode, totalResources);
			var usedResources = resourceUsagesCache.get(resourceNode + ":" + resourceType);
			if (usedResources == null)
				usedResources = 0;
			var score = getAllocationScore(effectiveTotalResources, usedResources, requiredResources);

			if (score > maxScore) {
				allocated = resourceNode;
				maxScore = score;
			}
		}
		return allocated;
	}

	private void acquireResource(String resourceKey, int totalResources, int acquireResources) {
		while (true) {
			synchronized (resourceUsages) {	
				var usedResources = resourceUsages.get(resourceKey);				
				if (usedResources == null)
					usedResources = 0;
				usedResources += acquireResources;
				if (usedResources <= totalResources) {
					resourceUsages.put(resourceKey, usedResources);
					resourceUsagesCache.put(resourceKey, usedResources);
					break;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void releaseResource(String resourceKey, int releaseResources) {
		synchronized (resourceUsages) {	
			var usedResources = resourceUsages.get(resourceKey);
			usedResources -= releaseResources;
			resourceUsages.put(resourceKey, usedResources);
			resourceUsagesCache.put(resourceKey, usedResources);
		}
	}

	@Override
	public void runServerJob(String resourceType, int totalResources,
							 int requiredResources, ClusterRunnable runnable) {
		while (true) {
			var servers = clusterManager.getServerAddresses();
			servers.retainAll(clusterManager.getOnlineServers());
			var server = allocateResource(servers, resourceType, totalResources, requiredResources);
			if (server != null) {
				jobManager.runJob(server, () -> {
					int effectiveTotalResources = getEffectiveTotalResources(server, totalResources);
					var resourceKey = server + ":" + resourceType;
					acquireResource(resourceKey, effectiveTotalResources, requiredResources);
					try {
						jobManager.runJob(server, runnable);
					} finally {
						releaseResource(resourceKey, requiredResources);
					}
				});
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void runAgentJob(AgentQuery agentQuery, String resourceType,
							int totalResources, int requiredResources, 
							AgentRunnable runnable) {
		while (true) {
			var agentIds = agentManager.query(agentQuery, 0, MAX_VALUE)
					.stream().filter(it -> it.isOnline() && !it.isPaused())
					.map(AbstractEntity::getId)
					.collect(toSet());
			agentIds.removeAll(disconnectingAgents.keySet());
			var agentIdString = allocateResource(
					agentIds.stream().map(Object::toString).collect(toList()),
					resourceType, totalResources, requiredResources);
			var agentId = agentIdString != null? Long.valueOf(agentIdString): null;
			if (agentId != null) {
				var server = agentManager.getAgentServer(agentId);
				if (server == null)
					throw new ExplicitException("Can not find server managing allocated agent, please retry later");

				jobManager.runJob(server, () -> {
					var effectiveTotalResources = getEffectiveTotalResources(agentIdString, totalResources);
					var resourceKey = agentId + ":" + resourceType;
					acquireResource(resourceKey, effectiveTotalResources, requiredResources);
					try {
						updateLastUsedDate(agentId);
						runnable.run(agentId);
					} finally {
						releaseResource(resourceKey, requiredResources);
					}
				});
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void execute() {
		synchronized (resourceUsages) {
			resourceUsagesCache.putAll(resourceUsages);
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatHourlyForever();
	}
	
}
