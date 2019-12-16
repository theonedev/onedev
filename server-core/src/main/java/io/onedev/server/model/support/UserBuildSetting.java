package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class UserBuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<JobSecret> secrets = new ArrayList<>();
	
	private List<BuildPreservation> buildPreservations = new ArrayList<>();
	
	public List<JobSecret> getSecrets() {
		return secrets;
	}

	public void setSecrets(List<JobSecret> secrets) {
		this.secrets = secrets;
	}

	public List<BuildPreservation> getBuildPreservations() {
		return buildPreservations;
	}

	public void setBuildPreservations(List<BuildPreservation> buildPreservations) {
		this.buildPreservations = buildPreservations;
	}
	
}
