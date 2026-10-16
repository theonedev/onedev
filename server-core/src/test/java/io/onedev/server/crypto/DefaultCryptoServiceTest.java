package io.onedev.server.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.model.Setting;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.SettingService;

class DefaultCryptoServiceTest {

	private final SettingService settingService = mock(SettingService.class);

	private final TransactionService transactionService = mock(TransactionService.class);

	private final ClusterService clusterService = mock(ClusterService.class);

	private final DefaultCryptoService cryptoService = newCryptoService();

	private DefaultCryptoService newCryptoService() {
		return Guice.createInjector(binder -> {
			binder.bind(SettingService.class).toInstance(settingService);
			binder.bind(TransactionService.class).toInstance(transactionService);
			binder.bind(ClusterService.class).toInstance(clusterService);
		}).getInstance(DefaultCryptoService.class);
	}

	private SshSetting sshSetting(int offset) {
		byte[] encoded = new byte[64];
		for (int i = 0; i < encoded.length; i++)
			encoded[i] = (byte) (i + offset);
		var privateKey = mock(PrivateKey.class);
		when(privateKey.getEncoded()).thenAnswer(it -> encoded.clone());
		var sshSetting = mock(SshSetting.class);
		when(sshSetting.getPrivateKey()).thenReturn(privateKey);
		return sshSetting;
	}

