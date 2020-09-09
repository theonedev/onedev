package io.onedev.server.web.component.branch.create;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class CreateBranchLink extends ModalLink {

	private final IModel<Project> projectModel;
	
	private final String revision;
	
	public CreateBranchLink(String id, IModel<Project> projectModel, String revision) {
		super(id);
		
		this.projectModel = projectModel;
		this.revision = revision;
	}
	
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canCreateBranch(projectModel.getObject(), Constants.R_HEADS));
	}
	
	@Override
	protected Component newContent(String id, ModalPanel modal) {
		return new CreateBranchPanel(id, projectModel, revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String branch) {
				modal.close();
				CreateBranchLink.this.onCreated(target, branch);
			}

			@Override
			protected void onCancel(AjaxRequestTarget target) {
				modal.close();
			}
			
		};
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		
		super.onDetach();
	}

	protected abstract void onCreated(AjaxRequestTarget target, String branch);
}
