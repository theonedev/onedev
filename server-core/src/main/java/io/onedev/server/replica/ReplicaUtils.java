package io.onedev.server.replica;

import io.onedev.server.replica.ProjectReplica.Type;

import java.util.*;

import static io.onedev.server.replica.ProjectReplica.Type.*;
import static java.util.Collections.reverse;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

public class ReplicaUtils {

	public static void redistributeProjects(Map<Long, Map<String, ProjectReplica>> replicas,
											Set<String> servers, int replicaCount) {
		// Normalize distributions
		for (var entry: replicas.values()) {
			entry.entrySet().removeIf(it -> !servers.contains(it.getKey()));
			var replicasOfProject = new ArrayList<>(entry.values());
			replicasOfProject.sort(comparing(ProjectReplica::getVersion));
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
				var replica = serverToReplica.getValue();
				if (replica.getType() != REDUNDANT) 
					serverReplicas.computeIfAbsent(server, k -> new LinkedHashMap<>()).put(projectId, replica);
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
				if (serverToReplica.getValue().getType() == PRIMARY) {
					primaryEntry = serverToReplica;
					primaryServerLoad = serverLoads.get(primaryEntry.getKey());
					break;
				}
			}

			var minBackupServerLoad = replicas.size();
			Map.Entry<String, ProjectReplica> minBackupEntry = null;
			for (var serverToReplica: replicasOfProject.entrySet()) {
				if (serverToReplica.getValue().getType() == BACKUP) {
					var backupServer = serverToReplica.getKey();
					var backupServerLoad = serverLoads.get(backupServer);
					if (backupServerLoad <  minBackupServerLoad) {
						minBackupServerLoad = backupServerLoad;
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
	
	private static Map<String, Integer> getServerLoads(Map<Long, Map<String, ProjectReplica>> replicas,
													 Set<String> servers, Set<Type> replicaTypes) {
		Map<String, Integer> serverLoads = new LinkedHashMap<>();
		for (var replicasOfProject: replicas.values()) {
			for (var entry: replicasOfProject.entrySet()) {
				if (replicaTypes.contains(entry.getValue().getType())) {
					Integer serverLoad = serverLoads.get(entry.getKey());
					if (serverLoad == null)
						serverLoad = 0;
					serverLoads.put(entry.getKey(), ++serverLoad);
				}
			}
		}
		servers.forEach(it->serverLoads.putIfAbsent(it, 0));
		return serverLoads;		
	}
	
	public static Map<String, ProjectReplica> addProject(Map<Long, Map<String, ProjectReplica>> replicas, 
													   Set<String> servers, int replicaCount, Long projectId) {
		for (var entry: replicas.values()) 
			entry.entrySet().removeIf(it -> !servers.contains(it.getKey()));
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
	
}
