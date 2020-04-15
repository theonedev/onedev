package io.onedev.server.web.component.issue.workflowreconcile;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.util.AjaxPayload;

public class WorkflowChanged extends AjaxPayload {

	public WorkflowChanged(AjaxRequestTarget target) {
		super(target);
	}

}