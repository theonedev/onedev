package com.pmease.gitplex.web.page.account.collaborators;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.page.account.AccountPeoplePage;

@SuppressWarnings("serial")
public abstract class CollaboratorPage extends AccountPeoplePage {

	private static final String PARAM_COLLABORATOR = "collaborator";

	protected final IModel<Account> collaboratorModel;
	
	public CollaboratorPage(PageParameters params) {
		super(params);
		
		String collaboratorName = params.get(PARAM_COLLABORATOR).toString();
		collaboratorModel = new LoadableDetachableModel<Account>() {

			@Override
			protected Account load() {
				AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
				Account collaborator = accountManager.findByName(collaboratorName);
				return Preconditions.checkNotNull(collaborator);
			}
			
		};
	}
	
	public static PageParameters paramsOf(Account account, Account collaborator) {
		PageParameters params = paramsOf(account);
		params.add(PARAM_COLLABORATOR, collaborator.getName());
		return params;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AvatarLink("collaboratorAvatar", collaboratorModel.getObject()));
		add(new UserLink("collaboratorName", collaboratorModel.getObject()));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}
	
	@Override
	protected void onDetach() {
		collaboratorModel.detach();
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(AccountCollaboratorListPage.class, AccountCollaboratorListPage.paramsOf(account));
	}

}
