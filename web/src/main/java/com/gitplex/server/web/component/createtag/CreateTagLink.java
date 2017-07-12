package com.gitplex.server.web.component.createtag;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;

import com.gitplex.server.model.Project;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.modal.ModalLink;
import com.gitplex.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class CreateTagLink extends ModalLink {

	private final IModel<Project> projectModel;
	
	private final String revision;
	
	public CreateTagLink(String id, IModel<Project> projectModel, String revision) {
		super(id);
		
		this.projectModel = projectModel;
		this.revision = revision;
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		setVisible(SecurityUtils.canCreateTag(projectModel.getObject(), Constants.R_TAGS));
	}
	
	@Override
	protected Component newContent(String id, ModalPanel modal) {
		return new CreateTagPanel(id, projectModel, null, revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String tag) {
				modal.close();
				CreateTagLink.this.onCreate(target, tag);
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

	protected abstract void onCreate(AjaxRequestTarget target, String tag);
}
