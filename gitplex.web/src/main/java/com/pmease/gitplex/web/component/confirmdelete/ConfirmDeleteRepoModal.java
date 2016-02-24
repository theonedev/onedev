package com.pmease.gitplex.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteRepoModal extends ConfirmDeleteModal {

	public ConfirmDeleteRepoModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void doDelete(AjaxRequestTarget target) {
		Depot depot = getDepot();
		
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
		return getDepot().getName();
	}

	protected abstract Depot getDepot();
	
}
