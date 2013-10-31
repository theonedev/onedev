package com.pmease.gitop.web.page.account.setting.teams;

import java.util.List;

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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.bootstrap.Icon;
import com.pmease.gitop.web.common.bootstrap.IconType;
import com.pmease.gitop.web.common.component.datagrid.DataGrid;
import com.pmease.gitop.web.common.component.datagrid.hibernate.EntityListProvider;
import com.pmease.gitop.web.common.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.model.TeamModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;
import com.pmease.gitop.web.util.EnumUtils;

@SuppressWarnings("serial")
public class AccountTeamsPage extends AccountSettingPage {

//	public static PageParameters newParams(User account) {
//		return PageSpec.forUser(account);
//	}
	
	@Override
	protected Category getSettingCategory() {
		return Category.TEAMS;
	}

	@Override
	protected String getPageTitle() {
		return "Teams - " + getAccount();
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new BookmarkablePageLink<Void>("addTeam", AddTeamPage.class));
//		add(newTeamsView());
		
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
	
	@SuppressWarnings("unused")
	private String formatTeamDescription(Team team) {
		if (team.isAnonymousTeam()) {
			return String.format("Users %s any repositories within this account without logging in",
					getPermissionMessage(team.getAuthorizedOperation())); 
		} else if (team.isLoggedInTeam()) {
			return String.format("Logged-in users %s any repositories within this account",
					getPermissionMessage(team.getAuthorizedOperation()));
		} else if (team.isOwnersTeam()) {
			return (team.getMemberships().size() + 1) + " members have full access to all repositories and this account";
		} else {
			return String.format(team.getMemberships().size() + " members can %s any repositories within this account",
					getPermissionMessage(team.getAuthorizedOperation()));
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
				link.add(new Icon("icon", IconType.PENCIL));
				frag.add(link);
//				frag.add(new Label("description", formatTeamDescription(team)));
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
				AbstractLink link = new AjaxConfirmLink<Team>("removelink",
						new TeamModel(team),
						Model.of("Are you sure you want to remove team <b>" + team.getName() + "</b>?")) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Team p = (Team) this.getDefaultModelObject();
						Gitop.getInstance(TeamManager.class).delete(p);
						target.add(AccountTeamsPage.this.get("teams"));
					}
				};
				frag.add(link);
				frag.setVisibilityAllowed(!team.isBuiltIn());
				cellItem.add(frag);
			}
			
			@Override
			public String getCssClass() {
				return "operations v-middle";
			}
		};
		
		columns.add(column);
		
		IModel<List<Team>> teamsModel = new LoadableDetachableModel<List<Team>>() {

			@Override
			protected List<Team> load() {
				TeamManager tm = Gitop.getInstance(TeamManager.class);
				return tm.query(
						new Criterion[] {Restrictions.eq("owner", getAccount())}, 
						new Order[] { Order.asc("id")}, 
						0, 
						Integer.MAX_VALUE);
			}
			
		};
		
		EntityListProvider<Team> provider = new EntityListProvider<Team>(teamsModel);
		DataGrid<Team> table = new DataGrid<Team>("teams", columns, provider, Integer.MAX_VALUE);
		table.addBottomToolbar(new NoRecordsToolbar(table, Model.of("No Teams Found")));
		table.addTopToolbar(new HeadersToolbar<String>(table, provider));
		
		return table;
	}
	
	private GeneralOperation getTeamPermission(Team team) {
		TeamManager tm = Gitop.getInstance(TeamManager.class);
		return tm.getTeamPermission(team);
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
			if (team.isAnonymousTeam()) {
				displayed = operation == GeneralOperation.READ;
			} else if (team.isLoggedInTeam()) {
				displayed = operation != GeneralOperation.ADMIN;
			}
			
			if (!displayed) {
				cellItem.add(new WebMarkupContainer(componentId).setVisibilityAllowed(false));
				return;
			}

			boolean enabled = true;
			
			if (team.isOwnersTeam()) {
				enabled = false; // cannot edit owners team permission
			} else if (team.isAnonymousTeam() || operation == GeneralOperation.ADMIN) {
				enabled = true; // can always edit anonymous and admin column
			} else {
				TeamManager tm = Gitop.getInstance(TeamManager.class);
				if (operation == GeneralOperation.READ) {
					GeneralOperation op = tm.getAnonymousTeam(getAccount()).getAuthorizedOperation();
					if (op == operation) {
						// READ
						enabled = false;
					} else {
						op = tm.getLoggedInTeam(getAccount()).getAuthorizedOperation();
						enabled = team.isLoggedInTeam() ? true : !op.can(GeneralOperation.READ);
					}
				} else if (operation == GeneralOperation.WRITE) {
					GeneralOperation op = tm.getLoggedInTeam(getAccount()).getAuthorizedOperation();
					enabled = team.isLoggedInTeam() ? true : !op.can(GeneralOperation.WRITE);
				}
			}
			
			final Long teamId = team.getId();
			AbstractLink link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					Team team = Gitop.getInstance(TeamManager.class).get(teamId);
					GeneralOperation permission = team.getAuthorizedOperation();
					if (Objects.equal(operation, permission)
							|| operation.ordinal() < permission.ordinal()) {
						team.setAuthorizedOperation(EnumUtils.dec(operation));
					} else {
						team.setAuthorizedOperation(operation);
					}
					
					Gitop.getInstance(TeamManager.class).save(team);
					target.add(AccountTeamsPage.this.get("teams"));
				}
			};
			
			link.setEnabled(enabled);
			link.add(new Icon("icon", new AbstractReadOnlyModel<IconType>() {

				@Override
				public IconType getObject() {
					Team team = Gitop.getInstance(TeamManager.class).get(teamId);
					return getTeamPermission(team).can(operation) ?
							IconType.CHECK : IconType.UNCHECKED;
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
				item.add(new AjaxConfirmLink<Team>("removelink",
						new TeamModel(team),
						Model.of("Are you sure you want to remove team <b>" + team.getName() + "</b>?")) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Team p = (Team) this.getDefaultModelObject();
						Gitop.getInstance(TeamManager.class).delete(p);
						target.add(teamsDiv);
					}
				});
			}
		};
		teamsDiv.add(view);
		return teamsDiv;
	}
}
