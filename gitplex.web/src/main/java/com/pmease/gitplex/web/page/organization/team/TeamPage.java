package com.pmease.gitplex.web.page.organization.team;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.StaleObjectStateException;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.ConfirmOnClick;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.PageTabLink;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.component.Team;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;

@SuppressWarnings("serial")
public abstract class TeamPage extends AccountLayoutPage {

	private static final String PARAM_TEAM = "team";

	protected final Team team;
	
	public TeamPage(PageParameters params) {
		super(params);

		String teamName = params.get(PARAM_TEAM).toString();
		team = Preconditions.checkNotNull(getAccount().getTeams().get(teamName));
		Preconditions.checkState(getAccount().isOrganization());
	}

	public static PageParameters paramsOf(Account organization, Team team) {
		PageParameters params = paramsOf(organization);
		params.set(PARAM_TEAM, team.getName());
		return params;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("teamName", team.getName()));
		if (team.getDescription() != null) {
			add(new MarkdownViewer("teamDescription", Model.of(team.getDescription()), false));
		} else {
			add(new Label("teamDescription", "<i>No description</i>").setEscapeModelStrings(false));
		}
		add(new Link<Void>("editTeam") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(SecurityUtils.canManage(getAccount()));
			}
			
			@Override
			public void onClick() {
				Account organization = getAccount();
				setResponsePage(TeamEditPage.class, TeamEditPage.paramsOf(organization, team));
			}
			
		});
		add(new Link<Void>("deleteTeam") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(SecurityUtils.canManage(getAccount()));
			}
			
			@Override
			public void onClick() {
				Account organization = getAccount();
				if (organization.getVersion() != accountVersion) {
					throw new StaleObjectStateException(Account.class.getName(), organization.getId());
				}
				GitPlex.getInstance(TeamManager.class).delete(organization, team.getName());
				setResponsePage(TeamListPage.class, TeamListPage.paramsOf(getAccount()));
			}

		}.add(new ConfirmOnClick("Do you really want to delete this team?")));
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Members"), TeamMemberListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, TeamMemberListPage.class, 
								TeamMemberListPage.paramsOf(getAccount(), team));
					}
					
				};
			}
			
		});
		tabs.add(new PageTab(Model.of("Repositories"), TeamDepotListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, TeamDepotListPage.class, 
								TeamDepotListPage.paramsOf(getAccount(), team));
					}
					
				};
			}
			
		});
		add(new Tabbable("teamTabs", tabs));
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
