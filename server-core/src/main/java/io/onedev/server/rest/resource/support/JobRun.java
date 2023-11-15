package io.onedev.server.rest.resource.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.rest.annotation.Api;

public abstract class JobRun implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Api(order=250) 
	private String jobName;
	
	@Api(order=260, description="A map of param name to value list. Normally the value list "
			+ "contains only one param value. However in case the job param is defined as "
			+ "multi-valued in build spec, you can add multiple param values") 
	private Map<String, List<String>> params = new HashMap<>();

	@Api(order=1000) 
	private String reason;

	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@NotNull
	public Map<String, List<String>> getParams() {
		return params;
	}

	public void setParams(Map<String, List<String>> params) {
		this.params = params;
	}

	@NotEmpty
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
}