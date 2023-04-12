package io.onedev.server.cluster;

import com.google.common.hash.Hashing;
import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.cp.IAtomicLong;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.ServerConfig;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.cluster.ConnectionLost;
import io.onedev.server.event.cluster.ConnectionRestored;
import io.onedev.server.model.ClusterServer;
import io.onedev.server.persistence.DataManager;
import io.onedev.server.persistence.HibernateConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static io.onedev.server.model.AbstractEntity.PROP_ID;
import static io.onedev.server.model.ClusterServer.PROP_ADDRESS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.max;
import static java.util.stream.Collectors.toList;

@Singleton
public class DefaultClusterManager implements ClusterManager {
	
	private static final String EXECUTOR_SERVICE_NAME = "default";
	
	private final ServerConfig serverConfig;
	
	private final DataManager dataManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private volatile Map<String, Integer> httpPorts;
	
	private volatile Map<String, Integer> sshPorts;
	
	private volatile Map<String, String> runningServers;
	
	private volatile HazelcastInstance hazelcastInstance;
	
	private final String credential;
	
	@Inject
	public DefaultClusterManager(ServerConfig serverConfig, DataManager dataManager, 
								 ListenerRegistry listenerRegistry, HibernateConfig hibernateConfig) { 
		this.serverConfig = serverConfig;
		this.dataManager = dataManager;
		this.listenerRegistry = listenerRegistry;
		var dbPassword = hibernateConfig.getPassword();
		if (dbPassword == null)
			dbPassword = "";
		credential = Hashing.sha256().hashString(dbPassword, UTF_8).toString();
	}
	
	@Override
	public String getLocalServerAddress() {
		return serverConfig.getClusterIp() + ":" +  serverConfig.getClusterPort();
	}
	
	private void saveServer(Connection conn, Long id, String server) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(String.format(
					"insert into %s values(%d, '%s')",
					dataManager.getTableName(ClusterServer.class), id, server));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void start() {
		var localServer = getLocalServerAddress();
		var servers = dataManager.callWithConnection(conn -> {
			boolean hasLock;
			try (Statement stmt = conn.createStatement()) {
				String query = String.format("select * from %s where %s=0 for update",
						dataManager.getTableName(ClusterServer.class),
						dataManager.getColumnName(PROP_ID));
				hasLock = stmt.executeQuery(query).next();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			if (!hasLock)
				saveServer(conn, 0L, "lock");

			var theServers = new HashMap<Long, String>();
			try (Statement stmt = conn.createStatement()) {
				String query = String.format("select %s, %s from %s where %s!=0",
						dataManager.getColumnName(PROP_ID),
						dataManager.getColumnName(PROP_ADDRESS),
						dataManager.getTableName(ClusterServer.class),
						dataManager.getColumnName(PROP_ID));
				try (ResultSet resultset = stmt.executeQuery(query)) {
					while (resultset.next()) 
						theServers.put(resultset.getLong(1), resultset.getString(2));
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}

			if (!theServers.containsValue(localServer)) {
				Long nextId = theServers.isEmpty() ? 1L : max(theServers.keySet()) + 1L;
				saveServer(conn, nextId, localServer);
				theServers.put(nextId, localServer);
			}
			return theServers;
		});

		Config config = new Config();
		config.setClusterName(credential);
		config.setInstanceName(localServer);
		config.setProperty("hazelcast.shutdownhook.enabled", "false");
		config.getExecutorConfig(EXECUTOR_SERVICE_NAME).setPoolSize(Integer.MAX_VALUE);
		config.getMapConfig("default").setStatisticsEnabled(false);
		config.getNetworkConfig().setPort(serverConfig.getClusterPort()).setPortAutoIncrement(false);
		config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);

		for (String server: servers.values())
			config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(server);

		hazelcastInstance = Hazelcast.newHazelcastInstance(config);
		hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
				var server = getServerAddress(membershipEvent.getMember());
				if (runningServers.containsKey(server)) 
					listenerRegistry.post(new ConnectionRestored(server));
			}

			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				var server = getServerAddress(membershipEvent.getMember());
				if (runningServers.containsKey(server)) 
					listenerRegistry.post(new ConnectionLost(server));
			}
		});
		
		httpPorts = hazelcastInstance.getMap("httpPorts");
		sshPorts = hazelcastInstance.getMap("sshPorts");
		runningServers = hazelcastInstance.getReplicatedMap("runningServers");
		
		httpPorts.put(getLocalServerAddress(), serverConfig.getHttpPort());
		sshPorts.put(getLocalServerAddress(), serverConfig.getSshPort());
	}

	@Override
	public void stop() {
		var localServer = getLocalServerAddress();
		if (httpPorts != null)
			httpPorts.put(localServer, serverConfig.getHttpPort());
		if (sshPorts != null)
			sshPorts.put(localServer, serverConfig.getSshPort());
		if (hazelcastInstance != null) {
			hazelcastInstance.shutdown();
			hazelcastInstance = null;
		}

		dataManager.callWithConnection(conn -> {
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate(String.format(
						"delete from %s where %s='%s'",
						dataManager.getTableName(ClusterServer.class),
						dataManager.getColumnName(PROP_ADDRESS),
						localServer));
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			return null;
		});
	}

	@Override
	public void postStart() {
		var localServer = getLocalServerAddress();
		runningServers.put(localServer, localServer);
	}

	@Override
	public void preStop() {
		if (runningServers != null)
			runningServers.remove(getLocalServerAddress());
	}
	
	@Override
	public Collection<String> getRunningServers() {
		return runningServers.keySet();
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
		if (mustExist)
			throw new ExplicitException("Server not found: " + serverAddress);
		else
			return null;
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
		for (var entry: getExecutorService().submitToAllMembers(task).entrySet())
			futures.put(getServerAddress(entry.getKey()), entry.getValue());
		return futures;
	}

	@Override
	public <T> Map<String, Future<T>> submitToServers(Collection<String> serverAddresses, ClusterTask<T> task) {
		Map<String, Future<T>> futures = new HashMap<>();
		var servers = hazelcastInstance.getCluster().getMembers().stream().filter(it -> serverAddresses.contains(getServerAddress(it))).collect(toList());
		if (!servers.isEmpty()) {
			for (var entry : getExecutorService().submitToMembers(task, servers).entrySet())
				futures.put(getServerAddress(entry.getKey()), entry.getValue());
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
	
	@Override
	public String getLeaderServerAddress() {
		return getServerAddress(hazelcastInstance.getCluster().getMembers().iterator().next());
	}

	@Override
	public String getCredential() {
		return credential;
	}

	@Override
	public List<String> getServerAddresses() {
		return getHazelcastInstance().getCluster().getMembers()
				.stream().map(this::getServerAddress).collect(toList());
	}
	
}
