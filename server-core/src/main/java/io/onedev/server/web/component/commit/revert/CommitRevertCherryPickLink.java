package io.onedev.server.web.component.commit.revert;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class CommitRevertCherryPickLink extends ModalLink {

	private final IModel<Project> projectModel;
	
	private final String revision;

	private final Integer type;
	
	public CommitRevertCherryPickLink(String id, IModel<Project> projectModel, String revision, Integer type) {
		super(id);
		this.projectModel = projectModel;
		this.revision = revision;
		this.type = type;
	}
	
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canCreateBranch(projectModel.getObject(), Constants.R_HEADS));
	}
	
	@Override
	protected Component newContent(String id, ModalPanel modal) {
		return new CommitRevertCherryPickPanel(id, projectModel, revision, type) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String branch) {
				modal.close();
				CommitRevertCherryPickLink.this.onCreated(target, branch);
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
