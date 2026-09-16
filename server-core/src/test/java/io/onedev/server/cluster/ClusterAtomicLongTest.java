package io.onedev.server.cluster;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import org.junit.jupiter.api.Test;

public class ClusterAtomicLongTest {
    @Test
    public void countersAreAtomicAcrossMembersAndSurvivePartitionOwnerLoss() throws Exception {
        String clusterName = "counter-test-" + UUID.randomUUID();
        var first = Hazelcast.newHazelcastInstance(config(clusterName));
        var secondConfig = config(clusterName);
        secondConfig.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true)
                .addMember("127.0.0.1:" + first.getCluster().getLocalMember().getAddress().getPort());
        var second = Hazelcast.newHazelcastInstance(secondConfig);
        var executor = Executors.newFixedThreadPool(4);
        try {
            assertEquals(2, first.getCluster().getMembers().size());
            assertEquals(2, second.getCluster().getMembers().size());
            var firstCounter = new ClusterAtomicLong(first, "ids");
            var secondCounter = new ClusterAtomicLong(second, "ids");
            assertEquals(0, firstCounter.get());
            assertTrue(firstCounter.compareAndSet(0, 1));
            assertFalse(secondCounter.compareAndSet(0, 99));
            assertEquals(1, secondCounter.get());
            var jobs = new ArrayList<Callable<Set<Long>>>();
            for (int thread = 0; thread < 4; thread++) {
                var counter = thread % 2 == 0 ? firstCounter : secondCounter;
                jobs.add(() -> {
                    var ids = new HashSet<Long>();
                    for (int i = 0; i < 100; i++)
                        assertTrue(ids.add(counter.getAndIncrement()));
                    return ids;
                });
            }
            var ids = new HashSet<Long>();
            for (var future : executor.invokeAll(jobs)) {
                var allocated = future.get(30, TimeUnit.SECONDS);
                assertTrue(java.util.Collections.disjoint(ids, allocated));
                ids.addAll(allocated);
            }
            assertEquals(400, ids.size());
            assertEquals(401, firstCounter.get());
            firstCounter.ensureAtLeast(100_000);
            secondCounter.ensureAtLeast(1);
            assertEquals(100_000, secondCounter.getAndIncrement());

            var owner = first.getPartitionService().getPartition("ids").getOwner();
            var failed = owner.getUuid().equals(first.getCluster().getLocalMember().getUuid()) ? first : second;
            var survivor = failed == first ? second : first;
            failed.getLifecycleService().terminate();
            var survivingCounter = new ClusterAtomicLong(survivor, "ids");
            assertEquals(100_001, executor.submit(survivingCounter::getAndIncrement).get(30, TimeUnit.SECONDS).longValue());
            assertEquals(100_002, survivingCounter.get());
        } finally {
            executor.shutdownNow();
            first.shutdown();
            second.shutdown();
        }
    }

    private static Config config(String name) throws java.io.IOException {
        var config = new Config().setClusterName(name);
        config.setProperty("hazelcast.logging.type", "none");
        config.setProperty("hazelcast.operation.thread.count", "2");
        config.setProperty("hazelcast.operation.generic.thread.count", "2");
        config.setProperty("hazelcast.partition.count", "17");
        try (var socket = new java.net.ServerSocket(0, 0, java.net.InetAddress.getLoopbackAddress())) {
            config.getNetworkConfig().setPort(socket.getLocalPort()).setPortAutoIncrement(true);
        }
        config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface("127.0.0.1");
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getMapConfig("default").setBackupCount(1);
        return config;
    }
}
