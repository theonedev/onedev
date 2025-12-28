package io.onedev.server.service;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

/**
 * Service for managing futures with automatic timeout handling and cleanup
 */
public interface ManagedFutureService {

	/**
	 * Add a future with specified timeout and optional callback when timeout occurs.
	 * 
	 * @param futureId unique identifier for the future
	 * @param future the future to manage
	 * @param timeout when the future should be considered timed out
	 * @param onTimeout optional callback invoked when timeout occurs (before cancellation)
	 */
	<T> void addFuture(String futureId, Future<T> future, Date timeout, @Nullable Consumer<Future<T>> onTimeout);

	/**
	 * Add a future with specified timeout in seconds and no timeout callback.
	 */
	default <T> void addFuture(String futureId, Future<T> future, int timeoutSeconds) {
		addFuture(futureId, future, new Date(System.currentTimeMillis() + timeoutSeconds * 1000L), null);
	}

	/**
	 * Add a future with specified timeout in seconds and a timeout callback.
	 */
	default <T> void addFuture(String futureId, Future<T> future, int timeoutSeconds, @Nullable Consumer<Future<T>> onTimeout) {
		addFuture(futureId, future, new Date(System.currentTimeMillis() + timeoutSeconds * 1000L), onTimeout);
	}

	/**
	 * Remove and return a future by its ID.
	 * 
	 * @return the future, or null if not found
	 */
	@Nullable
	<T> Future<T> removeFuture(String futureId);

	/**
	 * Get a future without removing it.
	 * 
	 * @return the future, or null if not found
	 */
	@Nullable
	<T> Future<T> getFuture(String futureId);

}

