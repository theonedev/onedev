package io.onedev.server.replica;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static io.onedev.server.replica.ProjectReplica.Type.*;
import static org.junit.Assert.assertEquals;

public class ReplicatUtilsTest {

	@Test
	public void redistributeProjects() {
		String server1 = "server1";
		String server2 = "server2";

		Set<String> servers = new LinkedHashSet<>();
		servers.add(server1);
		servers.add(server2);

		Map<Long, Map<String, ProjectReplica>> replicas = new LinkedHashMap<>();
		
		Map<String, ProjectReplica> replicasOfProject = new LinkedHashMap<>();
		
		ProjectReplica replica = new ProjectReplica();
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

		ReplicaUtils.redistributeProjects(replicas, servers, 1);
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

		ReplicaUtils.redistributeProjects(replicas, servers, 1);
		assertEquals(REDUNDANT, replicas.get(1L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(1L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(2L).get(server1).getType());

		ReplicaUtils.redistributeProjects(replicas, servers, 2);
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
		ReplicaUtils.redistributeProjects(replicas, servers, 2);
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

		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(PRIMARY);
		replicasOfProject = new LinkedHashMap<>();
		replicasOfProject.put(server1, replica);
		replicas.put(5L, replicasOfProject);

		replica = new ProjectReplica();
		replica.setVersion(1);
		replica.setType(PRIMARY);
		replicasOfProject = new LinkedHashMap<>();
		replicasOfProject.put(server1, replica);
		replicas.put(6L, replicasOfProject);
		ReplicaUtils.redistributeProjects(replicas, servers, 2);
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
		assertEquals(BACKUP, replicas.get(5L).get(server1).getType());
		assertEquals(PRIMARY, replicas.get(5L).get(server2).getType());
		assertEquals(PRIMARY, replicas.get(6L).get(server1).getType());
		assertEquals(BACKUP, replicas.get(6L).get(server2).getType());

		String server4 = "server4";
		servers.add(server4);

		ReplicaUtils.redistributeProjects(replicas, servers, 3);
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
		ReplicaUtils.redistributeProjects(replicas, servers, 3);
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