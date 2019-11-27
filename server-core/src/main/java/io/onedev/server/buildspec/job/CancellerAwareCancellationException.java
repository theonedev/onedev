package io.onedev.server.buildspec.job;

import java.util.concurrent.CancellationException;

import javax.annotation.Nullable;

public class CancellerAwareCancellationException extends CancellationException {

	private static final long serialVersionUID = 1L;
	
	private final Long cancellerId;
	
	public CancellerAwareCancellationException(@Nullable Long cancellerId) {
		this.cancellerId = cancellerId;
	}
	
	@Nullable
	public Long getCancellerId() {
		return cancellerId;
	}
	
}
