package io.onedev.server.job;

import static java.lang.Integer.MAX_VALUE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.AgentService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.cluster.ConnectionRestored;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import oshi.SystemInfo;

@Singleton
public class DefaultResourceAllocator implements ResourceAllocator, Serializable, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultResourceAllocator.class);

	private final AgentService agentService;

	private final ClusterService clusterService;

	private final TransactionService transactionService;

	private final JobService jobService;
	
	private final TaskScheduler taskScheduler;

	private volatile Map<String, Integer> cpuCounts;

	private final Map<String, Integer> concurrencyUsages = new HashMap<>();
	
	private volatile Map<String, Integer> concurrencyUsagesCache;
	
	private volatile Map<Long, Long> disconnectingAgents;
	
	private volatile String taskId;
	
	@Inject
	public DefaultResourceAllocator(AgentService agentService, TransactionService transactionService,
                                    ClusterService clusterService, JobService jobService,
                                    TaskScheduler taskScheduler) {
		this.agentService = agentService;
		this.transactionService = transactionService;
		this.clusterService = clusterService;
		this.jobService = jobService;
		this.taskScheduler = taskScheduler;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ResourceAllocator.class);
	}

	@Listen
	public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();

		cpuCounts = hazelcastInstance.getReplicatedMap("cpuCounts");
		var localServer = clusterService.getLocalServerAddress();
		try {
			cpuCounts.put(
					localServer,
					new SystemInfo().getHardware().getProcessor().getLogicalProcessorCount());
		} catch (Exception e) {
			logger.debug("Error calling oshi", e);
			cpuCounts.put(localServer, 4);
		}
		concurrencyUsagesCache = hazelcastInstance.getReplicatedMap("concurrencyUsagesCache");
		disconnectingAgents = hazelcastInstance.getReplicatedMap("disconnectingAgents");
		removeNodeFromConcurrencyUsagesCache(localServer);		
	}

	@Listen
	public void on(SystemStopped event) {
		if (cpuCounts != null)
			cpuCounts.remove(clusterService.getLocalServerAddress());
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
		if (clusterService.isLeaderServer()) {
			clusterService.submitToServer(event.getServer(), () -> {
				try {
					while (true) {
						synchronized (concurrencyUsages) {
							if (concurrencyUsagesCache != null) {
								concurrencyUsagesCache.putAll(concurrencyUsages);
								break;
							}
						}
						Thread.sleep(100);
					}
				} catch (Exception e) {
					logger.error("Error syncing concurrency usages cache", e);
				}
				return null;
			});
		}
	}

	private void removeNodeFromConcurrencyUsagesCache(String node) {
		var keysToRemove = concurrencyUsagesCache.keySet().stream()
				.filter(it -> it.startsWith(node + ":"))
				.collect(toList());
		for (var key: keysToRemove)
			concurrencyUsagesCache.remove(key);
	}

	@Transactional
	@Listen
	public void on(AgentConnected event) {
		var agentId = event.getAgent().getId();
		var agentCpuCount = event.getAgent().getCpuCount();
		transactionService.runAfterCommit(() -> {
			cpuCounts.put(String.valueOf(agentId), agentCpuCount);
			removeNodeFromConcurrencyUsagesCache(agentId.toString());
			synchronized (concurrencyUsages) {
				for (var entry : concurrencyUsages.entrySet()) {
					if (entry.getKey().startsWith(agentId + ":"))
						concurrencyUsagesCache.put(entry.getKey(), entry.getValue());
				}
			}
			disconnectingAgents.remove(agentId);
		});
	}

	@Transactional
	@Listen
	public void on(AgentDisconnected event) {
		var agentId = event.getAgent().getId();
		transactionService.runAfterCommit(() -> {
			cpuCounts.remove(String.valueOf(agentId));
		});
	}

	private int getAllocationScore(int totalConcurrency, int usedConcurrency, int requiredConcurrency) {
		if (usedConcurrency + requiredConcurrency <= totalConcurrency)
			return totalConcurrency * 100 / (usedConcurrency + requiredConcurrency);
		else
			return 0;
	}

	@Transactional
	protected void updateLastUsedDate(Long agentId) {
		agentService.load(agentId).getLastUsedDate().setValue(new Date());
	}

	@Override
	public void agentDisconnecting(Long agentId) {
		disconnectingAgents.put(agentId, agentId);
		while (true) {
			boolean idle = true;
			synchronized (concurrencyUsages) {
				for (var entry : concurrencyUsages.entrySet()) {
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

	private int getEffectiveTotalConcurrency(String node, int totalConcurrency) {
		if (totalConcurrency != 0) {
			return totalConcurrency;
		} else {
			var cpu = cpuCounts.get(node);
			if (cpu != null)
				return cpu;
			else 
				return 0;
		}
	}

	@Nullable
	private String allocateNode(Collection<String> nodes, String executorName,
								int totalConcurrency, int requiredConcurrency) {
		String allocatedNode = null;
		var maxScore = 0;
		var nodeList = new ArrayList<>(nodes);
		Collections.shuffle(nodeList);
		for (var node: nodeList) {
			var effectiveTotalConcurrency = getEffectiveTotalConcurrency(node, totalConcurrency);
			var usedConcurrency = concurrencyUsagesCache.get(node + ":" + executorName);
			if (usedConcurrency == null)
				usedConcurrency = 0;
			var score = getAllocationScore(effectiveTotalConcurrency, usedConcurrency, requiredConcurrency);

			if (score > maxScore) {
				allocatedNode = node;
				maxScore = score;
			}
		}
		return allocatedNode;
	}

	private void acquireConcurrency(String concurrencyKey, int totalConcurrency, int acquireConcurrency) {
		while (true) {
			synchronized (concurrencyUsages) {	
				var usedCocurrency = concurrencyUsages.get(concurrencyKey);				
				if (usedCocurrency == null)
					usedCocurrency = 0;
				usedCocurrency += acquireConcurrency;
				if (usedCocurrency <= totalConcurrency) {
					concurrencyUsages.put(concurrencyKey, usedCocurrency);
					concurrencyUsagesCache.put(concurrencyKey, usedCocurrency);
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

	private void releaseConcurrency(String concurrencyKey, int releaseConcurrency) {
		synchronized (concurrencyUsages) {	
			var usedResources = concurrencyUsages.get(concurrencyKey);
			usedResources -= releaseConcurrency;
			concurrencyUsages.put(concurrencyKey, usedResources);
			concurrencyUsagesCache.put(concurrencyKey, usedResources);
		}
	}

	@Override
	public boolean runServerJob(String executorName, int totalConcurrency, 
								int requiredConcurrency, ClusterTask<Boolean> runnable) {
		while (true) {
			var servers = clusterService.getServerAddresses();
			servers.retainAll(clusterService.getOnlineServers());
			var server = allocateNode(servers, executorName, totalConcurrency, requiredConcurrency);
			if (server != null) {
				return jobService.runJob(server, () -> {
					int effectiveTotalConcurrency = getEffectiveTotalConcurrency(server, totalConcurrency);
					var concurrencyKey = server + ":" + executorName;
					acquireConcurrency(concurrencyKey, effectiveTotalConcurrency, requiredConcurrency);
					try {
						return jobService.runJob(server, runnable);
					} finally {
						releaseConcurrency(concurrencyKey, requiredConcurrency);
					}
				});
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean runAgentJob(AgentQuery agentQuery, String executorName,
							int totalConcurrency, int requiredConcurrency, 
							AgentRunnable runnable) {
		while (true) {
			var agentIds = agentService.query(agentQuery, 0, MAX_VALUE)
					.stream().filter(it -> it.isOnline() && !it.isPaused())
					.map(AbstractEntity::getId)
					.collect(toSet());
			agentIds.removeAll(disconnectingAgents.keySet());
			var agentIdString = allocateNode(
					agentIds.stream().map(Object::toString).collect(toList()),
					executorName, totalConcurrency, requiredConcurrency);
			var agentId = agentIdString != null? Long.valueOf(agentIdString): null;
			if (agentId != null) {
				var server = agentService.getAgentServer(agentId);
				if (server == null)
					throw new ExplicitException("Can not find server managing allocated agent, please retry later");

				return jobService.runJob(server, () -> {
					var effectiveTotalConcurrency = getEffectiveTotalConcurrency(agentIdString, totalConcurrency);
					var concurrencyKey = agentId + ":" + executorName;
					acquireConcurrency(concurrencyKey, effectiveTotalConcurrency, requiredConcurrency);
					try {
						updateLastUsedDate(agentId);
						return runnable.run(agentId);
					} finally {
						releaseConcurrency(concurrencyKey, requiredConcurrency);
					}
				});
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
		synchronized (concurrencyUsages) {
			concurrencyUsagesCache.putAll(concurrencyUsages);
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatHourlyForever();
	}
	
}
