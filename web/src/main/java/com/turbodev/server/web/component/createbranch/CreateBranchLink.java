package com.turbodev.server.web.component.createbranch;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.turbodev.server.model.Project;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.component.modal.ModalLink;
import com.turbodev.server.web.component.modal.ModalPanel;

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
		setVisible(SecurityUtils.canWrite(projectModel.getObject()));
	}
	
	@Override
	protected Component newContent(String id, ModalPanel modal) {
		return new CreateBranchPanel(id, projectModel, revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String branch) {
				modal.close();
				CreateBranchLink.this.onCreate(target, branch);
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

	protected abstract void onCreate(AjaxRequestTarget target, String branch);
}
