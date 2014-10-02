package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;

@SuppressWarnings("serial")
@Embeddable
public class IntegrationPreview implements Serializable {
	
	@Column(name="G_PV_TARGET_HEAD")
	private String targetHead;
	
	@Column(name="G_PV_REQUEST_HEAD")
	private String requestHead;
	
	@Column(name="G_PV_INT_STRATEGY")
	private IntegrationStrategy integrationStrategy;
	
	@Column(name="G_PV_INTEGRATED")
	private String integrated;
	
	@SuppressWarnings("unused")
	private IntegrationPreview() {
	}
	
	public IntegrationPreview(String targetHead, String requestHead, IntegrationStrategy integrationStrategy, 
			@Nullable String previewIntegrated) {
		this.targetHead = targetHead;
		this.requestHead = requestHead;
		this.integrationStrategy = integrationStrategy;
		this.integrated = previewIntegrated;
	}

	public String getTargetHead() {
		return targetHead;
	}

	public String getRequestHead() {
		return requestHead;
	}

	public IntegrationStrategy getIntegrationStrategy() {
		return integrationStrategy;
	}

	public String getIntegrated() {
		return integrated;
	}

	public void setIntegrated(String integrated) {
		this.integrated = integrated;
	}

}