	@Test
	void cachesKeyAndPreservesExistingCiphertext() {
		var sshSetting = sshSetting(0);
		when(settingService.getSshSetting()).thenReturn(sshSetting);
		// Captured from the original implementation using encoded private-key bytes 0 through 63.
		byte[] oldKey = Base64.getDecoder().decode("fLpW9Nn+cp8SKEgss4otvEHZ0RdTbWP391YC0Q/5GGo=");
		byte[] oldCiphertext = Base64.getDecoder().decode(
				"Fa6Sl+VOchRgq/PL1GrBXdOzAtut5TqU554/MBgv85CYPO8Zri5U7V3AJT9oAOi4bYSj");
		assertArrayEquals(oldKey, cryptoService.getCipherKey());
		assertArrayEquals("existing ciphertext".getBytes(UTF_8), cryptoService.decrypt(oldCiphertext));

		byte[] data = new byte[1024];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) i;
		byte[] encrypted = cryptoService.encrypt(data);
		assertArrayEquals(data, cryptoService.decrypt(encrypted));
		assertFalse(Arrays.equals(encrypted, cryptoService.encrypt(data)));
		verify(settingService).getSshSetting();
		verify(sshSetting).getPrivateKey();
	}

	@Test
	void initializesOnlyOnceAndReadsCachedKeyWithoutLocking() throws Exception {
		var sshSetting = sshSetting(0);
		when(settingService.getSshSetting()).thenReturn(sshSetting);
		var executor = Executors.newFixedThreadPool(8);
		var ready = new CountDownLatch(8);
		var start = new CountDownLatch(1);
		try {
			List<Future<byte[]>> results = new ArrayList<>();
			for (int i = 0; i < 8; i++) {
				results.add(executor.submit(() -> {
					ready.countDown();
					assertTrue(start.await(5, TimeUnit.SECONDS));
					return cryptoService.getCipherKey();
				}));
			}
			assertTrue(ready.await(5, TimeUnit.SECONDS));
			start.countDown();
			byte[] key = results.get(0).get(5, TimeUnit.SECONDS);
			for (var result: results)
				assertSame(key, result.get(5, TimeUnit.SECONDS));
			verify(sshSetting).getPrivateKey();
			synchronized (cryptoService) {
				var cachedRead = executor.submit(cryptoService::getCipherKey);
				assertSame(key, cachedRead.get(5, TimeUnit.SECONDS));
			}
		} finally {
			start.countDown();
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	@Test
	void oldInitializationDoesNotOverwriteUpdatedKey() throws Exception {
		var oldSshSetting = sshSetting(0);
		var oldPrivateKey = oldSshSetting.getPrivateKey();
		var initializationStarted = new CountDownLatch(1);
		var finishInitialization = new CountDownLatch(1);
		when(oldSshSetting.getPrivateKey()).thenAnswer(it -> {
			initializationStarted.countDown();
			assertTrue(finishInitialization.await(5, TimeUnit.SECONDS));
			return oldPrivateKey;
		});
		when(settingService.getSshSetting()).thenReturn(oldSshSetting);
		doAnswer(it -> {
			it.<Runnable>getArgument(0).run();
			return null;
		}).when(transactionService).runAfterCommit(any());
		var submittedTask = new AtomicReference<ClusterTask<?>>();
		when(clusterService.submitToAllServers(any())).thenAnswer(it -> {
			submittedTask.set(it.getArgument(0));
			return Map.of();
		});

		var executor = Executors.newFixedThreadPool(2);
		try {
			var oldInitialization = executor.submit(cryptoService::getCipherKey);
			assertTrue(initializationStarted.await(5, TimeUnit.SECONDS));
			var newSshSetting = sshSetting(1);
			when(settingService.getSshSetting()).thenReturn(newSshSetting);
			var setting = new Setting();
			setting.setKey(Setting.Key.SSH);
			cryptoService.on(new EntityPersisted(setting, false));
			var invalidationStarted = new CountDownLatch(1);
			var invalidation = executor.submit(() -> {
				invalidationStarted.countDown();
				return submittedTask.get().call();
			});
			assertTrue(invalidationStarted.await(5, TimeUnit.SECONDS));
			assertThrows(TimeoutException.class, () -> invalidation.get(100, TimeUnit.MILLISECONDS));

			finishInitialization.countDown();
			byte[] oldKey = oldInitialization.get(5, TimeUnit.SECONDS);
			invalidation.get(5, TimeUnit.SECONDS);
			byte[] newKey = cryptoService.getCipherKey();
			assertFalse(Arrays.equals(newKey, oldKey));
			assertSame(newKey, cryptoService.getCipherKey());
		} finally {
			finishInitialization.countDown();
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	@Test
	void invalidatesEachServerAfterCommitAndReloadsLazily() throws Exception {
		var oldSshSetting = sshSetting(0);
		when(settingService.getSshSetting()).thenReturn(oldSshSetting);
		var otherServer = newCryptoService();
		byte[] oldKey = cryptoService.getCipherKey();
		byte[] otherOldKey = otherServer.getCipherKey();
		var afterCommit = new AtomicReference<Runnable>();
		doAnswer(it -> {
			afterCommit.set(it.getArgument(0));
			return null;
		}).when(transactionService).runAfterCommit(any());

		var previousInjector = AppLoader.injector;
		try {
			var currentServer = new AtomicReference<CryptoService>();
			var submittedTask = new AtomicReference<byte[]>();
			var pendingTask = mock(Future.class);
			AppLoader.injector = Guice.createInjector(binder ->
					binder.bind(CryptoService.class).toProvider(currentServer::get));
			when(clusterService.submitToAllServers(any())).thenAnswer(it -> {
				ClusterTask<?> task = it.getArgument(0);
				submittedTask.set(SerializationUtils.serialize(task));
				return Map.of("first", pendingTask, "second", pendingTask);
			});

			var setting = new Setting();
			setting.setKey(Setting.Key.SSH);
			cryptoService.on(new EntityPersisted(setting, false));
			assertNotNull(afterCommit.get());
			verifyNoInteractions(clusterService);
			assertSame(oldKey, cryptoService.getCipherKey());
			assertSame(otherOldKey, otherServer.getCipherKey());

			var newSshSetting = sshSetting(1);
			when(settingService.getSshSetting()).thenReturn(newSshSetting);
			afterCommit.get().run();
			verify(clusterService).submitToAllServers(any());
			verifyNoInteractions(pendingTask);
			assertSame(oldKey, cryptoService.getCipherKey());
			assertSame(otherOldKey, otherServer.getCipherKey());

			for (var server: List.of(cryptoService, otherServer)) {
				currentServer.set(server);
				ClusterTask<?> remoteTask = SerializationUtils.deserialize(submittedTask.get());
				remoteTask.call();
			}
			verify(newSshSetting, never()).getPrivateKey();
			byte[] newKey = cryptoService.getCipherKey();
			assertFalse(Arrays.equals(oldKey, newKey));
			assertArrayEquals(newKey, otherServer.getCipherKey());
			assertSame(newKey, cryptoService.getCipherKey());
			verify(newSshSetting, times(2)).getPrivateKey();
		} finally {
			AppLoader.injector = previousInjector;
		}
	}

	@Test
	void ignoresUnrelatedChanges() {
		var setting = new Setting();
		setting.setKey(Setting.Key.SYSTEM);
		cryptoService.on(new EntityPersisted(setting, false));
		cryptoService.on(new EntityPersisted(new User(), false));
		verifyNoInteractions(settingService, transactionService, clusterService);
	}

}
