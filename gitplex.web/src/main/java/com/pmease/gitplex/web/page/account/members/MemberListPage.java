package com.pmease.gitplex.web.page.account.members;

import static com.pmease.gitplex.web.page.account.members.RoleSelectionPanel.ROLE_ADMIN;
import static com.pmease.gitplex.web.page.account.members.RoleSelectionPanel.ROLE_MEMBER;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
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
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.manager.OrganizationMembershipManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.collaborators.AccountCollaboratorListPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class MemberListPage extends AccountLayoutPage {

	private PageableListView<OrganizationMembership> membersView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer membersContainer; 
	
	private WebMarkupContainer noMembersContainer;
	
	private String filterRole;
	
	private Set<Long> pendingRemovals = new HashSet<>();
	
	public MemberListPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new ClearableTextField<String>("searchMembers", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(membersContainer);
				target.add(pagingNavigator);
				target.add(noMembersContainer);
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
						target.add(membersContainer);
						target.add(pagingNavigator);
						target.add(noMembersContainer);
					}

					@Override
					protected void onSelectOrdinary(AjaxRequestTarget target) {
						close();
						filterRole = ROLE_MEMBER;
						target.add(filterContainer);
						target.add(membersContainer);
						target.add(pagingNavigator);
						target.add(noMembersContainer);
					}
					
				};
			}
		});
		filterContainer.add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filterRole = null;
				target.add(filterContainer);
				target.add(membersContainer);
				target.add(pagingNavigator);
				target.add(noMembersContainer);
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
				setResponsePage(NewMembersPage.class, NewMembersPage.paramsOf(getAccount()));
			}
			
		});
		
		AjaxLink<Void> confirmRemoveLink;
		add(confirmRemoveLink = new AjaxLink<Void>("confirmRemove") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Set<OrganizationMembership> organizationMemberships = new HashSet<>();
				OrganizationMembershipManager organizationMembershipManager = 
						GitPlex.getInstance(OrganizationMembershipManager.class);
				for (Long pendingRemoval: pendingRemovals) {
					OrganizationMembership organizationMembership = organizationMembershipManager.load(pendingRemoval);
					organizationMemberships.add(organizationMembership);
				}
				GitPlex.getInstance(OrganizationMembershipManager.class).delete(organizationMemberships);
				pendingRemovals.clear();
				target.add(this);
				target.add(pagingNavigator);
				target.add(membersContainer);
				target.add(noMembersContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!pendingRemovals.isEmpty());
			}
			
		});
		confirmRemoveLink.setOutputMarkupPlaceholderTag(true);
		
		membersContainer = new WebMarkupContainer("members") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!membersView.getModelObject().isEmpty());
			}
			
		};
		membersContainer.setOutputMarkupPlaceholderTag(true);
		add(membersContainer);
		
		membersContainer.add(membersView = new PageableListView<OrganizationMembership>("members", 
				new LoadableDetachableModel<List<OrganizationMembership>>() {

			@Override
			protected List<OrganizationMembership> load() {
				List<OrganizationMembership> memberships = new ArrayList<>();
				
				for (OrganizationMembership membership: getAccount().getOrganizationMembers()) {
					Account user = membership.getUser();
					if (user.matches(searchField.getInput())) {
						if (filterRole == null 
								|| filterRole.equals(ROLE_ADMIN) && membership.isAdmin() 
								|| filterRole.equals(ROLE_MEMBER) && !membership.isAdmin()) {
							memberships.add(membership);
						}
					}
				}
				
				memberships.sort((membership1, membership2) 
						-> membership1.getUser().compareTo(membership2.getUser()));
				return memberships;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<OrganizationMembership> item) {
				OrganizationMembership membership = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<>("avatarLink", MemberTeamListPage.class, 
						MemberTeamListPage.paramsOf(membership)); 
				link.add(new Avatar("avatar", membership.getUser()));
				item.add(link);
				
				link = new BookmarkablePageLink<>("nameLink", MemberTeamListPage.class, 
						MemberTeamListPage.paramsOf(membership)); 
				link.add(new Label("name", membership.getUser().getDisplayName()));
				item.add(link);
						
				item.add(new DropdownLink("role") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						add(new Label("label", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return membership.isAdmin()?ROLE_ADMIN:ROLE_MEMBER;
							}
							
						}));
						Account user = item.getModelObject().getUser();
						if (!SecurityUtils.canManage(getAccount()) || user.equals(getLoginUser()))
							add(AttributeAppender.append("disabled", "disabled"));
					}

					@Override
					public String getAfterDisabledLink() {
						return null;
					}

					@Override
					public String getBeforeDisabledLink() {
						return null;
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						Account user = item.getModelObject().getUser();
						setEnabled(SecurityUtils.canManage(getAccount()) && !user.equals(getLoginUser()));
					}

					@Override
					protected Component newContent(String id) {
						return new RoleSelectionPanel(id, item.getModelObject().isAdmin()?ROLE_ADMIN:ROLE_MEMBER) {
							
							@Override
							protected void onSelectOrdinary(AjaxRequestTarget target) {
								close();
								membership.setAdmin(false);
								GitPlex.getInstance(OrganizationMembershipManager.class).save(membership);
								target.add(pagingNavigator);
								target.add(membersContainer);
								target.add(noMembersContainer);
								Session.get().success("Role updated");
							}
							
							@Override
							protected void onSelectAdmin(AjaxRequestTarget target) {
								close();
								membership.setAdmin(true);
								GitPlex.getInstance(OrganizationMembershipManager.class).save(membership);
								target.add(pagingNavigator);
								target.add(membersContainer);
								target.add(noMembersContainer);
								Session.get().success("Role updated");
							}
							
						};
					}
					
				});
				item.add(new AjaxLink<Void>("remove") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						Account user = item.getModelObject().getUser();
						setVisible(SecurityUtils.canManage(getAccount()) 
								&& !user.equals(getLoginUser())
								&& !pendingRemovals.contains(item.getModelObject().getId()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						pendingRemovals.add(item.getModelObject().getId());
						target.add(item);
						target.add(confirmRemoveLink);
					}

				});
				item.add(new WebMarkupContainer("pendingRemoval") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(pendingRemovals.contains(item.getModelObject().getId()));
					}
					
				});
				item.add(new AjaxLink<Void>("undoRemove") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						pendingRemovals.remove(item.getModelObject().getId());
						target.add(item);
						target.add(confirmRemoveLink);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(pendingRemovals.contains(item.getModelObject().getId()));
					}
					
				});
				item.setOutputMarkupId(true);
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("pageNav", membersView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(membersView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		noMembersContainer = new WebMarkupContainer("noMembers") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(membersView.getModelObject().isEmpty());
			}
			
		};
		noMembersContainer.setOutputMarkupPlaceholderTag(true);
		add(noMembersContainer);
		
		add(new BookmarkablePageLink<Void>("organizationProfile", 
				ProfileEditPage.class, ProfileEditPage.paramsOf(getAccount())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getAccount().getDefaultPrivilege() != DepotPrivilege.NONE);
				setEnabled(SecurityUtils.canManage(getAccount()));
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("defaultPrivilege", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return "the default <b>" + getAccount().getDefaultPrivilege() + "</b> privilege"; 
					}
					
				}).setEscapeModelStrings(false));
				
				setBeforeDisabledLink("");
				setAfterDisabledLink("");
			}
			
		});
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isMemberOf(getAccount());
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(MemberListPage.class, paramsOf(account));
		else if (SecurityUtils.canManage(account))
			setResponsePage(AccountCollaboratorListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}
	
}
