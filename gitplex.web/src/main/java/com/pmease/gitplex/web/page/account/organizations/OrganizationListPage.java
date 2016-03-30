package com.pmease.gitplex.web.page.account.organizations;

import static com.pmease.gitplex.web.component.roleselection.RoleSelectionPanel.ROLE_ADMIN;
import static com.pmease.gitplex.web.component.roleselection.RoleSelectionPanel.ROLE_MEMBER;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.roleselection.RoleSelectionPanel;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class OrganizationListPage extends AccountLayoutPage {

	private PageableListView<OrganizationMembership> organizationsView;
	
	private String filterRole;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer organizationsContainer; 
	
	private WebMarkupContainer noOrganizationsContainer; 
	
	public OrganizationListPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(!getAccount().isOrganization());
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
		
		WebMarkupContainer filterContainer = new WebMarkupContainer("filter");
		filterContainer.setOutputMarkupId(true);
		add(filterContainer);
		
		filterContainer.add(new DropdownLink("selection") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (filterRole == null)
							return "Filter by role";
						else 
							return filterRole;
					}
					
				}));
			}

			@Override
			protected Component newContent(String id) {
				return new RoleSelectionPanel(id, filterRole) {

					@Override
					protected void onSelectAdmin(AjaxRequestTarget target) {
						close();
						filterRole = ROLE_ADMIN;
						target.add(filterContainer);
						target.add(organizationsContainer);
						target.add(pagingNavigator);
						target.add(noOrganizationsContainer);
					}

					@Override
					protected void onSelectOrdinary(AjaxRequestTarget target) {
						close();
						filterRole = ROLE_MEMBER;
						target.add(filterContainer);
						target.add(organizationsContainer);
						target.add(pagingNavigator);
						target.add(noOrganizationsContainer);
					}
					
				};
			}
		});
		filterContainer.add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filterRole = null;
				target.add(filterContainer);
				target.add(organizationsContainer);
				target.add(pagingNavigator);
				target.add(noOrganizationsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(filterRole != null);
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
		
		organizationsContainer.add(organizationsView = new PageableListView<OrganizationMembership>("organizations", 
				new LoadableDetachableModel<List<OrganizationMembership>>() {

			@Override
			protected List<OrganizationMembership> load() {
				List<OrganizationMembership> memberships = new ArrayList<>();
				
				for (OrganizationMembership membership: getAccount().getOrganizations()) {
					if (membership.getOrganization().matches(searchField.getInput())) {
						if (filterRole == null 
								|| filterRole.equals(ROLE_ADMIN) && membership.isAdmin() 
								|| filterRole.equals(ROLE_MEMBER) && !membership.isAdmin()) {
							memberships.add(membership);
						}
					}
				}
				
				memberships.sort((membership1, membership2) 
						-> membership1.getOrganization().compareTo(membership2.getOrganization()));
				return memberships;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<OrganizationMembership> item) {
				OrganizationMembership membership = item.getModelObject();

				item.add(new AvatarLink("avatarLink", membership.getOrganization()));
				item.add(new AccountLink("nameLink", membership.getOrganization()));
						
				item.add(new Label("role", membership.isAdmin()?ROLE_ADMIN:ROLE_MEMBER));
				
				item.add(new BookmarkablePageLink<Void>("setting", 
						ProfileEditPage.class, ProfileEditPage.paramsOf(membership.getOrganization())) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(item.getModelObject().getOrganization()));
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
