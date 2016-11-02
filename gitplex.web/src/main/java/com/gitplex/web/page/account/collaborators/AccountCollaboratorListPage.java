package com.gitplex.web.page.account.collaborators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.UserAuthorization;
import com.gitplex.core.manager.UserAuthorizationManager;
import com.gitplex.core.security.SecurityUtils;
import com.gitplex.web.Constants;
import com.gitplex.web.component.avatar.Avatar;
import com.gitplex.web.page.account.AccountLayoutPage;
import com.gitplex.web.page.account.overview.AccountOverviewPage;
import com.gitplex.commons.wicket.behavior.OnTypingDoneBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class AccountCollaboratorListPage extends AccountLayoutPage {

	private PageableListView<Account> collaboratorsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer collaboratorsContainer; 
	
	private WebMarkupContainer noCollaboratorsContainer;
	
	private String searchInput;
	
	public AccountCollaboratorListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("searchCollaborators", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(collaboratorsContainer);
				target.add(pagingNavigator);
				target.add(noCollaboratorsContainer);
			}
			
		});
		
		collaboratorsContainer = new WebMarkupContainer("collaborators") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!collaboratorsView.getModelObject().isEmpty());
			}
			
		};
		collaboratorsContainer.setOutputMarkupPlaceholderTag(true);
		add(collaboratorsContainer);
		
		collaboratorsContainer.add(collaboratorsView = new PageableListView<Account>("collaborators", 
				new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				Set<Account> setOfCollaborators = new HashSet<>();

				UserAuthorizationManager userAuthorizationManager = 
						GitPlex.getInstance(UserAuthorizationManager.class);
				for (UserAuthorization authorization: userAuthorizationManager.findAll(getAccount())) {
					if (authorization.getUser().matches(searchInput)) {
						setOfCollaborators.add(authorization.getUser());
					}
				}

				List<Account> listOfCollaborators = new ArrayList<>(setOfCollaborators);
				Collections.sort(listOfCollaborators);
				return listOfCollaborators;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Account> item) {
				Account collaborator = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<>("avatarLink", 
						CollaboratorDepotListPage.class, 
						CollaboratorDepotListPage.paramsOf(getAccount(), collaborator)); 
				link.add(new Avatar("avatar", collaborator));
				item.add(link);
				
				link = new BookmarkablePageLink<>("nameLink", 
						CollaboratorDepotListPage.class, 
						CollaboratorDepotListPage.paramsOf(getAccount(), collaborator)); 
				link.add(new Label("name", collaborator.getDisplayName()));
				item.add(link);
						
				item.setOutputMarkupId(true);
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("pageNav", collaboratorsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(collaboratorsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		noCollaboratorsContainer = new WebMarkupContainer("noCollaborators") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(collaboratorsView.getModelObject().isEmpty());
			}
			
		};
		noCollaboratorsContainer.setOutputMarkupPlaceholderTag(true);
		add(noCollaboratorsContainer);
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
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(AccountCollaboratorListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}
	
}
