package com.pmease.gitplex.web.page.organization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.manager.MembershipManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class MemberListPage extends AccountLayoutPage {

	private static final String ROLE_ADMIN = "Admin";
	
	private static final String ROLE_MEMBER = "Member";
	
	private PageableListView<Membership> membersView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer membersContainer; 
	
	private String role;
	
	private Set<Long> pendingRemovals = new HashSet<>();
	
	public MemberListPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
	}

	@Override
	protected String getPageTitle() {
		return "Members - " + getAccount();
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
						if (role == null)
							return "Filter by role";
						else 
							return WordUtils.capitalize(role);
					}
					
				}));
			}

			@Override
			protected Component newContent(String id) {
				return new RoleSelectionPanel(id) {

					@Override
					protected void onSelectAdmin(AjaxRequestTarget target) {
						close();
						role = ROLE_ADMIN;
						target.add(filterContainer);
						target.add(membersContainer);
						target.add(pagingNavigator);
					}

					@Override
					protected void onSelectMember(AjaxRequestTarget target) {
						close();
						role = ROLE_MEMBER;
						target.add(filterContainer);
						target.add(membersContainer);
						target.add(pagingNavigator);
					}
					
				};
			}
		});
		filterContainer.add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				role = null;
				target.add(filterContainer);
				target.add(membersContainer);
				target.add(pagingNavigator);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(role != null);
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
				Set<Membership> membershipsToRemove = new HashSet<>();
				for (Membership membership: getAccount().getUserMemberships()) {
					if (pendingRemovals.contains(membership.getId()))
						membershipsToRemove.add(membership);
				}
				GitPlex.getInstance(MembershipManager.class).delete(membershipsToRemove);
				pendingRemovals.clear();
				target.add(this);
				target.add(pagingNavigator);
				target.add(membersContainer);
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
		
		membersContainer.add(membersView = new PageableListView<Membership>("members", 
				new LoadableDetachableModel<List<Membership>>() {

			@Override
			protected List<Membership> load() {
				List<Membership> memberships = new ArrayList<>();
				
				String searchInput = searchField.getInput();
				if (searchInput != null)
					searchInput = searchInput.toLowerCase().trim();
				else
					searchInput = "";
				
				for (Membership membership: getAccount().getUserMemberships()) {
					Account user = membership.getUser();
					String fullName = user.getFullName();
					if (fullName == null)
						fullName = "";
					else
						fullName = fullName.toLowerCase();
					if ((user.getName().toLowerCase().contains(searchInput) || fullName.contains(searchInput))) {
						if (role == null 
								|| role.equals(ROLE_ADMIN) && membership.isAdmin() 
								|| role.equals(ROLE_MEMBER) && !membership.isAdmin()) {
							memberships.add(membership);
						}
					}
				}
				
				Collections.sort(memberships, new Comparator<Membership>() {

					@Override
					public int compare(Membership membership1, Membership membership2) {
						return membership1.getUser().getName().compareTo(membership2.getUser().getName());
					}
					
				});
				return memberships;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Membership> item) {
				Membership membership = item.getModelObject();

				item.add(new Avatar("avatar", membership.getUser()));
				
				Link<Void> link = new BookmarkablePageLink<>("link", MembershipPage.class, 
						MembershipPage.paramsOf(membership)); 
				link.add(new Label("name", membership.getUser().getName()));
				item.add(link);
						
				item.add(new Label("fullName", membership.getUser().getFullName()));
				
				item.add(new DropdownLink("role") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						add(new Label("label", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								if (membership.isAdmin())
									return ROLE_ADMIN;
								else
									return ROLE_MEMBER;
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
						return new RoleSelectionPanel(id) {
							
							@Override
							protected void onSelectMember(AjaxRequestTarget target) {
								close();
								Membership membership = item.getModelObject();
								membership.setAdmin(false);
								GitPlex.getInstance(MembershipManager.class).save(membership);
								target.add(pagingNavigator);
								target.add(membersContainer);
							}
							
							@Override
							protected void onSelectAdmin(AjaxRequestTarget target) {
								close();
								Membership membership = item.getModelObject();
								membership.setAdmin(true);
								GitPlex.getInstance(MembershipManager.class).save(membership);
								target.add(pagingNavigator);
								target.add(membersContainer);
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
								&& pendingRemovals.contains(item.getModelObject().getId()));
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

				item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (pendingRemovals.contains(item.getModelObject().getId()))
							return "pending-removal";
						else
							return "";
					}
					
				}));
				item.setOutputMarkupId(true);
			}
			
		});

		add(pagingNavigator = new BootstrapPagingNavigator("pageNav", membersView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(membersView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				MemberListPage.class, "organization.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(MemberListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}
	
}
