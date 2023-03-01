package io.onedev.server.web.page.admin.servicedesk;

import java.io.Serializable;

import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.annotation.Editable;

@Editable
public class ServiceDeskSettingHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private ServiceDeskSetting serviceDeskSetting;

	@Editable(name="Enable")
	public ServiceDeskSetting getServiceDeskSetting() {
		return serviceDeskSetting;
	}

	public void setServiceDeskSetting(ServiceDeskSetting serviceDeskSetting) {
		this.serviceDeskSetting = serviceDeskSetting;
	}
	
}
