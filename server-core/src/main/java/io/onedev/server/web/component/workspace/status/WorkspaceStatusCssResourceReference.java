package io.onedev.server.web.component.workspace.status;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class WorkspaceStatusCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public WorkspaceStatusCssResourceReference() {
		super(WorkspaceStatusCssResourceReference.class, "workspace-status.css");
	}

}
