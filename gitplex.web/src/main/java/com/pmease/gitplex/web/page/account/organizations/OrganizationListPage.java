package com.pmease.gitplex.web.page.account.organizations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteAccountModal;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class OrganizationListPage extends AccountLayoutPage {

	private PageableListView<Account> organizationsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer organizationsContainer; 
	
	private WebMarkupContainer noOrganizationsContainer; 
	
	public OrganizationListPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(!getAccount().isOrganization());
	}

	@Override
	protected String getPageTitle() {
		return "Organizations - " + getAccount();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new ClearableTextField<String>("searchOrganizations", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(organizationsContainer);
				target.add(noOrganizationsContainer);
				target.add(pagingNavigator);
			}
			
		});
		
		add(new Link<Void>("addNew") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getAccount()));
			}

			@Override
			public void onClick() {
				setResponsePage(NewOrganizationPage.class, NewOrganizationPage.paramsOf(getAccount()));
			}
			
		});
		
		organizationsContainer = new WebMarkupContainer("organizations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!organizationsView.getModelObject().isEmpty());
			}
			
		};
		organizationsContainer.setOutputMarkupPlaceholderTag(true);
		add(organizationsContainer);
		
		noOrganizationsContainer = new WebMarkupContainer("noOrganizations") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(organizationsView.getModelObject().isEmpty());
			}
			
		};
		noOrganizationsContainer.setOutputMarkupPlaceholderTag(true);
		add(noOrganizationsContainer);
		
		organizationsContainer.add(organizationsView = new PageableListView<Account>("organizations", new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				List<Account> organizations = new ArrayList<>();
				
				for (OrganizationMembership membership: getAccount().getOrganizations()) {
					Account organization = membership.getOrganization();
					if (organization.matches(searchField.getInput()) 
							&& (getAccount().equals(getLoginUser()) || membership.isAdmin())) {
						organizations.add(organization);
					}
				}
				
				Collections.sort(organizations, new Comparator<Account>() {

					@Override
					public int compare(Account account1, Account account2) {
						return account1.getName().compareTo(account2.getName());
					}
					
				});
				return organizations;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Account> item) {
				Account organization = item.getModelObject();

				item.add(new Avatar("avatar", organization));
				
				Link<Void> link = new BookmarkablePageLink<>("link", AccountOverviewPage.class, 
						AccountOverviewPage.paramsOf(organization)); 
				link.add(new Label("name", organization.getName()));
				item.add(link);
						
				item.add(new Label("fullName", organization.getFullName()));
				String role = "Not member";
				for (OrganizationMembership membership: getAccount().getOrganizations()) {
					if (membership.getOrganization().equals(organization)) {
						if (membership.isAdmin())
							role = "Admin";
						else
							role = "Member";
						break;
					}
				}
				item.add(new Label("role", role));
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(SecurityUtils.canManage(item.getModelObject()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						new ConfirmDeleteAccountModal(target) {

							@Override
							protected void onDeleted(AjaxRequestTarget target) {
								setResponsePage(OrganizationListPage.this);
							}

							@Override
							protected Account getAccount() {
								return item.getModelObject();
							}
							
						};
					}
					
				});
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("pageNav", organizationsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(organizationsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
		else
			setResponsePage(OrganizationListPage.class, paramsOf(account));
	}

}
