package io.onedev.server.util.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.service.SettingService;

public class DefaultWorkExecutionServiceTest {

	private DefaultWorkExecutionService service;

	private QueuedExecutor executor;

	private Subject previousSubject;

	@BeforeEach
	public void setup() throws Exception {
		previousSubject = ThreadContext.getSubject();
		ThreadContext.bind(mock(Subject.class));
		var performanceSetting = new PerformanceSetting();
		performanceSetting.setCpuIntensiveTaskConcurrency(1);
		var settingService = mock(SettingService.class);
		when(settingService.getPerformanceSetting()).thenReturn(performanceSetting);
		executor = new QueuedExecutor();
		service = new DefaultWorkExecutionService();
		inject("settingService", settingService);
		inject("executorService", executor);
	}

	@AfterEach
	public void cleanup() {
		executor.shutdownNow();
		if (previousSubject != null)
			ThreadContext.bind(previousSubject);
		else
			ThreadContext.unbindSubject();
	}

	@Test
	public void testCancellationBeforeExecutionReleasesConcurrencySlot() throws Exception {
		for (boolean mayInterrupt : new boolean[] {false, true}) {
			var cancelledRan = new AtomicBoolean();
			var cancelled = service.submit(2, "first", () -> {
				cancelledRan.set(true);
				return "cancelled";
			});
			var next = service.submit(2, "next", () -> "next");
			assertEquals(1, executor.tasks.size());

			assertTrue(cancelled.cancel(mayInterrupt));
			executor.runNext();

			assertFalse(cancelledRan.get());
			assertTrue(cancelled.isCancelled());
			assertThrows(CancellationException.class, cancelled::get);
			assertEquals(1, executor.tasks.size());
			executor.runNext();
			assertEquals("next", next.get());
		}
	}

	@Test
	public void testCancelledGroupMemberDoesNotReleaseOtherMembersSlot() throws Exception {
		var first = service.submit(2, "group", () -> "first");
		var cancelledRan = new AtomicBoolean();
		var cancelled = service.submit(2, "group", () -> {
			cancelledRan.set(true);
			return "cancelled";
		});
		var next = service.submit(2, "next", () -> "next");
		var firstTask = executor.takeNext();

		assertTrue(cancelled.cancel(true));
		executor.runNext();

		assertFalse(cancelledRan.get());
		assertTrue(executor.tasks.isEmpty());
		assertFalse(next.isDone());
		firstTask.run();
		assertEquals("first", first.get());
		assertEquals(1, executor.tasks.size());
		executor.runNext();
		assertEquals("next", next.get());
	}

	@Test
	public void testFailurePropagatesAndReleasesConcurrencySlot() throws Exception {
		var failure = new IllegalStateException("work failed");
		var failed = service.submit(2, "failing", () -> {
			throw failure;
		});
		var next = service.submit(2, "next", () -> "next");

		executor.runNext();

		assertSame(failure, assertThrows(ExecutionException.class, failed::get).getCause());
		assertEquals(1, executor.tasks.size());
		executor.runNext();
		assertEquals("next", next.get());
	}

	@Test
	public void testRunningCancellationRetainsSlotUntilWorkActuallyFinishes() throws Exception {
		var started = new CountDownLatch(1);
		var interrupted = new CountDownLatch(1);
		var finish = new CountDownLatch(1);
		var completed = new CountDownLatch(1);
		var failure = new AtomicReference<Throwable>();
		var running = service.submit(2, "running", () -> {
			started.countDown();
			while (true) {
				try {
					finish.await();
					return "finished";
				} catch (InterruptedException e) {
					interrupted.countDown();
				}
			}
		});
		var next = service.submit(2, "next", () -> "next");
		var task = executor.takeNext();
		var worker = new Thread(() -> {
			try {
				task.run();
			} catch (Throwable e) {
				failure.set(e);
			} finally {
				ThreadContext.remove();
				completed.countDown();
			}
		}, "work-execution-test");
		worker.setDaemon(true);
		worker.start();
		try {
			assertTrue(started.await(5, TimeUnit.SECONDS));
			assertTrue(running.cancel(true));
			assertTrue(interrupted.await(5, TimeUnit.SECONDS));
			assertTrue(running.isCancelled());
			assertTrue(executor.tasks.isEmpty());
			assertFalse(next.isDone());

			finish.countDown();
			assertTrue(completed.await(5, TimeUnit.SECONDS));
			assertNull(failure.get());
			assertEquals(1, executor.tasks.size());
			executor.runNext();
			assertEquals("next", next.get());
		} finally {
			finish.countDown();
			worker.join(5000);
			assertFalse(worker.isAlive());
		}
	}

	private void inject(String name, Object value) throws Exception {
		var field = DefaultWorkExecutionService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(service, value);
	}

	private static class QueuedExecutor extends AbstractExecutorService {

		private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

		private boolean shutdown;

		@Override
		public void execute(Runnable command) {
			if (shutdown)
				throw new RejectedExecutionException();
			tasks.add(command);
		}

		private Runnable takeNext() {
			var task = tasks.poll();
			assertNotNull(task);
			return task;
		}

		private void runNext() {
			takeNext().run();
		}

		@Override
		public void shutdown() {
			shutdown = true;
		}

		@Override
		public List<Runnable> shutdownNow() {
			shutdown();
			var pending = new ArrayList<>(tasks);
			tasks.clear();
			return pending;
		}

		@Override
		public boolean isShutdown() {
			return shutdown;
		}

		@Override
		public boolean isTerminated() {
			return shutdown && tasks.isEmpty();
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) {
			return isTerminated();
		}
	}

}
