package com.pmease.gitplex.web.component.createbranch;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.security.SecurityUtils;

@SuppressWarnings("serial")
public abstract class CreateBranchLink extends ModalLink {

	private final IModel<Depot> depotModel;
	
	private final String revision;
	
	public CreateBranchLink(String id, IModel<Depot> depotModel, String revision) {
		super(id);
		
		this.depotModel = depotModel;
		this.revision = revision;
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		ObjectId commitId = depotModel.getObject().getRevCommit(revision);
		setVisible(SecurityUtils.canPushRef(depotModel.getObject(), Constants.R_HEADS, ObjectId.zeroId(), commitId));
	}
	
	@Override
	protected Component newContent(String id) {
		return new CreateBranchPanel(id, depotModel, revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String branch) {
				close(target);
				CreateBranchLink.this.onCreate(target, branch);
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

	protected abstract void onCreate(AjaxRequestTarget target, String branch);
}
