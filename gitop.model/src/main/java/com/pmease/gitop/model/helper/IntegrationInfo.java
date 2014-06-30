package com.pmease.gitop.model.helper;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class IntegrationInfo implements Serializable {
	
	private String branchHead;
	
	private String requestHead;
	
	private String integrationBase;
	
	private String integrationHead;
	
	@SuppressWarnings("unused")
	private IntegrationInfo() {
	}
	
	public IntegrationInfo(String branchHead, String requestHead, String integrationBase, @Nullable String integrationHead) {
		this.branchHead = branchHead;
		this.requestHead = requestHead;
		this.integrationBase = integrationBase;
		this.integrationHead = integrationHead;
	}

	public String getBranchHead() {
		return branchHead;
	}

	public String getRequestHead() {
		return requestHead;
	}

	@Nullable
	public String getIntegrationHead() {
		return integrationHead;
	}
	
	public String getIntegrationBase() {
		return integrationBase;
	}

}