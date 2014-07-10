package com.pmease.gitop.model.helper;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class IntegrationInfo implements Serializable {
	
	public enum IntegrateApproach {REBASE_SOURCE, REBASE_TARGET, MERGE_ALWAYS, MERGE_IF_NECESSARY};
	
	private String branchHead;
	
	private String requestHead;
	
	private String integrationHead;
	
	private IntegrateApproach integrateApproach;
	
	@SuppressWarnings("unused")
	private IntegrationInfo() {
	}
	
	public IntegrationInfo(String branchHead, String requestHead, @Nullable String integrationHead, 
			IntegrateApproach integrateApproach) {
		this.branchHead = branchHead;
		this.requestHead = requestHead;
		this.integrationHead = integrationHead;
		this.integrateApproach = integrateApproach;
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

	public IntegrateApproach getIntegrateApproach() {
		return integrateApproach;
	}
	
}