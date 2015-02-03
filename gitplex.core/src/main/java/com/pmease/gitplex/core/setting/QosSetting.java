package com.pmease.gitplex.core.setting;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.Range;

import com.pmease.commons.editable.annotation.Editable;

/**
 * This setting controls QOS (quality of service) of the system. 
 *
 */
@Editable
public class QosSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer integrationPreviewWorkers;
	
	@Editable(description="Specify number of workers available to calculate pull request integration previews. "
			+ "If leave empty, GitPlex will default this value to number of cores of the machine.")
	@Range(min=1, max=10000)
	@Nullable
	public Integer getIntegrationPreviewWorkers() {
		return integrationPreviewWorkers;
	}

	public void setIntegrationPreviewWorkers(Integer integrationPreviewWorkers) {
		this.integrationPreviewWorkers = integrationPreviewWorkers;
	}

}
