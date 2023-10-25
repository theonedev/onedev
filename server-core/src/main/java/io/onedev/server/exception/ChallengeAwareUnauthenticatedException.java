package io.onedev.server.exception;

import org.apache.shiro.authz.UnauthenticatedException;

import javax.annotation.Nullable;

public class ChallengeAwareUnauthenticatedException extends UnauthenticatedException {

	private static final long serialVersionUID = 1L;
	
	private final String challenge;
	
	public ChallengeAwareUnauthenticatedException(String challenge, @Nullable String message) {
		super(message);
		this.challenge = challenge;
	}

	public ChallengeAwareUnauthenticatedException(String challenge) {
		this(challenge, null);
	}
	
	public String getChallenge() {
		return challenge;
	}
}
