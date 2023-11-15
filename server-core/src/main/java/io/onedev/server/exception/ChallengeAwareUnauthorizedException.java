package io.onedev.server.exception;

import org.apache.shiro.authz.UnauthorizedException;

import javax.annotation.Nullable;

public class ChallengeAwareUnauthorizedException extends UnauthorizedException {

	private static final long serialVersionUID = 1L;
	
	private final String challenge;
	
	public ChallengeAwareUnauthorizedException(String challenge, @Nullable String message) {
		super(message);
		this.challenge = challenge;
	}

	public ChallengeAwareUnauthorizedException(String challenge) {
		this(challenge, null);
	}
	
	public String getChallenge() {
		return challenge;
	}
}
