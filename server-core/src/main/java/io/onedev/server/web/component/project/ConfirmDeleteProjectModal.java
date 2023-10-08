package io.onedev.server.web.component.project;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteProjectModal extends ConfirmModalPanel {

	public ConfirmDeleteProjectModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void onConfirm(AjaxRequestTarget target) {
		Project project = getProject();
		
		OneDev.getInstance(ProjectManager.class).delete(project);
		getSession().success("Project '" + project.getPath() + "' deleted");
		
		onDeleted(target);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	@Override
	protected String getConfirmMessage() {
		return "Everything inside this project and all child projects will be deleted and can not be recovered, "
				+ "please type project path <code>" + getProject().getPath() + "</code> below to confirm deletion.";
	}

	@Override
	protected String getConfirmInput() {
		return getProject().getPath();
	}

	protected abstract Project getProject();
	
}
