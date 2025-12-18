package io.onedev.server.service;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

public interface TemporalFutureService {
	
    <T> void addFuture(String futureId, CompletableFuture<T> future);

    @Nullable
    <T> CompletableFuture<T> removeFuture(String futureId);

}