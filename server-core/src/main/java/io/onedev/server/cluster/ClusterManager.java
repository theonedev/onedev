package io.onedev.server.cluster;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;

public interface ClusterManager {

	void start();
	
	void stop();
	
	boolean isLeaderServer();

	HazelcastInstance getHazelcastInstance();
	
	void init(IAtomicLong data, Callable<Long> initializer);
	
	<T> T runOnServer(Member server, ClusterTask<T> task);
	
	<T> T runOnServer(UUID serverUUID, ClusterTask<T> task);
	
	<T> Map<UUID, T> runOnAllServers(ClusterTask<T> task);
	
	<T> Map<UUID, Future<T>> submitToAllServers(ClusterTask<T> task);
	
	<T> Future<T> submitToServer(UUID serverUUID, ClusterTask<T> task);
	
	<T> Future<T> submitToServer(Member server, ClusterTask<T> task);
	
	String getServerUrl(UUID serverUUID);
	
	String getServerAddress(UUID serverUUID);
	
	UUID getLeaderServerUUID();
	
	UUID getLocalServerUUID();
	
	String getCredentialValue();

	@Nullable
	Member getServer(UUID serverUUID, boolean mustExist);
	
}
