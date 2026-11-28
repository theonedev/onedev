package io.onedev.server.security;

import org.apache.shiro.realm.Realm;

/** Verifies the password. Callers establishing a login must enforce the user's two-factor requirement. */
public interface AuthenticatingService extends Realm {
}
