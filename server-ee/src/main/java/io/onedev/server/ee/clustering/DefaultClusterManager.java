package io.onedev.server.ee.clustering;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.cp.IAtomicLong;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.ServerConfig;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.data.DataManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.cluster.ConnectionLost;
import io.onedev.server.event.cluster.ConnectionRestored;
import io.onedev.server.exception.ServerNotFoundException;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.replica.ProjectReplica;
import org.eclipse.jetty.server.session.SessionData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static io.onedev.server.persistence.PersistenceUtils.*;
import static io.onedev.server.replica.ProjectReplica.Type.*;
import static java.lang.String.format;
import static java.util.Collections.reverse;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

@Singleton
public class DefaultClusterManager implements ClusterManager, Serializable {
	
	private static final String EXECUTOR_SERVICE_NAME = "default";
	
	private static final String TABLE_SERVER = "o_ClusterServer";
	
	private static final String COLUMN_ADDRESS = "o_address";
	
	private final ServerConfig serverConfig;
	
	private final HibernateConfig hibernateConfig;
	
	private final DataManager dataManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final SettingManager settingManager;
	
	private final SubscriptionManager subscriptionManager;
	
	private volatile Map<String, Integer> httpPorts;
	
	private volatile Map<String, Integer> sshPorts;
	
	private volatile Map<String, String> serverNames;
	
	private volatile Map<String, String> onlineServers;
	
	private volatile HazelcastInstance hazelcastInstance;
	
	@Inject
	public DefaultClusterManager(ServerConfig serverConfig, DataManager dataManager,
								 ListenerRegistry listenerRegistry, HibernateConfig hibernateConfig,
								 SettingManager settingManager, SubscriptionManager subscriptionManager) { 
		this.serverConfig = serverConfig;
		this.dataManager = dataManager;
		this.listenerRegistry = listenerRegistry;
		this.hibernateConfig = hibernateConfig;
		this.settingManager = settingManager;
		this.subscriptionManager = subscriptionManager;
	}
	
	@Override
	public String getLocalServerAddress() {
		return serverConfig.getClusterIp() + ":" +  serverConfig.getClusterPort();
	}
	
