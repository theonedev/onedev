package com.pmease.gitop.web.page.project.settings;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Authorization;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.bootstrap.Icon;
import com.pmease.gitop.web.common.bootstrap.IconType;
import com.pmease.gitop.web.common.component.datagrid.DataGrid;
import com.pmease.gitop.web.common.component.datagrid.hibernate.EntityListProvider;
import com.pmease.gitop.web.page.account.setting.teams.AccountTeamsPage;
import com.pmease.gitop.web.page.account.setting.teams.EditTeamPage;
import com.pmease.gitop.web.page.project.ProjectPubliclyAccessibleChanged;
import com.pmease.gitop.web.util.EnumUtils;

@SuppressWarnings("serial")
public class ProjectPermissionsPage extends AbstractProjectSettingPage {

	public ProjectPermissionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.PERMISSIONS;
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(newTeamsTable());
		add(new AjaxLink<Void>("resetlink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Project project = getProject();
				Collection<Authorization> list = project.getAuthorizations();
				AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
				for (Iterator<Authorization> it = list.iterator(); it.hasNext();) {
					Authorization auth = it.next();
					am.delete(auth);
					it.remove();
				}
				
				Gitop.getInstance(ProjectManager.class).save(project);
				onPermissionChanged(null, target);
			}
			
		});
		add(new BookmarkablePageLink<Void>("teamslink", AccountTeamsPage.class)
				.setVisibilityAllowed(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(getAccount()))));
	}
	
	private Authorization getAuthorization(Team team) {
 		for (Authorization each : getProject().getAuthorizations()) {
			if (Objects.equal(each.getTeam(), team)) {
				return each;
			}
		}
		
		return null;
	}
	
	private GeneralOperation getOriginalPermission(Team team) {
		if (team.isOwnersTeam()) {
			return GeneralOperation.ADMIN;
		}
		
		Authorization auth = getAuthorization(team);
		if (auth != null) {
			return auth.getRepoPermission();
		} else {
			return Gitop.getInstance(TeamManager.class).getTeamPermission(team); 
		}
	}

	private GeneralOperation getTeamPermission(Team team) {
		GeneralOperation permission = getOriginalPermission(team);
		if (team.isOwnersTeam() || team.isAnonymousTeam()) {
			return permission;
		}
		
		TeamManager tm = Gitop.getInstance(TeamManager.class);
		Team anonymous = tm.getAnonymousTeam(getAccount());
		GeneralOperation anonymousPermission = getOriginalPermission(anonymous);
		if (team.isLoggedInTeam()) {
			return GeneralOperation.higher(permission, anonymousPermission);
		}
		
		Team loggedIn = tm.getLoggedInTeam(getAccount());
		GeneralOperation op = GeneralOperation.higher(anonymousPermission, getOriginalPermission(loggedIn));
		return GeneralOperation.higher(permission, op);
	}
	
	private Component newTeamsTable() {
		List<IColumn<Team, String>> columns = Lists.newArrayList();
		
		IColumn<Team, String> column = new AbstractColumn<Team, String>(Model.of("Team")) {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem,
					String componentId, IModel<Team> rowModel) {
				Fragment frag = new Fragment(componentId, "namefrag", ProjectPermissionsPage.this);
				Team team = rowModel.getObject();
				AbstractLink link = new BookmarkablePageLink<Void>("editlink", 
																	EditTeamPage.class,
																	EditTeamPage.newParams(team));
				boolean enabled = SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(getAccount()));
				link.setEnabled(enabled);
				link.add(new Label("name", team.getName()));
				link.add(new WebMarkupContainer("icon").setVisibilityAllowed(enabled));
				frag.add(link);
				cellItem.add(frag);
			}
		};
		
		columns.add(column);
		
		columns.add(new PermissionColumn(GeneralOperation.READ));
		columns.add(new PermissionColumn(GeneralOperation.WRITE));
		columns.add(new PermissionColumn(GeneralOperation.ADMIN));
		columns.add(new HeaderlessColumn<Team, String>() {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem,
					String componentId, final IModel<Team> rowModel) {
				Fragment frag = new Fragment(componentId, "operationfrag", ProjectPermissionsPage.this);
				
				IModel<Authorization> model = new AbstractReadOnlyModel<Authorization>() {

					@Override
					public Authorization getObject() {
						AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
						return am.find(Restrictions.eq("team", rowModel.getObject()),
										Restrictions.eq("project", getProject()));
					}
					
				};
				
				frag.add(new AjaxLink<Authorization>("btn", model) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
						ProjectManager pm = Gitop.getInstance(ProjectManager.class);
						Authorization auth = (Authorization) getDefaultModelObject();
						am.delete(auth);
						Project project = getProject();
						project.getAuthorizations().remove(auth);
						pm.save(project);
						onPermissionChanged(rowModel.getObject(), target);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						boolean enabled = getDefaultModelObject() != null;
						this.setEnabled(enabled);
					}
				});
				
				cellItem.add(frag);
			}
			
			@Override
			public String getCssClass() {
				return "operations";
			}
		});
		
		IModel<List<Team>> teamsModel = new LoadableDetachableModel<List<Team>>() {

			@Override
			protected List<Team> load() {
				TeamManager tm = Gitop.getInstance(TeamManager.class);
				return tm.query(
						new Criterion[] {Restrictions.eq("owner", getProject().getOwner())}, 
						new Order[] { Order.asc("id")}, 
						0, 
						Integer.MAX_VALUE);
			}
			
		};
		
		EntityListProvider<Team> provider = new EntityListProvider<Team>(teamsModel);
		DataGrid<Team> table = new DataGrid<Team>("teamstable", columns, provider, Integer.MAX_VALUE);
		table.addTopToolbar(new HeadersToolbar<String>(table, provider));
		
		return table;
	}
	
	private class PermissionColumn extends AbstractColumn<Team, String> {
		
		private final GeneralOperation operation;
		
		public PermissionColumn(GeneralOperation operation) {
			super(Model.of(operation.toString()));
			this.operation = operation;
		}

		@Override
		public void populateItem(Item<ICellPopulator<Team>> cellItem,
				String componentId, final IModel<Team> rowModel) {
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

			Fragment frag = new Fragment(componentId, "permissionop", ProjectPermissionsPage.this);
			cellItem.add(frag);

			boolean enabled = true;
			if (team.isOwnersTeam()) {
				enabled = false; // cannot edit owners team permission
			} else if (team.isAnonymousTeam() || operation == GeneralOperation.ADMIN) {
				enabled = true; // can always edit anonymous and admin column
			} else {
				TeamManager tm = Gitop.getInstance(TeamManager.class);
				if (operation == GeneralOperation.READ) {
					GeneralOperation op = getTeamPermission(tm.getAnonymousTeam(getAccount()));
					if (op == GeneralOperation.READ) {
						enabled = false;
					} else {
						// operation is NO_ACCESS
						op = getTeamPermission(tm.getLoggedInTeam(getAccount()));
						enabled = team.isLoggedInTeam() ? true : !op.can(GeneralOperation.READ);
					}
				} else if (operation == GeneralOperation.WRITE) {
					GeneralOperation op = getTeamPermission(tm.getLoggedInTeam(getAccount()));
					enabled = team.isLoggedInTeam() ? true : !op.can(GeneralOperation.WRITE);
				}
			}
			
			AbstractLink link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					Team team = rowModel.getObject();
					GeneralOperation permission = getTeamPermission(rowModel.getObject());
					if (Objects.equal(operation, permission)) {
						permission = EnumUtils.dec(permission);
					} else if (operation.ordinal() < permission.ordinal()) {
						permission = EnumUtils.dec(operation);
					} else {
						permission = operation;
					}
					
					AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
					Authorization auth = am.find(
							Restrictions.eq("project", getProject()),
							Restrictions.eq("team", rowModel.getObject()));

					if (auth == null) {
						auth = new Authorization();
						auth.setTeam(team);
						auth.setProject(getProject());
					}
					
					auth.setRepoPermission(permission);
					am.save(auth);
					Project project = getProject();
					project.getAuthorizations().add(auth);
					Gitop.getInstance(ProjectManager.class).save(project);
					onPermissionChanged(team, target);
					
					if (team.isAnonymousTeam()) {
						send(getPage(), Broadcast.BREADTH, new ProjectPubliclyAccessibleChanged(target));
					}
				}
				
			};
			
			link.add(new Icon("icon", new AbstractReadOnlyModel<IconType>() {

				@Override
				public IconType getObject() {
					return operation.ordinal() > getTeamPermission(rowModel.getObject()).ordinal() ?
							IconType.UNCHECKED : IconType.CHECK;
				}
				
			}));
			
			link.setEnabled(enabled);
			frag.add(link);
		}
		
		@Override
		public String getCssClass() {
			return "permission-col";
		}
	}
	
	private void onPermissionChanged(Team team, AjaxRequestTarget target) {
		if (team == null || team.isAnonymousTeam()) {
			send(this, Broadcast.BREADTH, new ProjectPubliclyAccessibleChanged(target));
		}
		
		target.add(get("teamstable"));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
}
