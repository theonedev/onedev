package io.onedev.server.cluster;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jspecify.annotations.Nullable;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;

import io.onedev.server.annotation.NoDBAccess;
import io.onedev.server.replica.ProjectReplica;

public interface ClusterService {

	void start();
	
	void postStart();

	void preStop();
	
	void stop();
	
	Collection<String> getOnlineServers();
	
	boolean isLeaderServer();

	@Nullable
	HazelcastInstance getHazelcastInstance();
	
    default ClusterAtomicLong getAtomicLong(String name) {
        return new ClusterAtomicLong(getHazelcastInstance(), name);
    }

    default ClusterLock getLock(String name) {
        return new ClusterLock(getHazelcastInstance(), name);
    }

	void initWithLead(ClusterAtomicLong data, Callable<Long> initializer);
	
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
	
	String getServerName(String serverAddress);
	
	String getServerAddress(Member server);
	
	String getLeaderServerAddress();

	@NoDBAccess
	String getLocalServerAddress();

	@NoDBAccess
	String getCredential();

	@Nullable
	Member getServer(String serverAddress, boolean mustExist);

	List<String> getServerAddresses();
	
	void redistributeProjects(Map<Long, LinkedHashMap<String, ProjectReplica>> replicas);

	LinkedHashMap<String, ProjectReplica> addProject(Map<Long, LinkedHashMap<String, ProjectReplica>> replicas, Long projectId);
	
	boolean isClusteringSupported();
}
