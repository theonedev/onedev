package com.gitplex.server.web.component.createbranch;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.gitplex.server.model.Depot;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.modal.ModalLink;
import com.gitplex.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class CreateBranchLink extends ModalLink {

	private final IModel<Depot> depotModel;
	
	private final String revision;
	
	public CreateBranchLink(String id, IModel<Depot> depotModel, String revision) {
		super(id);
		
		this.depotModel = depotModel;
		this.revision = revision;
	}
	
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canWrite(depotModel.getObject()));
	}
	
	@Override
	protected Component newContent(String id, ModalPanel modal) {
		return new CreateBranchPanel(id, depotModel, revision) {

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
		depotModel.detach();
		
		super.onDetach();
	}

	protected abstract void onCreate(AjaxRequestTarget target, String branch);
}
