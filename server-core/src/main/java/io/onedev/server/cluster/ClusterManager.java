package io.onedev.server.cluster;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ClusterManager {

	void start();
	
	void postStart();
	
	void stop();
	
	void preStop();
	
	Collection<String> getRunningServers();
	
	boolean isLeaderServer();

	HazelcastInstance getHazelcastInstance();
	
	void init(IAtomicLong data, Callable<Long> initializer);
	
	<T> T runOnServer(Member server, ClusterTask<T> task);
	
	<T> T runOnServer(String serverAddress, ClusterTask<T> task);
	
	<T> Map<String, T> runOnAllServers(ClusterTask<T> task);

	<T> Map<String, T> runOnServers(Collection<String> servers, ClusterTask<T> task);
	
	<T> Map<String, Future<T>> submitToAllServers(ClusterTask<T> task);

	<T> Map<String, Future<T>> submitToServers(Collection<String> servers, ClusterTask<T> task);
	
	<T> Future<T> submitToServer(String serverAddress, ClusterTask<T> task);
	
	<T> Future<T> submitToServer(Member server, ClusterTask<T> task);
	
	String getServerUrl(String serverAddress);
	
	int getHttpPort(String serverAddress);

	int getSshPort(String serverAddress);

	String getServerHost(String serverAddress);
	
	String getServerAddress(Member server);
	
	String getLeaderServerAddress();
	
	String getLocalServerAddress();
	
	String getCredential();

	@Nullable
	Member getServer(String serverAddress, boolean mustExist);

	List<String> getServerAddresses();
	
}
