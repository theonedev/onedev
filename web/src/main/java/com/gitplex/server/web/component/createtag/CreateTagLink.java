package com.gitplex.server.web.component.createtag;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.Depot;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.modal.ModalLink;

@SuppressWarnings("serial")
public abstract class CreateTagLink extends ModalLink {

	private final IModel<Depot> depotModel;
	
	private final String revision;
	
	public CreateTagLink(String id, IModel<Depot> depotModel, String revision) {
		super(id);
		
		this.depotModel = depotModel;
		this.revision = revision;
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		ObjectId commit = depotModel.getObject().getRevCommit(revision);
		setVisible(SecurityUtils.canPushRef(depotModel.getObject(), Constants.R_TAGS, 
				ObjectId.zeroId(), commit));
	}
	
	@Override
	protected Component newContent(String id) {
		return new CreateTagPanel(id, depotModel, revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String tag) {
				close(target);
				CreateTagLink.this.onCreate(target, tag);
			}

			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close(target);
			}
			
		};
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		
		super.onDetach();
	}

	protected abstract void onCreate(AjaxRequestTarget target, String tag);
}
