package com.pmease.gitplex.web.page.account.teams;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.ConfirmOnClick;
import com.pmease.commons.wicket.component.markdown.MarkdownPanel;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.PageTabLink;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public abstract class TeamPage extends AccountLayoutPage {

	private static final String PARAM_TEAM = "team";

	protected final IModel<Team> teamModel;
	
	public TeamPage(PageParameters params) {
		super(params);

		String teamName = params.get(PARAM_TEAM).toString();
		teamModel = new LoadableDetachableModel<Team>() {

			@Override
			protected Team load() {
				TeamManager teamManager = GitPlex.getInstance(TeamManager.class);
				return Preconditions.checkNotNull(teamManager.find(getAccount(), teamName));
			}
			
		};
		Preconditions.checkState(getAccount().isOrganization());
	}

	public static PageParameters paramsOf(Team team) {
		PageParameters params = paramsOf(team.getOrganization());
		params.set(PARAM_TEAM, team.getName());
		return params;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("teamName", teamModel.getObject().getName()));
		if (teamModel.getObject().getDescription() != null) {
			add(new MarkdownPanel("teamDescription", Model.of(teamModel.getObject().getDescription()), null));
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
				setResponsePage(TeamEditPage.class, TeamEditPage.paramsOf(teamModel.getObject()));
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
				GitPlex.getInstance(TeamManager.class).delete(teamModel.getObject());
				setResponsePage(TeamListPage.class, TeamListPage.paramsOf(getAccount()));
			}

		}.add(new ConfirmOnClick("Do you really want to delete this team?")));
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Team Members"), TeamMemberListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, TeamMemberListPage.class, 
								TeamMemberListPage.paramsOf(teamModel.getObject()));
					}
					
				};
			}
			
		});
		tabs.add(new PageTab(Model.of("Repository Authorizations"), TeamDepotListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, TeamDepotListPage.class, 
								TeamDepotListPage.paramsOf(teamModel.getObject()));
					}
					
				};
			}
			
		});

		add(new Tabbable("teamTabs", tabs).setVisible(SecurityUtils.canManage(getAccount())));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccess(getAccount());
	}
	
	@Override
	protected void onDetach() {
		teamModel.detach();
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(TeamListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

}
