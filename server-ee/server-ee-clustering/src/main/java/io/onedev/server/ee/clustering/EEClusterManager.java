package io.onedev.server.ee.clustering;

import io.onedev.server.ServerConfig;
import io.onedev.server.cluster.DefaultClusterManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.persistence.DataManager;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.replica.ProjectReplica;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static io.onedev.server.replica.ProjectReplica.Type.*;
import static java.util.Collections.reverse;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

@Singleton
public class EEClusterManager extends DefaultClusterManager {
	
	private final SettingManager settingManager;
	
	@Inject
	public EEClusterManager(ServerConfig serverConfig, DataManager dataManager,
							SettingManager settingManager, ListenerRegistry listenerRegistry, 
							HibernateConfig hibernateConfig) {
		super(serverConfig, dataManager, listenerRegistry, hibernateConfig);
		this.settingManager = settingManager;
	}

	@Override
	public void redistributeProjects(Map<Long, Map<String, ProjectReplica>> replicas) {
		var servers = new LinkedHashSet<>(getServerAddresses());
		var replicaCount = settingManager.getClusterSetting().getReplicaCount();
		// Normalize distributions
		for (var entry: replicas.values()) {
			var replicasOfProject = entry.entrySet().stream()
					.filter(it -> servers.contains(it.getKey()))
					.map(Map.Entry::getValue)
					.sorted(comparing(ProjectReplica::getVersion))
					.collect(toList());
			reverse(replicasOfProject);
			var primaryFound = false;
			for (var replica : replicasOfProject) {
				if (replica.getType() == PRIMARY) {
					if (primaryFound)
						replica.setType(BACKUP);
					else
						primaryFound = true;
				}
			}
			if (!primaryFound) {
				if (!replicasOfProject.isEmpty()) {
					replicasOfProject.iterator().next().setType(PRIMARY);
				} else {
					var replica = new ProjectReplica();
					replica.setType(PRIMARY);
					replica.setVersion(0);
					replicasOfProject.add(replica);
					entry.put(servers.iterator().next(), replica);
				}
			}
			int backupCount = 0;
			for (var replica : replicasOfProject) {
				if (replica.getType() == BACKUP && ++backupCount > replicaCount - 1)
					replica.setType(REDUNDANT);
			}
			if (backupCount < replicaCount - 1) {
				for (var replica : replicasOfProject) {
					if (replica.getType() == REDUNDANT) {
						replica.setType(BACKUP);
						backupCount++;
						if (backupCount == replicaCount - 1)
							break;
					}
				}
			}
			for (int i = 0; i < replicaCount - 1 - backupCount; i++) {
				for (var server : servers) {
					if (!entry.containsKey(server)) {
						ProjectReplica replica = new ProjectReplica();
						replica.setType(BACKUP);
						replica.setVersion(0);
						replicasOfProject.add(replica);
						entry.put(server, replica);
						break;
					}
				}
			}
		}

		// Now distribute all replicas across all servers evenly
		Map<String, Integer> serverLoads = getServerLoads(replicas, servers, EnumSet.of(PRIMARY, BACKUP));

		Map<String, Map<Long, ProjectReplica>> serverReplicas = new LinkedHashMap<>();
		for (var projectToReplicas: replicas.entrySet()) {
			var projectId = projectToReplicas.getKey();
			for (var serverToReplica: projectToReplicas.getValue().entrySet()) {
				var server = serverToReplica.getKey();
				if (servers.contains(server)) {
					var replica = serverToReplica.getValue();
					if (replica.getType() != REDUNDANT)
						serverReplicas.computeIfAbsent(server, k -> new LinkedHashMap<>()).put(projectId, replica);
				}
			}
		}
		servers.forEach(it->serverReplicas.putIfAbsent(it, new LinkedHashMap<>()));

		while (true) {
			String minLoadServer = null, maxLoadServer = null;
			int minServerLoad = replicas.size() + 1, maxServerLoad = -1;
			for (var entry : serverLoads.entrySet()) {
				if (entry.getValue() > maxServerLoad) {
					maxLoadServer = entry.getKey();
					maxServerLoad = entry.getValue();
				}
				if (entry.getValue() < minServerLoad) {
					minLoadServer = entry.getKey();
					minServerLoad = entry.getValue();
				}
			}
			if (maxServerLoad - minServerLoad >= 2) {
				var replicasOnMaxLoadServer = serverReplicas.get(maxLoadServer);
				var replicasOnMinLoadServer = serverReplicas.get(minLoadServer);
				var moveLoad = (maxServerLoad - minServerLoad) / 2;
				for (var i = 0; i < moveLoad; i++) {
					for (var it = replicasOnMaxLoadServer.entrySet().iterator(); it.hasNext();) {
						var entry = it.next();
						var projectId = entry.getKey();
						var replica = entry.getValue();
						if (!replicasOnMinLoadServer.containsKey(projectId)) {
							var replicasOfProject = replicas.get(projectId);
							var prevReplica = replicasOfProject.get(minLoadServer);
							if (prevReplica != null) {
								prevReplica.setType(replica.getType());
								replicasOnMinLoadServer.put(projectId, prevReplica);
							} else {
								var newReplica = new ProjectReplica();
								newReplica.setType(replica.getType());
								newReplica.setVersion(0);
								replicasOfProject.put(minLoadServer, newReplica);
								replicasOnMinLoadServer.put(projectId, newReplica);
							}
							replica.setType(REDUNDANT);
							it.remove();
							break;
						}
					}
				}
				serverLoads.put(maxLoadServer, maxServerLoad - moveLoad);
				serverLoads.put(minLoadServer, minServerLoad + moveLoad);
			} else {
				break;
			}
		}

		// Now distribute primary replicas across servers evenly (best try)
		serverLoads = getServerLoads(replicas, servers, EnumSet.of(PRIMARY));

		for (var replicasOfProject: replicas.values()) {
			var primaryServerLoad = -1;
			Map.Entry<String, ProjectReplica> primaryEntry = null;
			for (var serverToReplica: replicasOfProject.entrySet()) {
				var server = serverToReplica.getKey();
				if (servers.contains(server) && serverToReplica.getValue().getType() == PRIMARY) {
					primaryEntry = serverToReplica;
					primaryServerLoad = serverLoads.get(server);
					break;
				}
			}

			var minBackupServerLoad = replicas.size();
			Map.Entry<String, ProjectReplica> minBackupEntry = null;
			for (var serverToReplica: replicasOfProject.entrySet()) {
				var server = serverToReplica.getKey();
				var replica = serverToReplica.getValue();
				if (servers.contains(server) && replica.getType() == BACKUP) {
					var serverLoad = serverLoads.get(server);
					if (serverLoad <  minBackupServerLoad) {
						minBackupServerLoad = serverLoad;
						minBackupEntry = serverToReplica;
					}
				}
			}

			if (primaryServerLoad - minBackupServerLoad >= 2) {
				primaryEntry.getValue().setType(BACKUP);
				minBackupEntry.getValue().setType(PRIMARY);
				serverLoads.put(primaryEntry.getKey(), primaryServerLoad - 1);
				serverLoads.put(minBackupEntry.getKey(), minBackupServerLoad + 1);
			}
		}
	}

