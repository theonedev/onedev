package com.pmease.gitplex.web.component.confirmdelete;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.WebSession;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteAccountModal extends ConfirmDeleteModal {

	public ConfirmDeleteAccountModal(String id) {
		super(id);
	}

	@Override
	protected void doDelete(AjaxRequestTarget target, ConfirmDeleteModalBehavior behavior) {
		UserManager userManager = GitPlex.getInstance(UserManager.class);
		
		ConfirmDeleteAccountModalBehavior confirmDeleteAccountBehavior = (ConfirmDeleteAccountModalBehavior) behavior;
		User account = confirmDeleteAccountBehavior.getAccount();
		if (account.equals(userManager.getCurrent())) {
			if (userManager.getPrevious() != null)
				SecurityUtils.getSubject().releaseRunAs();
			else
				WebSession.get().logout();
		} else if (account.equals(userManager.getPrevious())) {
			WebSession.get().logout();
		}
		
		GitPlex.getInstance(UserManager.class).delete(account);
		getSession().success("Account has been deleted");
		
		onDeleted(target);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
}
