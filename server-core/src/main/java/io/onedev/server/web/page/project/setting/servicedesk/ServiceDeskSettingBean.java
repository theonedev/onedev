package io.onedev.server.web.page.project.setting.servicedesk;

import java.io.Serializable;

import io.onedev.server.annotation.ProjectName;
import io.onedev.server.annotation.Editable;

@Editable
public class ServiceDeskSettingBean implements Serializable {

	private static final long serialVersionUID = 1L;

	static final String PROP_SERVICE_DESK_NAME = "serviceDeskName";
	
	private String serviceDeskName;
	
	@Editable(order=100, placeholder="Use project path", description="Service desk name can be used to "
			+ "construct service desk email address of current project, which takes the form "
			+ "<tt>&lt;system email address name&gt;+&lt;service desk name&gt;@&lt;system email address domain&gt;</tt>. "
			+ "Issues can be created in this project by sending email to this address")
	@ProjectName
	public String getServiceDeskName() {
		return serviceDeskName;
	}

	public void setServiceDeskName(String serviceDeskName) {
		this.serviceDeskName = serviceDeskName;
	}
	
}
