package com.pmease.gitplex.web.component.addtag;

import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;

import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.security.SecurityUtils;

@SuppressWarnings("serial")
public abstract class AddTagLink extends ModalLink {

	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	public AddTagLink(String id, IModel<Repository> repoModel, String revision) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canModify(repoModel.getObject(), Constants.R_TAGS + UUID.randomUUID().toString()));
	}
	
	@Override
	protected Component newContent(String id) {
		return new AddTagPanel(id, repoModel, revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String tag) {
				close(target);
				AddTagLink.this.onCreate(target, tag);
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

	protected abstract void onCreate(AjaxRequestTarget target, String tag);
}
