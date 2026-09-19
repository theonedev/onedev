package io.onedev.server.service.impl;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.ResourceService;
import io.onedev.server.service.support.AgentCallable;
import io.onedev.server.event.Listen;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.agent.AgentQuery;
import oshi.SystemInfo;

@Singleton
public class DefaultResourceService implements ResourceService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultResourceService.class);

	private final AgentService agentService;

	private final ClusterService clusterService;

	private final TransactionService transactionService;

	private volatile Map<String, Integer> cpuCounts;

	/*
	 * Only the leader allocates resources. These counters intentionally are not replicated:
	 * after failover, existing tasks may temporarily exceed the limit until they finish.
	 * Each coordinating task releases its reservation on the same server that acquired it.
	 */
	private final Map<String, Integer> concurrencyUsages = new HashMap<>();

	// Guarded by concurrencyUsages, in order of arrival at the leader.
	private final List<AllocationRequest> allocationRequests = new ArrayList<>();

	private static class AllocationRequest {

		private final String resourceName;

		private Collection<String> nodes = Set.of();

		private AllocationRequest(String resourceName) {
			this.resourceName = resourceName;
		}

	}
	
	@Inject
	public DefaultResourceService(AgentService agentService, TransactionService transactionService,
			ClusterService clusterService) {
		this.agentService = agentService;
		this.transactionService = transactionService;
		this.clusterService = clusterService;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ResourceService.class);
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
	}

	@Listen
	public void on(SystemStopped event) {
		if (cpuCounts != null)
			cpuCounts.remove(clusterService.getLocalServerAddress());
	}

	@Transactional
	@Listen
	public void on(AgentConnected event) {
		var agentId = event.getAgent().getId();
		var agentCpuCount = event.getAgent().getCpuCount();
		transactionService.runAfterCommit(() -> {
			cpuCounts.put(String.valueOf(agentId), agentCpuCount);
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

	private String acquireNode(Supplier<Collection<String>> candidateNodes, String resourceName,
			int totalConcurrency, int requiredConcurrency) throws InterruptedException {
		var request = new AllocationRequest(resourceName);
		synchronized (concurrencyUsages) {
			allocationRequests.add(request);
		}
		try {
			while (true) {
				if (Thread.interrupted())
					throw new InterruptedException();
				if (!clusterService.isLeaderServer())
					throw new ExplicitException("Cluster leader changed, please retry later");
				// Agent queries may access the database, so resolve candidates outside the monitor.
				var nodes = new HashSet<>(candidateNodes.get());
				nodes.removeIf(node -> getEffectiveTotalConcurrency(node, totalConcurrency) < requiredConcurrency);
				synchronized (concurrencyUsages) {
					if (Thread.interrupted())
						throw new InterruptedException();
					request.nodes = nodes;
					var availableNodes = new HashSet<>(nodes);
					// Leave shared nodes to older requests, even if a smaller request could fit now.
					// Unrelated nodes/resources and requests with no eligible nodes do not block us.
					for (var earlier : allocationRequests) {
						if (earlier == request)
							break;
						if (earlier.resourceName.equals(resourceName))
							availableNodes.removeAll(earlier.nodes);
					}
					var node = allocateNode(availableNodes, resourceName, totalConcurrency, requiredConcurrency);
					if (node != null) {
						concurrencyUsages.merge(node + ":" + resourceName, requiredConcurrency, Integer::sum);
						return node;
					}
					// Poll capacity, eligibility, and leadership once per second to avoid wake-up storms.
					concurrencyUsages.wait(1000);
				}
			}
		} finally {
			synchronized (concurrencyUsages) {
				allocationRequests.remove(request);
			}
		}
	}

	// Called with concurrencyUsages locked, so selection and reservation are atomic.
	@Nullable
	private String allocateNode(Collection<String> nodes, String resourceName,
			int totalConcurrency, int requiredConcurrency) {
		String allocatedNode = null;
		var maxScore = 0;
		var nodeList = new ArrayList<>(nodes);
		Collections.shuffle(nodeList);
		for (var node : nodeList) {
			var effectiveTotalConcurrency = getEffectiveTotalConcurrency(node, totalConcurrency);
			var usedConcurrency = concurrencyUsages.getOrDefault(node + ":" + resourceName, 0);
			var score = getAllocationScore(effectiveTotalConcurrency, usedConcurrency, requiredConcurrency);
			if (score > maxScore) {
				allocatedNode = node;
				maxScore = score;
			}
		}
		return allocatedNode;
	}

	private void releaseConcurrency(String concurrencyKey, int releaseConcurrency) {
		synchronized (concurrencyUsages) {
			var usedConcurrency = concurrencyUsages.get(concurrencyKey) - releaseConcurrency;
			if (usedConcurrency != 0)
				concurrencyUsages.put(concurrencyKey, usedConcurrency);
			else
				concurrencyUsages.remove(concurrencyKey);
		}
	}

	private <T> T runTask(Supplier<Collection<String>> candidateNodes, String resourceName,
			int totalConcurrency, int requiredConcurrency, Function<String, T> task) throws InterruptedException {
		var node = acquireNode(candidateNodes, resourceName, totalConcurrency, requiredConcurrency);
		try {
			return task.apply(node);
		} finally {
			releaseConcurrency(node + ":" + resourceName, requiredConcurrency);
		}
	}

	@Override
	public <T> Future<T> submitServerTask(@Nullable String pinnedServerAddress, String resourceName,
			int totalConcurrency, int requiredConcurrency, ClusterTask<T> task) {
		return clusterService.submitToServer(clusterService.getLeaderServerAddress(), () -> runTask(() -> {
			var candidateServers = new ArrayList<>(clusterService.getOnlineServers());
			candidateServers.retainAll(new HashSet<>(clusterService.getServerAddresses()));
			if (pinnedServerAddress != null)
				candidateServers.retainAll(List.of(pinnedServerAddress));
			return candidateServers;
		}, resourceName, totalConcurrency, requiredConcurrency,
				server -> clusterService.runOnServer(server, task)));
	}

	@Override
	public <T> Future<T> submitAgentTask(@Nullable Long pinnedAgentId, AgentQuery agentQuery,
			String resourceName, int totalConcurrency, int requiredConcurrency, AgentCallable<T> task) {
		return clusterService.submitToServer(clusterService.getLeaderServerAddress(), () -> runTask(() -> {
			Set<Long> agentIds;
			if (pinnedAgentId != null) {
				agentIds = new HashSet<>();
				var agent = agentService.get(pinnedAgentId);
				if (agent != null && agent.isOnline() && !agent.isPaused())
					agentIds.add(pinnedAgentId);
			} else {
				agentIds = agentService.query(agentQuery, 0, MAX_VALUE)
						.stream().filter(it -> it.isOnline() && !it.isPaused())
						.map(AbstractEntity::getId)
						.collect(toSet());
			}
			return agentIds.stream().map(Object::toString).collect(toList());
		}, resourceName, totalConcurrency, requiredConcurrency, node -> {
			var agentId = Long.valueOf(node);
			var server = agentService.getAgentServer(agentId);
			if (server == null)
				throw new ExplicitException("Cannot find server managing allocated agent, please retry later");
			return clusterService.runOnServer(server, () -> {
				updateLastUsedDate(agentId);
				return task.call(agentId);
			});
		}));
	}

}
