package io.onedev.server.cluster;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.cp.IAtomicLong;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.ServerConfig;
import io.onedev.server.exception.SystemNotReadyException;
import io.onedev.server.model.ClusterCredential;
import io.onedev.server.model.ClusterServer;
import io.onedev.server.persistence.ConnectionCallable;
import io.onedev.server.persistence.DataManager;

@Singleton
public class DefaultClusterManager implements ClusterManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultClusterManager.class);
	
	private static final String EXECUTOR_SERVICE_NAME = "default";
	
	private final ServerConfig serverConfig;
	
	private final DataManager dataManager;
	
	private volatile Map<UUID, Integer> httpPorts;
	
	private volatile HazelcastInstance hazelcastInstance;
	
	private volatile String credentialValue;
	
	@Inject
	public DefaultClusterManager(ServerConfig serverConfig, DataManager dataManager) { 
		this.serverConfig = serverConfig;
		this.dataManager = dataManager;
	}
	
	@Nullable
	private String loadCredential(Connection conn) {
		try (Statement stmt = conn.createStatement()) {
			String query = String.format(
					"select %s from %s", 
					dataManager.getColumnName(ClusterCredential.PROP_VALUE), 
					dataManager.getTableName(ClusterCredential.class)); 
			try (ResultSet resultset = stmt.executeQuery(query)) {
				if (!resultset.next())
					return null;
				else
					return resultset.getString(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void saveCredential(Connection conn, String credential) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(String.format(
					"insert into %s values(1, '%s')", 
					dataManager.getTableName(ClusterCredential.class), credential));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Map<Long, String> loadServers(Connection conn) {
		Map<Long, String> servers = new HashMap<>();
		try (Statement stmt = conn.createStatement()) {
			String query = String.format(
					"select %s, %s from %s", 
					dataManager.getColumnName(ClusterServer.PROP_ID), 
					dataManager.getColumnName(ClusterServer.PROP_ADDRESS), 
					dataManager.getTableName(ClusterServer.class)); 
			try (ResultSet resultset = stmt.executeQuery(query)) {
				while (resultset.next()) {
					servers.put(resultset.getLong(1), resultset.getString(2));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return servers;
	}
	
	private void saveServer(Connection conn, Long id, String address) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(String.format(
					"insert into %s values(%d, '%s')", 
					dataManager.getTableName(ClusterServer.class), id, address));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getLocalAddress() {
		return serverConfig.getClusterIp() + ":" +  serverConfig.getClusterPort();
	}
	
	@Override
	public void start() {
		dataManager.callWithConnection(new ConnectionCallable<Void>() {

			@Override
			public Void call(Connection conn) {
				credentialValue = loadCredential(conn);
				while (credentialValue == null) {
					credentialValue = UUID.randomUUID().toString();
					try {
						saveCredential(conn, credentialValue);
					} catch (Exception e) {
						logger.warn("Error generating cluster credential, will retry later...", e);
						try {
							Thread.sleep((RandomUtils.nextInt(5)+1)*1000L);
						} catch (InterruptedException e2) {
						}
						credentialValue = loadCredential(conn);
					}
				}

				String localAddress = getLocalAddress();
				Map<Long, String> servers;
				while (true) {
					servers = loadServers(conn);
					if (!servers.containsValue(localAddress)) {
						Long nextId = servers.isEmpty()? 1L: Collections.max(servers.keySet()) + 1L;
						try {
							saveServer(conn, nextId, localAddress);
							servers.put(nextId, localAddress);
							break;
						} catch (Exception e) {
							logger.warn("Error adding cluster server, will retry later...", e);
							try {
								Thread.sleep((RandomUtils.nextInt(5)+1)*1000L);
							} catch (InterruptedException e2) {
							}
						}
					} else {
						break;
					}
				}
				
				Config config = new Config();
				config.setClusterName(credentialValue);
				config.setInstanceName(localAddress);
				config.setProperty("hazelcast.shutdownhook.enabled", "false");
				config.getExecutorConfig(EXECUTOR_SERVICE_NAME).setPoolSize(Integer.MAX_VALUE);
				config.getNetworkConfig().setPort(serverConfig.getClusterPort()).setPortAutoIncrement(false);
				config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
				
				for (String server: servers.values()) 
					config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(server);

				hazelcastInstance = Hazelcast.newHazelcastInstance(config);
				return null;
			}
			
		});
		
		httpPorts = hazelcastInstance.getReplicatedMap("httpPorts");
		hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {

			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
			}

			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				if (isLeaderServer()) 
					httpPorts.remove(membershipEvent.getMember().getUuid());
			}
			
		});

		httpPorts.put(getLocalServerUUID(), serverConfig.getHttpPort());
	}

	@Override
	public void stop() {
		if (hazelcastInstance != null) {
			hazelcastInstance.shutdown();
			hazelcastInstance = null;
			
			dataManager.callWithConnection(new ConnectionCallable<Void>() {

				@Override
				public Void call(Connection conn) {
					try (Statement stmt = conn.createStatement()) {
						stmt.executeUpdate(String.format(
								"delete from %s where %s='%s'", 
								dataManager.getTableName(ClusterServer.class), 
								dataManager.getColumnName(ClusterServer.PROP_ADDRESS), 
								getLocalAddress()));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
					return null;
				}
				
			});
		}
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
		}
	}
	
	@Override
	public Member getServer(UUID serverUUID, boolean mustExist) {
		Optional<Member> member = hazelcastInstance.getCluster().getMembers().stream()
				.filter(it->it.getUuid().equals(serverUUID)).findFirst();
		if (mustExist)
			return member.get();
		else
			return member.orElse(null);
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
	public <T> T runOnServer(UUID serverUUID, ClusterTask<T> task) {
		return getResult(submitToServer(serverUUID, task));
	}
	
	@Override
	public <T> T runOnServer(Member server, ClusterTask<T> task) {
		return getResult(submitToServer(server, task));
	}
	
	private IExecutorService getExecutorService() {
		return hazelcastInstance.getExecutorService(EXECUTOR_SERVICE_NAME);
	}
	
	@Override
	public <T> Future<T> submitToServer(UUID serverUUID, ClusterTask<T> task) {
		return getExecutorService().submitToMember(task, getServer(serverUUID, true));
	}
	
	@Override
	public <T> Future<T> submitToServer(Member server, ClusterTask<T> task) {
		return getExecutorService().submitToMember(task, server);
	}
	
	@Override
	public <T> Map<UUID, Future<T>> submitToAllServers(ClusterTask<T> task) {
		Map<UUID, Future<T>> futures = new HashMap<>();
		for (var entry: getExecutorService().submitToAllMembers(task).entrySet()) 
			futures.put(entry.getKey().getUuid(), entry.getValue());
		return futures;
	}

	@Override
	public <T> Map<UUID, T> runOnAllServers(ClusterTask<T> task) {
		Map<UUID, T> result = new HashMap<>();

		Map<UUID, Future<T>> futures = submitToAllServers(task);
		try {
			for (var entry: futures.entrySet()) 
				result.put(entry.getKey(), entry.getValue().get());
		} catch (InterruptedException e) {
			for (var entry: futures.entrySet()) 
				entry.getValue().cancel(true);
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			for (var entry: futures.entrySet()) 
				entry.getValue().cancel(true);
			throw new RuntimeException(e);
		}
		return result;
	}
	
	@Override
	public String getServerUrl(UUID serverUUID) {
		return "http://" + getServerAddress(serverUUID) + ":" + httpPorts.get(serverUUID);
	}

	@Override
	public String getServerAddress(UUID serverUUID) {
		Member server = getServer(serverUUID, true);
		try {
			return server.getAddress().getInetAddress().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public UUID getLeaderServerUUID() {
		return hazelcastInstance.getCluster().getMembers().iterator().next().getUuid();
	}

	@Override
	public UUID getLocalServerUUID() {
		return hazelcastInstance.getCluster().getLocalMember().getUuid();
	}

	@Override
	public String getCredentialValue() {
		if (credentialValue == null)
			throw new SystemNotReadyException();
		return credentialValue;
	}

}
