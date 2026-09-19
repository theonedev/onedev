package io.onedev.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.replicatedmap.ReplicatedMap;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentLastUsedDate;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.service.AgentService;

class DefaultResourceServiceTest {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ThreadLocal<String> executingServer = new ThreadLocal<>();

    private final AtomicReference<String> leader = new AtomicReference<>("leader");

    private final Map<Future<?>, AtomicReference<Thread>> taskThreads = new ConcurrentHashMap<>();

    private final Map<Thread, AtomicInteger> candidateQueries = new ConcurrentHashMap<>();

    private final ClusterService clusterService = mock(ClusterService.class);

    private final AgentService agentService = mock(AgentService.class);

    private DefaultResourceService resourceService;

    @BeforeEach
    void setup() throws Exception {
        when(clusterService.getLeaderServerAddress()).thenAnswer(it -> leader.get());
        when(clusterService.isLeaderServer()).thenAnswer(it -> leader.get().equals(executingServer.get()));
        when(clusterService.getLocalServerAddress()).thenReturn("leader");
        when(clusterService.getServerAddresses()).thenReturn(List.of("leader", "worker", "other-worker"));
        when(clusterService.getOnlineServers()).thenAnswer(it -> {
            assertEquals(leader.get(), executingServer.get(), "Selection must run on the leader");
            candidateQueries.computeIfAbsent(Thread.currentThread(), key -> new AtomicInteger()).incrementAndGet();
            // The online-server registry may still contain a departed Hazelcast member.
            return List.of("worker", "other-worker", "offline");
        });
        when(clusterService.submitToServer(anyString(), any())).thenAnswer(it -> {
            String server = it.getArgument(0);
            ClusterTask<?> task = it.getArgument(1);
            var thread = new AtomicReference<Thread>();
            var future = executor.submit(() -> {
                thread.set(Thread.currentThread());
                executingServer.set(server);
                try {
                    return task.call();
                } finally {
                    executingServer.remove();
                }
            });
            taskThreads.put(future, thread);
            return future;
        });
        when(clusterService.runOnServer(anyString(), any())).thenAnswer(it -> {
            String server = it.getArgument(0);
            ClusterTask<?> task = it.getArgument(1);
            Future<?> future = clusterService.submitToServer(server, task);
            try {
                return future.get();
            } catch (InterruptedException e) {
                // Match ClusterService's interruptible future wrapper.
                future.cancel(true);
                throw new RuntimeException(e);
            }
        });
        var hazelcast = mock(HazelcastInstance.class);
        @SuppressWarnings("unchecked")
        ReplicatedMap<String, Integer> cpuCounts = mock(ReplicatedMap.class);
        when(clusterService.getHazelcastInstance()).thenReturn(hazelcast);
        when(hazelcast.<String, Integer>getReplicatedMap("cpuCounts")).thenReturn(cpuCounts);
        when(cpuCounts.get(any())).thenReturn(2);
        resourceService = newResourceService();
    }

    private DefaultResourceService newResourceService() {
        var service = new DefaultResourceService(agentService, mock(TransactionService.class), clusterService);
        service.on(new SystemStarting());
        return service;
    }

    private void awaitWaiting(Future<?> future) throws Exception {
        awaitWaiting(future, 1);
    }

