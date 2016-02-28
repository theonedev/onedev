package com.pmease.gitplex.web.component.confirmdelete;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.WebSession;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteAccountModal extends ConfirmDeleteModal {

	public ConfirmDeleteAccountModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void doDelete(AjaxRequestTarget target) {
		AccountManager userManager = GitPlex.getInstance(AccountManager.class);
		
		Account account = getAccount();
		if (account.equals(userManager.getCurrent())) {
			if (userManager.getPrevious() != null)
				SecurityUtils.getSubject().releaseRunAs();
			else
				WebSession.get().logout();
		} else if (account.equals(userManager.getPrevious())) {
			WebSession.get().logout();
		}
		
		GitPlex.getInstance(AccountManager.class).delete(account);
		getSession().success("Account has been deleted");
		
		onDeleted(target);
	}

	@Override
	protected String getConfirmInput() {
		return getAccount().getName();
	}

	@Override
	protected String getWarningMessage() {
		return "All repositories belonging to this account will also be deleted, please input account name below to confirm deletion.";
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	protected abstract Account getAccount();
}
