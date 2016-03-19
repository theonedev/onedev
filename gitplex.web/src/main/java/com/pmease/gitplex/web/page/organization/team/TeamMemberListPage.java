package com.pmease.gitplex.web.page.organization.team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.commons.wicket.component.select2.SelectToAddChoice;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.entity.TeamMembership;
import com.pmease.gitplex.core.manager.OrganizationMembershipManager;
import com.pmease.gitplex.core.manager.TeamMembershipManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.component.EmailLink;
import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.accountchoice.AccountChoiceResourceReference;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class TeamMemberListPage extends TeamPage {

	private static final String ADD_MEMBER_PLACEHOLDER = "Select user to add to team...";
	
	private PageableListView<TeamMembership> membersView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer membersContainer; 
	
	private WebMarkupContainer noMembersContainer;
	
	private Set<Long> pendingRemovals = new HashSet<>();
	
	public TeamMemberListPage(PageParameters params) {
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
		
		AjaxLink<Void> confirmRemoveLink;
		add(confirmRemoveLink = new AjaxLink<Void>("confirmRemove") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				TeamMembershipManager teamMembershipManager = GitPlex.getInstance(TeamMembershipManager.class);
				Collection<TeamMembership> memberships = new ArrayList<>();
				for (Long id: pendingRemovals) {
					memberships.add(teamMembershipManager.load(id));
				}
				GitPlex.getInstance(TeamMembershipManager.class).delete(memberships);
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
		
		add(new SelectToAddChoice<OrganizationMembership>("addNew", new ChoiceProvider<OrganizationMembership>() {

			@Override
			public void query(String term, int page, Response<OrganizationMembership> response) {
				List<OrganizationMembership> memberships = new ArrayList<>();
				term = term.toLowerCase();
				Set<Account> teamMembers = new HashSet<>();
				for (TeamMembership membership: teamModel.getObject().getMemberships()) {
					teamMembers.add(membership.getUser());
				}
				for (OrganizationMembership membership: getAccount().getUserMemberships()) {
					Account user = membership.getUser();
					if (user.matches(term) && !teamMembers.contains(user)) {
						memberships.add(membership);
					}
				}
				
				Collections.sort(memberships, new Comparator<OrganizationMembership>() {

					@Override
					public int compare(OrganizationMembership membership1, OrganizationMembership membership2) {
						return membership1.getUser().getDisplayName()
								.compareTo(membership2.getUser().getDisplayName());
					}
					
				});
				
				new ResponseFiller<OrganizationMembership>(response).fill(memberships, page, Constants.DEFAULT_PAGE_SIZE);
			}

			@Override
			public void toJson(OrganizationMembership choice, JSONWriter writer) throws JSONException {
				String displayName = StringEscapeUtils.escapeHtml4(choice.getUser().getDisplayName()); 
				writer.key("id").value(choice.getId()).key("name").value(displayName);
				String avatarUrl = GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice.getUser());
				writer.key("avatar").value(avatarUrl);
			}

			@Override
			public Collection<OrganizationMembership> toChoices(Collection<String> ids) {
				List<OrganizationMembership> memberships = Lists.newArrayList();
				OrganizationMembershipManager membershipManager = GitPlex.getInstance(OrganizationMembershipManager.class);
				for (String each : ids) {
					Long id = Long.valueOf(each);
					memberships.add(membershipManager.load(id));
				}

				return memberships;
			}

		}, ADD_MEMBER_PLACEHOLDER) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder(ADD_MEMBER_PLACEHOLDER);
				getSettings().setFormatResult("gitplex.accountChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.accountChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.accountChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getAccount()));
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(AccountChoiceResourceReference.INSTANCE));
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, OrganizationMembership selection) {
				TeamMembership membership = new TeamMembership();
				membership.setTeam(teamModel.getObject());
				membership.setUser(selection.getUser());
				GitPlex.getInstance(TeamMembershipManager.class).persist(membership);
				target.add(membersContainer);
				target.add(pagingNavigator);
				target.add(noMembersContainer);
			}
			
		});
		
		membersContainer = new WebMarkupContainer("members") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!membersView.getModelObject().isEmpty());
			}
			
		};
		membersContainer.setOutputMarkupPlaceholderTag(true);
		add(membersContainer);
		
		membersContainer.add(membersView = new PageableListView<TeamMembership>("members", 
				new LoadableDetachableModel<List<TeamMembership>>() {

			@Override
			protected List<TeamMembership> load() {
				List<TeamMembership> memberships = new ArrayList<>();
				
				String searchInput = searchField.getInput();
				if (searchInput != null)
					searchInput = searchInput.toLowerCase().trim();
				else
					searchInput = "";
				
				for (TeamMembership membership: teamModel.getObject().getMemberships()) {
					if (membership.getUser().matches(searchInput)) {
						memberships.add(membership);
					}
				}
				
				Collections.sort(memberships, new Comparator<TeamMembership>() {

					@Override
					public int compare(TeamMembership membership1, TeamMembership membership2) {
						return membership1.getUser().getDisplayName()
								.compareTo(membership2.getUser().getDisplayName());
					}
					
				});
				return memberships;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<TeamMembership> item) {
				Account member = item.getModelObject().getUser();

				item.add(new Avatar("avatar", member));
				item.add(new UserLink("link", member));
						
				item.add(new EmailLink("email", Model.of(member.getEmail())));
				item.add(new AjaxLink<Void>("remove") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(SecurityUtils.canManage(getAccount()) 
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
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccess(getAccount());
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(TeamListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}
	
}
