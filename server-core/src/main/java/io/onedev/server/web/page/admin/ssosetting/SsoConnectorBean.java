package io.onedev.server.web.page.admin.ssosetting;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

@Editable
public class SsoConnectorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private SsoConnector connector;

	@Editable
	@OmitName
	@NotNull
	public SsoConnector getConnector() {
		return connector;
	}

	public void setConnector(SsoConnector connector) {
		this.connector = connector;
	}

}
