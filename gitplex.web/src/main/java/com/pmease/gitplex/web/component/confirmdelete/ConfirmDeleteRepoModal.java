package com.pmease.gitplex.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteRepoModal extends ConfirmDeleteModal {

	public ConfirmDeleteRepoModal(String id) {
		super(id);
	}

	@Override
	protected void doDelete(AjaxRequestTarget target, ConfirmDeleteModalBehavior behavior) {
		ConfirmDeleteRepoModalBehavior confirmDeleteRepoBehavior = (ConfirmDeleteRepoModalBehavior) behavior;
		Repository repository = confirmDeleteRepoBehavior.getRepository();
		
		GitPlex.getInstance(RepositoryManager.class).delete(repository);
		getSession().success("Repository has been deleted");
		
		onDeleted(target);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
}
