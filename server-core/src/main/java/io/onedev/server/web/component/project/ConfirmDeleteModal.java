package io.onedev.server.web.component.project;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteModal extends ConfirmModalPanel {

	public ConfirmDeleteModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void onConfirm(AjaxRequestTarget target) {
		Project project = getProject();
		
		OneDev.getInstance(ProjectManager.class).requestToDelete(Sets.newHashSet(project));
		getSession().success("Project '" + project.getPath() + "' deleted");
	}
	
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
