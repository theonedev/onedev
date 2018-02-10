package com.turbodev.server.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.model.Project;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteProjectModal extends ConfirmDeleteModal {

	public ConfirmDeleteProjectModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void doDelete(AjaxRequestTarget target) {
		Project project = getProject();
		
		TurboDev.getInstance(ProjectManager.class).delete(project);
		getSession().success("Repository has been deleted");
		
		onDeleted(target);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	@Override
	protected String getWarningMessage() {
		return "Everything inside this project will be deleted and can not be recovered, "
				+ "please input project name \"" + getProject().getName() + "\" below to confirm deletion.";
	}

	@Override
	protected String getConfirmInput() {
		return getProject().getName();
	}

	protected abstract Project getProject();
	
}