	private Map<String, Integer> getServerLoads(Map<Long, Map<String, ProjectReplica>> replicas,
													   Collection<String> servers, Set<ProjectReplica.Type> replicaTypes) {
		Map<String, Integer> serverLoads = new LinkedHashMap<>();
		for (var replicasOfProject: replicas.values()) {
			for (var serverToReplica: replicasOfProject.entrySet()) {
				var server = serverToReplica.getKey();
				if (servers.contains(server)) {
					var replica = serverToReplica.getValue();
					if (replicaTypes.contains(replica.getType())) {
						Integer serverLoad = serverLoads.get(server);
						if (serverLoad == null)
							serverLoad = 0;
						serverLoads.put(server, ++serverLoad);
					}
				}
			}
		}
		servers.forEach(it->serverLoads.putIfAbsent(it, 0));
		return serverLoads;
	}

	@Override
	public Map<String, ProjectReplica> addProject(Map<Long, Map<String, ProjectReplica>> replicas, Long projectId) {
		var servers = new HashSet<>(getServerAddresses());
		var replicaCount = settingManager.getClusterSetting().getReplicaCount();
		
		var serverLoads = getServerLoads(replicas, servers, EnumSet.of(PRIMARY, BACKUP));
		var sortedServers = new ArrayList<>(servers);
		sortedServers.sort(comparingInt(serverLoads::get));

		Map<String, ProjectReplica> replicasOfNewProject = new HashMap<>();
		for (var server: sortedServers) {
			var replica = new ProjectReplica();
			replica.setType(BACKUP);
			replica.setVersion(0);
			replicasOfNewProject.put(server, replica);
			if (replicasOfNewProject.size() >= replicaCount)
				break;
		}

		serverLoads = getServerLoads(replicas, servers, EnumSet.of(PRIMARY));
		sortedServers = new ArrayList<>(replicasOfNewProject.keySet());
		var primaryServer = sortedServers.stream().min(comparing(serverLoads::get)).get();
		replicasOfNewProject.get(primaryServer).setType(PRIMARY);

		return replicasOfNewProject;
	}

	@Override
	public boolean isClusteringSupported() {
		return true;
	}
	
}