package io.onedev.server.web.component.project;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.confirmaction.ConfirmActionModal;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteProjectModal extends ConfirmActionModal {

	public ConfirmDeleteProjectModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void onConfirm(AjaxRequestTarget target) {
		Project project = getProject();
		
		OneDev.getInstance(ProjectManager.class).delete(project);
		getSession().success("Project has been deleted");
		
		onDeleted(target);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	@Override
	protected String getConfirmMessage() {
		return "Everything inside this project will be deleted and can not be recovered, "
				+ "please input project name \"" + getProject().getName() + "\" below to confirm deletion.";
	}

	@Override
	protected String getConfirmInput() {
		return getProject().getName();
	}

	@Override
	protected void onCancel(AjaxRequestTarget target) {
	}

	protected abstract Project getProject();
	
}
