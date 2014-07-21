package com.pmease.gitplex.web.page.account.setting.teams;

import java.util.List;

import com.pmease.gitplex.core.GitPlex;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;
import com.pmease.gitplex.web.common.wicket.bootstrap.Icon;
import com.pmease.gitplex.web.common.wicket.component.datagrid.DataGrid;
import com.pmease.gitplex.web.common.wicket.component.datagrid.hibernate.EntityListProvider;
import com.pmease.gitplex.web.model.TeamModel;
import com.pmease.gitplex.web.page.account.setting.AccountSettingPage;
import com.pmease.gitplex.web.util.EnumUtils;

@SuppressWarnings("serial")
public class AccountTeamsPage extends AccountSettingPage {

	public AccountTeamsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Teams - " + getAccount();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("addTeam", AddTeamPage.class, paramsOf(getAccount())));
		add(createTeamTable());
	}
	
	private String getPermissionMessage(GeneralOperation operation) {
		switch (operation) {
		case NO_ACCESS:
			return "can NOT access";
			
		case READ:
			return "can pull and browse";
			
		case WRITE:
			return "can pull and push";
			
		case ADMIN:
			return "can pull, push and admin";
			
		default:
			throw new IllegalArgumentException(operation.name());
		}
	}
	
	private String formatTeamDescription(Team team) {
		TeamManager tm = GitPlex.getInstance(TeamManager.class);
		GeneralOperation operation = tm.getActualAuthorizedOperation(team);
		if (team.isAnonymous()) {
			return String.format("Users %s any repositories within this account without logging in",
					getPermissionMessage(operation)); 
		} else if (team.isLoggedIn()) {
			return String.format("Logged-in users %s any repositories within this account",
					getPermissionMessage(operation));
		} else if (team.isOwners()) {
			return team.getMemberships().size() + " members have full access to all repositories and this account";
		} else {
			return String.format(team.getMemberships().size() + " members %s any repositories within this account",
					getPermissionMessage(operation));
		}
	}
	
	private Component createTeamTable() {
		List<IColumn<Team, String>> columns = Lists.newArrayList();
		IColumn<Team, String> column = new AbstractColumn<Team, String>(Model.of("Team")) {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem,
					String componentId, IModel<Team> rowModel) {
				Team team = rowModel.getObject();
				Fragment frag = new Fragment(componentId, "namefrag", AccountTeamsPage.this);
				AbstractLink link = new BookmarkablePageLink<Void>("link", 
																	EditTeamPage.class,
																	EditTeamPage.newParams(team));
				link.add(new Label("name", Model.of(team.getName())));
				link.add(new Icon("icon", "icon-pencil"));
				frag.add(link);
				frag.add(new Label("summary", formatTeamDescription(team)));
				frag.add(new Label("builtin", "built-in").setVisibilityAllowed(team.isBuiltIn()));
				cellItem.add(frag);
			}
		};
		
		columns.add(column);
		
		columns.add(new PermissionColumn(GeneralOperation.READ));
		columns.add(new PermissionColumn(GeneralOperation.WRITE));
		columns.add(new PermissionColumn(GeneralOperation.ADMIN));
		
		column = new HeaderlessColumn<Team, String>() {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem,
					String componentId, IModel<Team> rowModel) {
				Team team = rowModel.getObject();
				Fragment frag = new Fragment(componentId, "ops", AccountTeamsPage.this);
				AbstractLink link = new AjaxLink<Team>("removelink", new TeamModel(team)) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Team p = (Team) this.getDefaultModelObject();
						GitPlex.getInstance(Dao.class).remove(p);
						target.add(AccountTeamsPage.this.get("teams"));
					}
				};
				link.add(new ConfirmBehavior("Are you sure you want to remove team <b>" + team.getName() + "</b>?"));
				
				frag.add(link);
				frag.setVisibilityAllowed(!team.isBuiltIn());
				cellItem.add(frag);
			}
			
			@Override
			public String getCssClass() {
				return "operations";
			}
		};
		
		columns.add(column);
		
		IModel<List<Team>> teamsModel = new LoadableDetachableModel<List<Team>>() {

			@Override
			protected List<Team> load() {
				Dao dao = GitPlex.getInstance(Dao.class);
				return dao.query(EntityCriteria.of(Team.class)
						.add(Restrictions.eq("owner", getAccount()))
						.addOrder(Order.asc("id")));
			}
			
		};
		
		EntityListProvider<Team> provider = new EntityListProvider<Team>(teamsModel);
		DataGrid<Team> table = new DataGrid<Team>("teams", columns, provider, Integer.MAX_VALUE);
		table.addBottomToolbar(new NoRecordsToolbar(table, Model.of("No Teams Found")));
		table.addTopToolbar(new HeadersToolbar<String>(table, provider));
		
		return table;
	}
	
	private GeneralOperation getTeamPermission(Team team) {
		TeamManager tm = GitPlex.getInstance(TeamManager.class);
		return tm.getActualAuthorizedOperation(team);
	}
	
	private class PermissionColumn extends AbstractColumn<Team, String> {

		final GeneralOperation operation;
		
		public PermissionColumn(GeneralOperation operation) {
			super(Model.of(operation.toString()));
			this.operation = operation;
		}

		@Override
		public void populateItem(Item<ICellPopulator<Team>> cellItem,
				String componentId, IModel<Team> rowModel) {
			Fragment frag = new Fragment(componentId, "permissionop", AccountTeamsPage.this);
			Team team = rowModel.getObject();
			boolean displayed = true;
			if (team.isAnonymous()) {
				displayed = operation == GeneralOperation.READ;
			} else if (team.isLoggedIn()) {
				displayed = operation != GeneralOperation.ADMIN;
			}
			
			if (!displayed) {
				cellItem.add(new WebMarkupContainer(componentId).setVisibilityAllowed(false));
				return;
			}

			boolean enabled = true;
			
			if (team.isOwners()) {
				enabled = false; // cannot edit owners team permission
			} else if (team.isAnonymous() || operation == GeneralOperation.ADMIN) {
				enabled = true; // can always edit anonymous and admin column
			} else {
				TeamManager tm = GitPlex.getInstance(TeamManager.class);
				if (operation == GeneralOperation.READ) {
					GeneralOperation op = tm.getAnonymous(getAccount()).getAuthorizedOperation();
					if (op == operation) {
						// READ
						enabled = false;
					} else {
						op = tm.getLoggedIn(getAccount()).getAuthorizedOperation();
						enabled = team.isLoggedIn() ? true : !op.can(GeneralOperation.READ);
					}
				} else if (operation == GeneralOperation.WRITE) {
					GeneralOperation op = tm.getLoggedIn(getAccount()).getAuthorizedOperation();
					enabled = team.isLoggedIn() ? true : !op.can(GeneralOperation.WRITE);
				}
			}
			
			final Long teamId = team.getId();
			AbstractLink link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					Team team = GitPlex.getInstance(Dao.class).load(Team.class, teamId);
					GeneralOperation permission = team.getAuthorizedOperation();
					if (Objects.equal(operation, permission)
							|| operation.ordinal() < permission.ordinal()) {
						team.setAuthorizedOperation(EnumUtils.dec(operation));
					} else {
						team.setAuthorizedOperation(operation);
					}
					
					GitPlex.getInstance(Dao.class).persist(team);
					target.add(AccountTeamsPage.this.get("teams"));
				}
			};
			
			link.setEnabled(enabled);
			link.add(new Icon("icon", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					Team team = GitPlex.getInstance(Dao.class).load(Team.class, teamId);
					return getTeamPermission(team).can(operation) ?
							"icon-checkbox-checked" : "icon-checkbox-unchecked";
				}
				
			}));
			frag.add(link);
			cellItem.add(frag);
		}
		
		@Override
		public String getCssClass() {
			return "permission-col";
		}
	}
	
	@Deprecated
	Component newTeamsView() {
		final WebMarkupContainer teamsDiv = new WebMarkupContainer("teams");
		teamsDiv.setOutputMarkupId(true);
		
		IModel<List<Team>> teamsModel = new LoadableDetachableModel<List<Team>>() {

			@Override
			protected List<Team> load() {
				List<Team> teams = Lists.newArrayList(getAccount().getTeams());
				return teams;
			}
		};
		
		ListView<Team> view = new ListView<Team>("team", teamsModel) {

			@Override
			protected void populateItem(ListItem<Team> item) {
				Team team = item.getModelObject();
				AbstractLink link = new BookmarkablePageLink<Void>("link", 
						EditTeamPage.class, 
						EditTeamPage.newParams(team));
				link.add(new Label("name", Model.of(team.getName())));
				item.add(link);
				item.add(new AjaxLink<Team>("removelink", new TeamModel(team)) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Team p = (Team) this.getDefaultModelObject();
						GitPlex.getInstance(Dao.class).remove(p);
						target.add(teamsDiv);
					}
				}.add(new ConfirmBehavior("Are you sure you want to remove team <b>" + team.getName() + "</b>?")));
			}
		};
		teamsDiv.add(view);
		return teamsDiv;
	}
}
