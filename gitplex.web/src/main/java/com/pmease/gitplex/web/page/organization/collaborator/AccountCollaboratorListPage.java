package com.pmease.gitplex.web.page.organization.collaborator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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

import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.entity.UserAuthorization;
import com.pmease.gitplex.core.manager.UserAuthorizationManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class AccountCollaboratorListPage extends AccountLayoutPage {

	private PageableListView<Account> collaboratorsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer collaboratorsContainer; 
	
	private WebMarkupContainer noCollaboratorsContainer;
	
	public AccountCollaboratorListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Outside Collaborators - " + getAccount();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new ClearableTextField<String>("searchCollaborators", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
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
				List<Account> collaborators = new ArrayList<>();

				Collection<Account> members = new HashSet<>();
				for (OrganizationMembership membership: getAccount().getOrganizationMembers()) {
					members.add(membership.getUser());
				}
				UserAuthorizationManager userAuthorizationManager = 
						GitPlex.getInstance(UserAuthorizationManager.class);
				for (UserAuthorization authorization: userAuthorizationManager.query(getAccount())) {
					if (authorization.getUser().matches(searchField.getInput()) 
							&& !members.contains(authorization.getUser())) {
						collaborators.add(authorization.getUser());
					}
				}
				
				Collections.sort(collaborators, new Comparator<Account>() {

					@Override
					public int compare(Account collaborator1, Account collaborator2) {
						return collaborator1.getDisplayName().compareTo(collaborator2.getDisplayName());
					}
					
				});
				return collaborators;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Account> item) {
				Account collaborator = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<>("avatarLink", 
						CollaboratorPage.class, 
						CollaboratorPage.paramsOf(getAccount(), collaborator)); 
				link.add(new Avatar("avatar", collaborator));
				item.add(link);
				
				link = new BookmarkablePageLink<>("nameLink", 
						CollaboratorPage.class, 
						CollaboratorPage.paramsOf(getAccount(), collaborator)); 
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
		
		add(new WebMarkupContainer("outsideTip").setVisible(getAccount().isOrganization()));
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isMemberOf(getAccount());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(AccountCollaboratorListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}
	
}