    private void awaitWaiting(Future<?> future, int minimumQueries) throws Exception {
        var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (future.isDone())
                fail("Expected a pending allocation, got: " + future.get());
            var thread = taskThreads.get(future).get();
            if (thread != null && thread.getState() == Thread.State.TIMED_WAITING
                    && candidateQueries.get(thread).get() >= minimumQueries)
                return;
            Thread.sleep(5);
        }
        fail("Allocation did not start waiting");
    }

    @AfterEach
    void teardown() throws Exception {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void reservesCapacityAtomicallyForConcurrentSubmissions() throws Exception {
        var running = new AtomicInteger();
        var peak = new AtomicInteger();
        var started = new CountDownLatch(1);
        var finish = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            futures.add(resourceService.submitServerTask("worker", "executor", 3, 2, () -> {
                peak.accumulateAndGet(running.incrementAndGet(), Math::max);
                try {
                    started.countDown();
                    assertTrue(finish.await(5, TimeUnit.SECONDS));
                    return executingServer.get();
                } finally {
                    running.decrementAndGet();
                }
            }));
        }
        assertTrue(started.await(5, TimeUnit.SECONDS));
        assertThrows(TimeoutException.class, () -> futures.get(0).get(100, TimeUnit.MILLISECONDS));
        finish.countDown();
        for (var future : futures)
            assertEquals("worker", future.get(15, TimeUnit.SECONDS));
        assertEquals(1, peak.get());
    }

    @Test
    void usesCpuCapacityAndKeepsResourcesAndNodesIndependent() throws Exception {
        var started = new CountDownLatch(1);
        var finish = new CountDownLatch(1);
        var first = resourceService.submitServerTask("worker", "executor", 0, 2, () -> {
            started.countDown();
            finish.await();
            return "first";
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        var queued = resourceService.submitServerTask("worker", "executor", 0, 1, () -> "queued");
        assertThrows(TimeoutException.class, () -> queued.get(100, TimeUnit.MILLISECONDS));
        assertEquals("other-worker", resourceService.submitServerTask(null, "executor", 0, 2,
                executingServer::get).get(5, TimeUnit.SECONDS));
        assertEquals("independent", resourceService.submitServerTask("worker", "another-executor", 0, 2,
                () -> "independent").get(5, TimeUnit.SECONDS));
        finish.countDown();
        assertEquals("first", first.get(5, TimeUnit.SECONDS));
        assertEquals("queued", queued.get(5, TimeUnit.SECONDS));
    }

    @Test
    void grantsCapacityInArrivalOrderAcrossTimedWakeups() throws Exception {
        var started = new CountDownLatch(1);
        var finish = new CountDownLatch(1);
        var first = resourceService.submitServerTask("worker", "executor", 1, 1, () -> {
            started.countDown();
            finish.await();
            return "first";
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        List<Integer> order = Collections.synchronizedList(new ArrayList<>());
        List<Future<Integer>> queued = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int index = i;
            var future = resourceService.submitServerTask("worker", "executor", 1, 1, () -> {
                order.add(index);
                return index;
            });
            awaitWaiting(future);
            queued.add(future);
        }
        // Periodic discovery must not move the oldest request to the back of the queue.
        awaitWaiting(queued.get(0), 2);
        finish.countDown();
        first.get(5, TimeUnit.SECONDS);
        for (var future : queued)
            future.get(5, TimeUnit.SECONDS);
        assertEquals(List.of(0, 1, 2, 3), order);
    }

    @Test
    void olderLargeRequestPreventsSmallerRequestsFromTakingSharedCapacity() throws Exception {
        var started = new CountDownLatch(1);
        var finish = new CountDownLatch(1);
        var first = resourceService.submitServerTask("worker", "executor", 3, 1, () -> {
            started.countDown();
            finish.await();
            return "first";
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        List<String> order = Collections.synchronizedList(new ArrayList<>());
        var older = resourceService.submitServerTask("worker", "executor", 3, 3, () -> {
            order.add("older");
            return "older";
        });
        awaitWaiting(older);
        var younger = resourceService.submitServerTask("worker", "executor", 3, 1, () -> {
            order.add("younger");
            return "younger";
        });
        awaitWaiting(younger);
        assertEquals("other-worker", resourceService.submitServerTask(null, "executor", 3, 1,
                executingServer::get).get(5, TimeUnit.SECONDS));
        assertEquals("unrelated", resourceService.submitServerTask("worker", "another-executor", 3, 1,
                () -> "unrelated").get(5, TimeUnit.SECONDS));
        finish.countDown();
        first.get(5, TimeUnit.SECONDS);
        older.get(5, TimeUnit.SECONDS);
        younger.get(5, TimeUnit.SECONDS);
        assertEquals(List.of("older", "younger"), order);
    }

    @Test
    void cancellingQueueHeadLetsSmallerRequestUseAlreadyAvailableCapacity() throws Exception {
        var started = new CountDownLatch(1);
        var finish = new CountDownLatch(1);
        var first = resourceService.submitServerTask("worker", "executor", 2, 1, () -> {
            started.countDown();
            finish.await();
            return "first";
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        var older = resourceService.submitServerTask("worker", "executor", 2, 2,
                () -> fail("Cancelled request ran"));
        awaitWaiting(older);
        var younger = resourceService.submitServerTask("worker", "executor", 2, 1, () -> "younger");
        awaitWaiting(younger);
        assertTrue(older.cancel(true));
        assertEquals("younger", younger.get(5, TimeUnit.SECONDS));
        assertFalse(first.isDone());
        finish.countDown();
        first.get(5, TimeUnit.SECONDS);
    }

    @Test
    void unavailableAndUndersizedNodesDoNotBlockOtherRequests() throws Exception {
        var offline = resourceService.submitServerTask("offline", "executor", 1, 1,
                () -> fail("Offline node selected"));
        awaitWaiting(offline);
        var tooLarge = resourceService.submitServerTask("worker", "executor", 1, 2,
                () -> fail("Capacity exceeded"));
        awaitWaiting(tooLarge);
        assertEquals("next", resourceService.submitServerTask("worker", "executor", 1, 1,
                () -> "next").get(5, TimeUnit.SECONDS));
        offline.cancel(true);
        tooLarge.cancel(true);
    }

    @Test
    void releasesCapacityAfterTaskAndSubmissionFailures() throws Exception {
        var failed = resourceService.submitServerTask("worker", "executor", 1, 1, () -> {
            throw new IllegalStateException("Task failed");
        });
        assertThrows(ExecutionException.class, () -> failed.get(5, TimeUnit.SECONDS));
        when(clusterService.runOnServer(eq("worker"), any())).thenThrow(new IllegalStateException("Node left"));
        var rejected = resourceService.submitServerTask("worker", "executor", 1, 1, () -> "rejected");
        assertThrows(ExecutionException.class, () -> rejected.get(5, TimeUnit.SECONDS));
        when(clusterService.runOnServer(eq("worker"), any())).thenReturn("recovered");
        assertEquals("recovered", resourceService.submitServerTask("worker", "executor", 1, 1,
                () -> "recovered").get(5, TimeUnit.SECONDS));
    }

    @Test
    void cancellationInterruptsRunningTaskAndReleasesCapacity() throws Exception {
        var started = new CountDownLatch(1);
        var interrupted = new CountDownLatch(1);
        var first = resourceService.submitServerTask("worker", "executor", 1, 1, () -> {
            started.countDown();
            try {
                new CountDownLatch(1).await();
                return "unexpected";
            } finally {
                interrupted.countDown();
            }
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        assertTrue(first.cancel(true));
        assertTrue(interrupted.await(5, TimeUnit.SECONDS));
        assertEquals("next", resourceService.submitServerTask("worker", "executor", 1, 1,
                () -> "next").get(5, TimeUnit.SECONDS));
    }

    @Test
    void cancellationWhileWaitingDoesNotDispatchOrLeakCapacity() throws Exception {
        var started = new CountDownLatch(1);
        var finish = new CountDownLatch(1);
        var first = resourceService.submitServerTask("worker", "executor", 1, 1, () -> {
            started.countDown();
            finish.await();
            return "first";
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        var queued = resourceService.submitServerTask("worker", "executor", 1, 1, () -> fail("Cancelled task ran"));
        assertThrows(TimeoutException.class, () -> queued.get(100, TimeUnit.MILLISECONDS));
        assertTrue(queued.cancel(true));
        finish.countDown();
        first.get(5, TimeUnit.SECONDS);
        assertEquals("next", resourceService.submitServerTask("worker", "executor", 1, 1,
                () -> "next").get(5, TimeUnit.SECONDS));
        verify(clusterService, times(2)).runOnServer(eq("worker"), any());
    }

    @Test
    void formerLeaderStopsAllocatingWaitingTasks() throws Exception {
        doReturn(List.of()).when(clusterService).getOnlineServers();
        var queued = resourceService.submitServerTask("worker", "executor", 1, 1, () -> "unexpected");
        assertThrows(TimeoutException.class, () -> queued.get(100, TimeUnit.MILLISECONDS));
        leader.set("new-leader");
        var failure = assertThrows(ExecutionException.class, () -> queued.get(5, TimeUnit.SECONDS));
        assertEquals("Cluster leader changed, please retry later", failure.getCause().getMessage());
        verify(clusterService, never()).runOnServer(anyString(), any());
    }

    @Test
    void failoverStartsFreshAndOldCompletionsDoNotReleaseNewReservations() throws Exception {
        var oldStarted = new CountDownLatch(1);
        var oldFinish = new CountDownLatch(1);
        var oldTask = resourceService.submitServerTask("worker", "executor", 1, 1, () -> {
            oldStarted.countDown();
            oldFinish.await();
            return "old";
        });
        assertTrue(oldStarted.await(5, TimeUnit.SECONDS));
        leader.set("new-leader");
        var newLeaderService = newResourceService();
        var newStarted = new CountDownLatch(1);
        var newFinish = new CountDownLatch(1);
        var newTask = newLeaderService.submitServerTask("worker", "executor", 1, 1, () -> {
            newStarted.countDown();
            newFinish.await();
            return "new";
        });
        assertTrue(newStarted.await(5, TimeUnit.SECONDS));
        oldFinish.countDown();
        assertEquals("old", oldTask.get(5, TimeUnit.SECONDS));
        var queued = newLeaderService.submitServerTask("worker", "executor", 1, 1, () -> "queued");
        assertThrows(TimeoutException.class, () -> queued.get(100, TimeUnit.MILLISECONDS));
        newFinish.countDown();
        assertEquals("new", newTask.get(5, TimeUnit.SECONDS));
        assertEquals("queued", queued.get(5, TimeUnit.SECONDS));
    }

    @Test
    void filtersAgentsAndRecoversFromMissingAgentServer() throws Exception {
        var eligible = agent(1L, true, false);
        var paused = agent(2L, true, true);
        var offline = agent(3L, false, false);
        var query = new AgentQuery();
        when(agentService.query(query, 0, Integer.MAX_VALUE)).thenAnswer(it -> {
            assertEquals(leader.get(), executingServer.get());
            return List.of(eligible, paused, offline);
        });
        when(agentService.get(1L)).thenReturn(eligible);
        when(agentService.load(1L)).thenReturn(eligible);
        var failed = resourceService.submitAgentTask(null, query, "executor", 1, 1, id -> id);
        assertThrows(ExecutionException.class, () -> failed.get(5, TimeUnit.SECONDS));
        when(agentService.getAgentServer(1L)).thenReturn("worker");
        assertEquals(1L, resourceService.submitAgentTask(null, query, "executor", 1, 1, id -> {
            assertEquals("worker", executingServer.get());
            return id;
        }).get(5, TimeUnit.SECONDS));
        assertNotNull(eligible.getLastUsedDate().getValue());
        assertEquals(1L, resourceService.submitAgentTask(1L, query, "executor", 1, 1,
                id -> id).get(5, TimeUnit.SECONDS));
    }

    private Agent agent(Long id, boolean online, boolean paused) {
        var agent = mock(Agent.class);
        when(agent.getId()).thenReturn(id);
        when(agent.isOnline()).thenReturn(online);
        when(agent.isPaused()).thenReturn(paused);
        when(agent.getLastUsedDate()).thenReturn(new AgentLastUsedDate());
        return agent;
    }

}
