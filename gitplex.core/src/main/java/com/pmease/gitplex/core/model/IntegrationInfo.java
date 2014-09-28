package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class IntegrationInfo implements Serializable {
	
	private String branchHead;
	
	private String requestHead;
	
	private String integrationHead;
	
	// Use Boolean instead of boolean to avoid Hibernate limitation
	// on embedded property
	private Boolean hasChanges;
	
	@SuppressWarnings("unused")
	private IntegrationInfo() {
	}
	
	public IntegrationInfo(String branchHead, String requestHead, @Nullable String integrationHead, boolean hasChanges) {
		this.branchHead = branchHead;
		this.requestHead = requestHead;
		this.integrationHead = integrationHead;
		this.hasChanges = hasChanges;
	}

	public String getBranchHead() {
		return branchHead;
	}

	public String getRequestHead() {
		return requestHead;
	}

	/**
	 * Get integration commit. 
	 *  
	 * @return
	 * 			integration commit. <tt>null</tt> will be returned if there are integration conflicts
	 */
	public @Nullable String getIntegrationHead() {
		return integrationHead;
	}

	public boolean hasChanges() {
		return hasChanges != null && hasChanges;
	}
	
}