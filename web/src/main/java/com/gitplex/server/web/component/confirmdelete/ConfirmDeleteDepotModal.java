package com.gitplex.server.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.manager.DepotManager;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteDepotModal extends ConfirmDeleteModal {

	public ConfirmDeleteDepotModal(AjaxRequestTarget target) {
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
