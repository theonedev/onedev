package io.onedev.server.web.component.issue.workflowreconcile;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class WorkflowReconcileResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public WorkflowReconcileResourceReference() {
		super(WorkflowReconcileResourceReference.class, "workflow-reconcile.css");
	}
	
}
