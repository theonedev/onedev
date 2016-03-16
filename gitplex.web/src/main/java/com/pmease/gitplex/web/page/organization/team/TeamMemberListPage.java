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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.StaleObjectStateException;
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
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.manager.MembershipManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.component.UserLink;
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
	
	private PageableListView<Account> membersView;
	
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
				Collection<Membership> memberships = new ArrayList<>();
				for (Membership membership: getAccount().getUserMemberships()) {
					if (pendingRemovals.contains(membership.getUser().getId())) {
						membership.getJoinedTeams().remove(team.getName());
						memberships.add(membership);
					}
				}
				GitPlex.getInstance(MembershipManager.class).save(memberships);
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
		
		add(new SelectToAddChoice<Membership>("addNew", new ChoiceProvider<Membership>() {

			@Override
			public void query(String term, int page, Response<Membership> response) {
				List<Membership> memberships = new ArrayList<>();
				term = term.toLowerCase();
				for (Membership membership: getAccount().getUserMemberships()) {
					Account user = membership.getUser();
					if (user.matches(term) && !user.getTeams().containsKey(team.getName())) {
						memberships.add(membership);
					}
				}
				
				Collections.sort(memberships, new Comparator<Membership>() {

					@Override
					public int compare(Membership membership1, Membership membership2) {
						return membership1.getUser().getDisplayName()
								.compareTo(membership2.getUser().getDisplayName());
					}
					
				});
				
				new ResponseFiller<Membership>(response).fill(memberships, page, Constants.DEFAULT_PAGE_SIZE);
			}

			@Override
			public void toJson(Membership choice, JSONWriter writer) throws JSONException {
				String displayName = StringEscapeUtils.escapeHtml4(choice.getUser().getDisplayName()); 
				writer.key("id").value(choice.getId()).key("name").value(displayName);
				String avatarUrl = GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice.getUser());
				writer.key("avatar").value(avatarUrl);
			}

			@Override
			public Collection<Membership> toChoices(Collection<String> ids) {
				List<Membership> memberships = Lists.newArrayList();
				MembershipManager membershipManager = GitPlex.getInstance(MembershipManager.class);
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
			protected void onSelect(AjaxRequestTarget target, Membership selection) {
				// with below check, it is impossible to add users to a team that has just been 
				// deleted
				if (accountVersion != selection.getOrganization().getVersion()) {
					throw new StaleObjectStateException(Membership.class.getName(), selection.getId());
				}
				selection.getJoinedTeams().add(team.getName());
				GitPlex.getInstance(MembershipManager.class).save(selection);
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
		
		membersContainer.add(membersView = new PageableListView<Account>("members", 
				new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				List<Account> members = new ArrayList<>();
				
				String searchInput = searchField.getInput();
				if (searchInput != null)
					searchInput = searchInput.toLowerCase().trim();
				else
					searchInput = "";
				
				for (Membership membership: getAccount().getUserMemberships()) {
					Account user = membership.getUser();
					if (membership.getJoinedTeams().contains(team.getName()) && user.matches(searchInput)) {
						members.add(user);
					}
				}
				
				Collections.sort(members, new Comparator<Account>() {

					@Override
					public int compare(Account member1, Account member2) {
						return member1.getDisplayName().compareTo(member2.getDisplayName());
					}
					
				});
				return members;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Account> item) {
				Account member = item.getModelObject();

				item.add(new Avatar("avatar", member));
				
				Link<Void> link = new UserLink("link", member); 
				link.add(new Label("name", member.getDisplayName()));
				item.add(link);
						
				item.add(new Label("email", member.getEmail()));
				
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
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(TeamListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}
	
}
