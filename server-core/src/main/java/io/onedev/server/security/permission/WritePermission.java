package io.onedev.server.security.permission;

/** A permission to change project data, subject to the project's maintenance mode. */
public interface WritePermission extends BasePermission {

	@Override
	default boolean isWrite() {
		return true;
	}
}
