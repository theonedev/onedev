package com.gitplex.server.web.page.account.collaborators;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.gitplex.commons.wicket.component.tabbable.PageTab;
import com.gitplex.commons.wicket.component.tabbable.PageTabLink;
import com.gitplex.commons.wicket.component.tabbable.Tabbable;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.manager.AccountManager;
import com.gitplex.server.core.security.SecurityUtils;
import com.gitplex.server.web.component.AccountLink;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.page.account.AccountLayoutPage;

@SuppressWarnings("serial")
public abstract class CollaboratorPage extends AccountLayoutPage {

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
		add(new AccountLink("collaboratorName", collaboratorModel.getObject()));
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Collaborating Repositories"), CollaboratorDepotListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, CollaboratorDepotListPage.class, 
								CollaboratorDepotListPage.paramsOf(getAccount(), collaboratorModel.getObject()));
					}
					
				};
			}
			
		});
		
		tabs.add(new PageTab(Model.of("Effective Privileges"), 
				CollaboratorEffectivePrivilegePage.class, CollaboratorPrivilegeSourcePage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, CollaboratorEffectivePrivilegePage.class, 
								CollaboratorEffectivePrivilegePage.paramsOf(getAccount(), collaboratorModel.getObject()));
					}
					
				};
			}
			
		});
		add(new Tabbable("collaboratorTabs", tabs));		
	}

	/*
	 * Collaborators page is only visible to administrator as it contains repository 
	 * authorization information and we do not want to expose that information to 
	 * normal users as repository name might also be a secret
	 */
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
