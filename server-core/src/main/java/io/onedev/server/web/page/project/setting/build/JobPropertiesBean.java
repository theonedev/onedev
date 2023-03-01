package io.onedev.server.web.page.project.setting.build;

import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.annotation.Editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class JobPropertiesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<JobProperty> properties = new ArrayList<>();

	@Editable
	public List<JobProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<JobProperty> properties) {
		this.properties = properties;
	}
	
}
