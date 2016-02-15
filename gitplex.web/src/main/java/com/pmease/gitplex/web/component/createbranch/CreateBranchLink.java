package com.pmease.gitplex.web.component.createbranch;

import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.security.SecurityUtils;

@SuppressWarnings("serial")
public abstract class CreateBranchLink extends ModalLink {

	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	public CreateBranchLink(String id, IModel<Repository> repoModel, String revision) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canCreate(repoModel.getObject(), UUID.randomUUID().toString()));
	}
	
	@Override
	protected Component newContent(String id) {
		return new CreateBranchPanel(id, repoModel, revision) {

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
		repoModel.detach();
		
		super.onDetach();
	}

	protected abstract void onCreate(AjaxRequestTarget target, String branch);
}
