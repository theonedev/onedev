package io.onedev.server.web.page.admin.workspaceprovisioner;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;

@Editable
public class WorkspaceProvisionerBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private WorkspaceProvisioner provisioner;

	@Editable(name="Type")
	@NotNull
	public WorkspaceProvisioner getProvisioner() {
		return provisioner;
	}

	public void setProvisioner(WorkspaceProvisioner provisioner) {
		this.provisioner = provisioner;
	}

}
