package com.pmease.gitplex.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.model.Depot;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteRepoModal extends ConfirmDeleteModal {

	public ConfirmDeleteRepoModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void doDelete(AjaxRequestTarget target) {
		Depot depot = getRepository();
		
		GitPlex.getInstance(DepotManager.class).delete(depot);
		getSession().success("Repository has been deleted");
		
		onDeleted(target);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	@Override
	protected String getWarningMessage() {
		return "Everything inside this repository will be deleted and can not be recovered, "
				+ "please input repository name below to confirm deletion.";
	}

	@Override
	protected String getConfirmInput() {
		return getRepository().getName();
	}

	protected abstract Depot getRepository();
	
}