	@Override
	public void start() {
		var localServer = getLocalServerAddress();
		Collection<String> servers;
		try (var conn = dataManager.openConnection()) {
			servers = callWithLock(conn, () -> {
				var innerServers = new HashSet<String>();
				if (!tableExists(conn, TABLE_SERVER)) {
					try (var stmt = conn.createStatement()) {
						stmt.execute(format("create table %s (%s varchar(255))", TABLE_SERVER, COLUMN_ADDRESS));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
				try (Statement stmt = conn.createStatement()) {
					String query = format("select %s from %s", COLUMN_ADDRESS, TABLE_SERVER);
					try (ResultSet resultset = stmt.executeQuery(query)) {
						while (resultset.next())
							innerServers.add(resultset.getString(1));
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}

				if (!innerServers.contains(localServer)) {
					try (Statement stmt = conn.createStatement()) {
						stmt.executeUpdate(format(
								"insert into %s values('%s')",
								TABLE_SERVER, localServer));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
					innerServers.add(localServer);
				}
				return innerServers;
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		};

		Config config = new Config();
		config.setClusterName(hibernateConfig.getClusterCredential());
		config.setInstanceName(localServer);
		config.setProperty("hazelcast.shutdownhook.enabled", "false");
		config.getExecutorConfig(EXECUTOR_SERVICE_NAME).setPoolSize(Integer.MAX_VALUE);
		config.getMapConfig("default").setStatisticsEnabled(false);
		config.getNetworkConfig().setPort(serverConfig.getClusterPort()).setPortAutoIncrement(false);
		config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
		SerializerConfig sc = new SerializerConfig()
				.setImplementation(new SessionDataSerializer())
				.setTypeClass(SessionData.class);
		config.getSerializationConfig().addSerializerConfig(sc);
		
		var hasLocalhost = false;
		var hasNonLocalhost = false;
		for (String server: servers) {
			if (server.startsWith("127.0.0.1:"))
				hasLocalhost = true;
			else 
				hasNonLocalhost = true;
			if (!server.equals(localServer))
				config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(server);
		}
		if (hasLocalhost && hasNonLocalhost)
			throw new ExplicitException("Invalid servers detected in cluster: loopback address should not be used");
		config.getNetworkConfig().setPublicAddress(localServer);
		hazelcastInstance = Hazelcast.newHazelcastInstance(config);

		hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
				var server = getServerAddress(membershipEvent.getMember());
				if (onlineServers.containsKey(server)) 
					listenerRegistry.post(new ConnectionRestored(server));
			}

			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				var server = getServerAddress(membershipEvent.getMember());
				if (onlineServers.containsKey(server)) 
					listenerRegistry.post(new ConnectionLost(server));
			}
		});
		
		httpPorts = hazelcastInstance.getMap("httpPorts");
		sshPorts = hazelcastInstance.getMap("sshPorts");
		serverNames = hazelcastInstance.getMap("serverNames");
		onlineServers = hazelcastInstance.getReplicatedMap("onlineServers");
		
		httpPorts.put(getLocalServerAddress(), serverConfig.getHttpPort());
		sshPorts.put(getLocalServerAddress(), serverConfig.getSshPort());
		serverNames.put(getLocalServerAddress(), serverConfig.getServerName());
	}

	@Override
	public void postStart() {
		var localServer = getLocalServerAddress();
		onlineServers.put(localServer, localServer);
	}

	@Override
	public void preStop() {
		if (onlineServers != null)
			onlineServers.remove(getLocalServerAddress());
	}

	@Override
	public void stop() {
		var localServer = getLocalServerAddress();
		if (httpPorts != null)
			httpPorts.put(localServer, serverConfig.getHttpPort());
		if (sshPorts != null)
			sshPorts.put(localServer, serverConfig.getSshPort());
		if (serverNames != null)
			serverNames.put(getLocalServerAddress(), serverConfig.getServerName());

		if (hazelcastInstance != null) {
			hazelcastInstance.shutdown();
			hazelcastInstance = null;
		}

		try (var conn = dataManager.openConnection()) {
			callWithTransaction(conn, () -> {
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate(String.format(
							"delete from %s where %s='%s'",
							TABLE_SERVER, COLUMN_ADDRESS, localServer));
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				return true;				
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Collection<String> getOnlineServers() {
		return onlineServers.keySet();
	}
	
	@Override
	public HazelcastInstance getHazelcastInstance() {
		return hazelcastInstance;
	}

	@Override
	public boolean isLeaderServer() {
		return hazelcastInstance.getCluster().getMembers().iterator().next().localMember();
	}

	@Override
	public void init(IAtomicLong data, Callable<Long> initializer) {
		while (data.get() == 0) {
			if (isLeaderServer()) {
				try {
					data.compareAndSet(0, initializer.call().longValue());
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public Member getServer(String serverAddress, boolean mustExist) {
		for (var member: hazelcastInstance.getCluster().getMembers()) {
			if (getServerAddress(member).equals(serverAddress))
				return member;
		}
		if (mustExist) {
			throw new ServerNotFoundException("Unable to find server '" + serverAddress + "', " +
					"this normally happens when project replica status is out of sync with cluster");
		} else {
			return null;
		}
	}
	
	private <T> T getResult(Future<T> future) {
		try {
			return future.get();
		} catch (InterruptedException e) {
			future.cancel(true);
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T runOnServer(String serverAddress, ClusterTask<T> task) {
		return getResult(submitToServer(serverAddress, task));
	}
	
	@Override
	public <T> T runOnServer(Member server, ClusterTask<T> task) {
		return getResult(submitToServer(server, task));
	}
	
	private IExecutorService getExecutorService() {
		return hazelcastInstance.getExecutorService(EXECUTOR_SERVICE_NAME);
	}
	
	@Override
	public <T> Future<T> submitToServer(String serverAddress, ClusterTask<T> task) {
		return getExecutorService().submitToMember(task, getServer(serverAddress, true));
	}
	
	@Override
	public <T> Future<T> submitToServer(Member server, ClusterTask<T> task) {
		return getExecutorService().submitToMember(task, server);
	}
	
	@Override
	public <T> Map<String, Future<T>> submitToAllServers(ClusterTask<T> task) {
		Map<String, Future<T>> futures = new HashMap<>();
		for (var entry: getExecutorService().submitToAllMembers(task).entrySet()) {
			futures.put(getServerAddress(entry.getKey()), entry.getValue());
		}
		return futures;
	}

	@Override
	public <T> Map<String, Future<T>> submitToServers(Collection<String> serverAddresses, ClusterTask<T> task) {
		Map<String, Future<T>> futures = new HashMap<>();
		var servers = hazelcastInstance.getCluster().getMembers().stream().filter(it -> serverAddresses.contains(getServerAddress(it))).collect(toList());
		if (!servers.isEmpty()) {
			for (var entry : getExecutorService().submitToMembers(task, servers).entrySet()) {
				futures.put(getServerAddress(entry.getKey()), entry.getValue());
			}
		}
		return futures;
	}
	
	@Override
	public <T> Map<String, T> runOnAllServers(ClusterTask<T> task) {
		return waitFor(submitToAllServers(task));
	}

	@Override
	public <T> Map<String, T> runOnServers(Collection<String> serverAddresses, ClusterTask<T> task) {
		return waitFor(submitToServers(serverAddresses, task));
	}
	
	private <T> Map<String, T> waitFor(Map<String, Future<T>> futures) {
		Map<String, T> result = new HashMap<>();
		try {
			for (var entry: futures.entrySet())
				result.put(entry.getKey(), entry.getValue().get());
		} catch (InterruptedException | ExecutionException e) {
			for (var entry: futures.entrySet())
				entry.getValue().cancel(true);
			throw new RuntimeException(e);
		}
		return result;
	}
	
	@Override
	public String getServerUrl(String serverAddress) {
		return "http://" + getServerHost(serverAddress) + ":" + getHttpPort(serverAddress);
	}

	@Override
	public String getServerAddress(Member server) {
		return server.getAddress().getHost() + ":" + server.getAddress().getPort();
	}
	
	@Override
	public String getServerHost(String serverAddress) {
		return StringUtils.substringBefore(serverAddress, ":");
	}

	@Override
	public int getHttpPort(String serverAddress) {
		return httpPorts.get(serverAddress);
	}

	@Override
	public int getSshPort(String serverAddress) {
		return sshPorts.get(serverAddress);
	}
	
	public String getServerName(String serverAddress) {
		return serverNames.get(serverAddress);
	}
	
	@Override
	public String getLeaderServerAddress() {
		return getServerAddress(hazelcastInstance.getCluster().getMembers().iterator().next());
	}

	@Override
	public String getCredential() {
		return hibernateConfig.getClusterCredential();
	}

	@Override
	public List<String> getServerAddresses() {
		return getHazelcastInstance().getCluster().getMembers()
				.stream().map(this::getServerAddress).collect(toList());
	}

	@Override
	public void redistributeProjects(Map<Long, LinkedHashMap<String, ProjectReplica>> replicas) {
		if (subscriptionManager.isSubscriptionActive()) {
			var servers = new LinkedHashSet<>(getServerAddresses());
			var replicaCount = settingManager.getClusterSetting().getReplicaCount();
			// Normalize distributions
			for (var entry : replicas.values()) {
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
			for (var projectToReplicas : replicas.entrySet()) {
				var projectId = projectToReplicas.getKey();
				for (var serverToReplica : projectToReplicas.getValue().entrySet()) {
					var server = serverToReplica.getKey();
					if (servers.contains(server)) {
						var replica = serverToReplica.getValue();
						if (replica.getType() != REDUNDANT)
							serverReplicas.computeIfAbsent(server, k -> new LinkedHashMap<>()).put(projectId, replica);
					}
				}
			}
			servers.forEach(it -> serverReplicas.putIfAbsent(it, new LinkedHashMap<>()));

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
						for (var it = replicasOnMaxLoadServer.entrySet().iterator(); it.hasNext(); ) {
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

			for (var replicasOfProject : replicas.values()) {
				var primaryServerLoad = -1;
				Map.Entry<String, ProjectReplica> primaryEntry = null;
				for (var serverToReplica : replicasOfProject.entrySet()) {
					var server = serverToReplica.getKey();
					if (servers.contains(server) && serverToReplica.getValue().getType() == PRIMARY) {
						primaryEntry = serverToReplica;
						primaryServerLoad = serverLoads.get(server);
						break;
					}
				}

				var minBackupServerLoad = replicas.size();
				Map.Entry<String, ProjectReplica> minBackupEntry = null;
				for (var serverToReplica : replicasOfProject.entrySet()) {
					var server = serverToReplica.getKey();
					var replica = serverToReplica.getValue();
					if (servers.contains(server) && replica.getType() == BACKUP) {
						var serverLoad = serverLoads.get(server);
						if (serverLoad < minBackupServerLoad) {
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
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private Map<String, Integer> getServerLoads(Map<Long, LinkedHashMap<String, ProjectReplica>> replicas,
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
	public LinkedHashMap<String, ProjectReplica> addProject(Map<Long, LinkedHashMap<String, ProjectReplica>> replicas, Long projectId) {
		if (subscriptionManager.isSubscriptionActive()) {
			var servers = new HashSet<>(getServerAddresses());
			var replicaCount = settingManager.getClusterSetting().getReplicaCount();

			var serverLoads = getServerLoads(replicas, servers, EnumSet.of(PRIMARY, BACKUP));
			var sortedServers = new ArrayList<>(servers);
			sortedServers.sort(comparingInt(serverLoads::get));

			LinkedHashMap<String, ProjectReplica> replicasOfNewProject = new LinkedHashMap<>();
			for (var server : sortedServers) {
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
		} else {
			var replicasOfProject = new LinkedHashMap<String, ProjectReplica>();
			var replica = new ProjectReplica();
			replica.setType(PRIMARY);
			replica.setVersion(0);
			replicasOfProject.put(getLocalServerAddress(), replica);
			return replicasOfProject;
		}
	}

	@Override
	public boolean isClusteringSupported() {
		return subscriptionManager.isSubscriptionActive();
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ClusterManager.class);
	}
	
}
