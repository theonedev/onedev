package io.onedev.server.security;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authz.AuthorizationInfo;
import org.jspecify.annotations.Nullable;

/** Shares authorization information within a synchronous operation and its nested calls. */
final class AuthorizationInfoCache implements AutoCloseable {

	private static final ThreadLocal<Map<String, AuthorizationInfo>> cache = new ThreadLocal<>();

	private final boolean owner;

	private AuthorizationInfoCache() {
		owner = cache.get() == null;
		if (owner)
			cache.set(new HashMap<>());
	}

	static AuthorizationInfoCache open() {
		return new AuthorizationInfoCache();
	}

	@Nullable
	static Map<String, AuthorizationInfo> get() {
		return cache.get();
	}

	@Override
	public void close() {
		if (owner)
			cache.remove();
	}

}
