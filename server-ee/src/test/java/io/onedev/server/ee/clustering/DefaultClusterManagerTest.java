package io.onedev.server.ee.clustering;

import io.onedev.server.ServerConfig;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.data.DataManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.model.support.administration.ClusterSetting;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.replica.ProjectReplica;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.onedev.server.replica.ProjectReplica.Type.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultClusterManagerTest extends TestCase {

	public void testRedistributeProjects() {
		var serverConfig = mock(ServerConfig.class);
		var listenerRegistry = mock(ListenerRegistry.class);
		var persistenceManager = mock(DataManager.class);
		var hibernateConfig = mock(HibernateConfig.class);
		var settingManager = mock(SettingManager.class);
		var subscriptionManager = mock(SubscriptionManager.class);
		
		var clusterSetting = new ClusterSetting();
		when(settingManager.getClusterSetting()).thenReturn(clusterSetting);
		when(subscriptionManager.isSubscriptionActive()).thenReturn(true);
		
		var servers = new ArrayList<String>();
		var clusterManager = new DefaultClusterManager(serverConfig, persistenceManager,  
				listenerRegistry, hibernateConfig, settingManager, subscriptionManager) {
			@Override
			public List<String> getServerAddresses() {
				return servers;
			}
		};
		
		String server0 = "server0";
		String server1 = "server1";
		String server2 = "server2";

		servers.add(server1);
		servers.add(server2);

		Map<Long, LinkedHashMap<String, ProjectReplica>> replicas = new LinkedHashMap<>();

		LinkedHashMap<String, ProjectReplica> replicasOfProject = new LinkedHashMap<>();

		var replica = new ProjectReplica();
		replica.setType(BACKUP);
		replica.setVersion(1);
		replicasOfProject.put("server0", replica);

		replica = new ProjectReplica();
		replica.setType(PRIMARY);
		replica.setVersion(1);
		replicasOfProject.put("server1", replica);

		replica = new ProjectReplica();
		replica.setType(BACKUP);
		replica.setVersion(1);
		replicasOfProject.put("server2", replica);

		replicas.put(1L, replicasOfProject);

		replicasOfProject = new LinkedHashMap<>();

		replica = new ProjectReplica();
		replica.setType(BACKUP);
		replica.setVersion(1);
		replicasOfProject.put("server1", replica);

		replica = new ProjectReplica();
		replica.setType(PRIMARY);
		replica.setVersion(1);
		replicasOfProject.put("server2", replica);

		replicas.put(2L, replicasOfProject);

		clusterSetting.setReplicaCount(1);
		clusterManager.redistributeProjects(replicas);
		assertEquals(BACKUP, replicas.get(1L).get(server0).getType());
		assertEquals(PRIMARY, replicas.get(1L).get(server1).getType());
		assertEquals(REDUNDANT, replicas.get(1L).get(server2).getType());
		assertEquals(REDUNDANT, replicas.get(2L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(2L).get(server2).getType());

		replicas = new LinkedHashMap<>();

		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(PRIMARY);
		replicasOfProject = new LinkedHashMap<>();
		replicasOfProject.put(server1, replica);
		replicas.put(1L, replicasOfProject);

		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(PRIMARY);
		replicasOfProject = new LinkedHashMap<>();
		replicasOfProject.put(server1, replica);
		replicas.put(2L, replicasOfProject);

		clusterManager.redistributeProjects(replicas);
		assertEquals(REDUNDANT, replicas.get(1L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(1L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(2L).get(server1).getType());

		clusterSetting.setReplicaCount(2);
		clusterManager.redistributeProjects(replicas);
		assertEquals(BACKUP, replicas.get(1L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(1L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(2L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(2L).get(server2).getType());

		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(PRIMARY);
		replicasOfProject = new LinkedHashMap<>();
		replicasOfProject.put(server1, replica);
		replicas.put(3L, replicasOfProject);

		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(PRIMARY);
		replicasOfProject = new LinkedHashMap<>();
		replicasOfProject.put(server2, replica);
		replicas.put(4L, replicasOfProject);

		String server3 = "server3";
		servers.add(server3);
		clusterManager.redistributeProjects(replicas);
		assertEquals(PRIMARY, replicas.get(1L).get(server1).getType());
		assertEquals(REDUNDANT, replicas.get(1L).get(server2).getType());
		assertEquals(BACKUP, replicas.get(1L).get(server3).getType());
		assertEquals(REDUNDANT, replicas.get(2L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(2L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(2L).get(server3).getType());
		assertEquals(PRIMARY, replicas.get(3L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(3L).get(server2).getType());
		assertEquals(BACKUP, replicas.get(4L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(4L).get(server2).getType());

		replicasOfProject = new LinkedHashMap<>();
		
		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(BACKUP);
		replicasOfProject.put(server0, replica);
		
		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(PRIMARY);
		replicasOfProject.put(server1, replica);
		
		replicas.put(5L, replicasOfProject);

		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(PRIMARY);
		replicasOfProject = new LinkedHashMap<>();
		replicasOfProject.put(server1, replica);
		replicas.put(6L, replicasOfProject);
		clusterManager.redistributeProjects(replicas);
		assertEquals(PRIMARY, replicas.get(1L).get(server1).getType());
		assertEquals(REDUNDANT, replicas.get(1L).get(server2).getType());
		assertEquals(BACKUP, replicas.get(1L).get(server3).getType());
		assertEquals(REDUNDANT, replicas.get(2L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(2L).get(server2).getType());
		assertEquals(BACKUP, replicas.get(2L).get(server3).getType());
		assertEquals(REDUNDANT, replicas.get(3L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(3L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(3L).get(server3).getType());
		assertEquals(BACKUP, replicas.get(4L).get(server1).getType());
		assertEquals(REDUNDANT, replicas.get(4L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(4L).get(server3).getType());
		assertEquals(BACKUP, replicas.get(5L).get(server0).getType());
		assertEquals(BACKUP, replicas.get(5L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(5L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(6L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(6L).get(server2).getType());

		String server4 = "server4";
		servers.add(server4);

		clusterSetting.setReplicaCount(3);
		clusterManager.redistributeProjects(replicas);
		assertEquals(BACKUP, replicas.get(1L).get(server1).getType());
		assertEquals(REDUNDANT, replicas.get(1L).get(server2).getType());
		assertEquals(BACKUP, replicas.get(1L).get(server3).getType());
		assertEquals(PRIMARY, replicas.get(1L).get(server4).getType());
		assertEquals(REDUNDANT, replicas.get(2L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(2L).get(server2).getType());
		assertEquals(BACKUP, replicas.get(2L).get(server3).getType());
		assertEquals(BACKUP, replicas.get(2L).get(server4).getType());
		assertEquals(REDUNDANT, replicas.get(3L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(3L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(3L).get(server3).getType());
		assertEquals(BACKUP, replicas.get(3L).get(server4).getType());
		assertEquals(BACKUP, replicas.get(4L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(4L).get(server2).getType());
		assertEquals(REDUNDANT, replicas.get(4L).get(server3).getType());
		assertEquals(PRIMARY, replicas.get(4L).get(server4).getType());
		assertEquals(BACKUP, replicas.get(5L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(5L).get(server2).getType());
		assertEquals(BACKUP, replicas.get(5L).get(server3).getType());
		assertEquals(PRIMARY, replicas.get(6L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(6L).get(server2).getType());
		assertEquals(BACKUP, replicas.get(6L).get(server3).getType());

		servers.remove(server1);
		servers.remove(server2);
		clusterManager.redistributeProjects(replicas);
		assertEquals(BACKUP, replicas.get(1L).get(server3).getType());
		assertEquals(PRIMARY, replicas.get(1L).get(server4).getType());
		assertEquals(BACKUP, replicas.get(2L).get(server3).getType());
		assertEquals(PRIMARY, replicas.get(2L).get(server4).getType());
		assertEquals(PRIMARY, replicas.get(3L).get(server3).getType());
		assertEquals(BACKUP, replicas.get(3L).get(server4).getType());
		assertEquals(BACKUP, replicas.get(4L).get(server3).getType());
		assertEquals(PRIMARY, replicas.get(4L).get(server4).getType());
		assertEquals(PRIMARY, replicas.get(5L).get(server3).getType());
		assertEquals(BACKUP, replicas.get(5L).get(server4).getType());
		assertEquals(PRIMARY, replicas.get(6L).get(server3).getType());
		assertEquals(BACKUP, replicas.get(6L).get(server4).getType());
	}
}