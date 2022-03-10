package io.onedev.server.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.model.Build;

public class StatusInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String pipeline;
	
	private final Long requestId;
	
	private final String refName;
	
	private final Build.Status status;
	
	public StatusInfo(Build.Status status, @Nullable String pipeline, @Nullable Long requestId, @Nullable String refName) {
		this.status = status;
		this.pipeline = pipeline;
		this.requestId = requestId;
		this.refName = refName;
	}

	@Nullable
	public String getPipeline() {
		return pipeline;
	}

	@Nullable
	public Long getRequestId() {
		return requestId;
	}

	@Nullable
	public String getRefName() {
		return refName;
	}

	public Build.Status getStatus() {
		return status;
	}
	
}
