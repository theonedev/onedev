package com.pmease.gitop.web.page.repository.settings;

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
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Authorization;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.model.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.common.wicket.component.datagrid.DataGrid;
import com.pmease.gitop.web.common.wicket.component.datagrid.hibernate.EntityListProvider;
import com.pmease.gitop.web.common.wicket.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.page.account.setting.teams.AccountTeamsPage;
import com.pmease.gitop.web.page.account.setting.teams.EditTeamPage;
import com.pmease.gitop.web.page.repository.RepositoryPubliclyAccessibleChanged;
import com.pmease.gitop.web.util.EnumUtils;

@SuppressWarnings("serial")
public class RepositoryPermissionsPage extends AbstractRepositorySettingPage {

	public RepositoryPermissionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newTeamsTable());
		add(new AjaxConfirmLink<Void>("resetlink", Model.of("Are you sure you want to reset to the default permissions?")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Repository repository = getRepository();
				Collection<Authorization> list = repository.getAuthorizations();
				AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
				for (Iterator<Authorization> it = list.iterator(); it.hasNext();) {
					Authorization auth = it.next();
					am.delete(auth);
					it.remove();
				}
				
				Gitop.getInstance(RepositoryManager.class).save(repository);
				onPermissionChanged(null, target);
			}
			
		});
		add(new BookmarkablePageLink<Void>("teamslink", AccountTeamsPage.class)
				.setVisibilityAllowed(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(getAccount()))));
	}
	
	private Authorization getAuthorization(Team team) {
 		for (Authorization each : getRepository().getAuthorizations()) {
			if (Objects.equal(each.getTeam(), team)) {
				return each;
			}
		}
		
		return null;
	}
	
	private GeneralOperation getOriginalPermission(Team team) {
		if (team.isOwners()) {
			return GeneralOperation.ADMIN;
		}
		
		Authorization auth = getAuthorization(team);
		if (auth != null) {
			return auth.getOperation();
		} else {
			return Gitop.getInstance(TeamManager.class).getActualAuthorizedOperation(team); 
		}
	}

	private GeneralOperation getTeamPermission(Team team) {
		GeneralOperation permission = getOriginalPermission(team);
		if (team.isOwners() || team.isAnonymous()) {
			return permission;
		}
		
		TeamManager tm = Gitop.getInstance(TeamManager.class);
		Team anonymous = tm.getAnonymous(getAccount());
		GeneralOperation anonymousPermission = getOriginalPermission(anonymous);
		if (team.isLoggedIn()) {
			return GeneralOperation.mostPermissive(permission, anonymousPermission);
		}
		
		Team loggedIn = tm.getLoggedIn(getAccount());
		GeneralOperation op = GeneralOperation.mostPermissive(anonymousPermission, getOriginalPermission(loggedIn));
		return GeneralOperation.mostPermissive(permission, op);
	}
	
	private Component newTeamsTable() {
		List<IColumn<Team, String>> columns = Lists.newArrayList();
		
		IColumn<Team, String> column = new AbstractColumn<Team, String>(Model.of("Team")) {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem,
					String componentId, IModel<Team> rowModel) {
				Fragment frag = new Fragment(componentId, "namefrag", RepositoryPermissionsPage.this);
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
				Fragment frag = new Fragment(componentId, "operationfrag", RepositoryPermissionsPage.this);
				
				IModel<Authorization> model = new AbstractReadOnlyModel<Authorization>() {

					@Override
					public Authorization getObject() {
						AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
						return am.find(Restrictions.eq("team", rowModel.getObject()),
										Restrictions.eq("repository", getRepository()));
					}
					
				};
				
				frag.add(new AjaxLink<Authorization>("btn", model) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
						RepositoryManager pm = Gitop.getInstance(RepositoryManager.class);
						Authorization auth = (Authorization) getDefaultModelObject();
						am.delete(auth);
						Repository repository = getRepository();
						repository.getAuthorizations().remove(auth);
						pm.save(repository);
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
						new Criterion[] {Restrictions.eq("owner", getRepository().getOwner())}, 
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
			if (team.isAnonymous()) {
				displayed = operation == GeneralOperation.READ;
			} else if (team.isLoggedIn()) {
				displayed = operation != GeneralOperation.ADMIN;
			}

			if (!displayed) {
				cellItem.add(new WebMarkupContainer(componentId).setVisibilityAllowed(false));
				return;
			}

			Fragment frag = new Fragment(componentId, "permissionop", RepositoryPermissionsPage.this);
			cellItem.add(frag);

			boolean enabled = true;
			if (team.isOwners()) {
				enabled = false; // cannot edit owners team permission
			} else if (team.isAnonymous() || operation == GeneralOperation.ADMIN) {
				enabled = true; // can always edit anonymous and admin column
			} else {
				TeamManager tm = Gitop.getInstance(TeamManager.class);
				if (operation == GeneralOperation.READ) {
					GeneralOperation op = getTeamPermission(tm.getAnonymous(getAccount()));
					if (op == GeneralOperation.READ) {
						enabled = false;
					} else {
						// operation is NO_ACCESS
						op = getTeamPermission(tm.getLoggedIn(getAccount()));
						enabled = team.isLoggedIn() ? true : !op.can(GeneralOperation.READ);
					}
				} else if (operation == GeneralOperation.WRITE) {
					GeneralOperation op = getTeamPermission(tm.getLoggedIn(getAccount()));
					enabled = team.isLoggedIn() ? true : !op.can(GeneralOperation.WRITE);
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
							Restrictions.eq("repository", getRepository()),
							Restrictions.eq("team", rowModel.getObject()));

					if (auth == null) {
						auth = new Authorization();
						auth.setTeam(team);
						auth.setRepository(getRepository());
					}
					
					auth.setOperation(permission);
					am.save(auth);
					Repository repository = getRepository();
					repository.getAuthorizations().add(auth);
					Gitop.getInstance(RepositoryManager.class).save(repository);
					onPermissionChanged(team, target);
					
					if (team.isAnonymous()) {
						send(getPage(), Broadcast.BREADTH, new RepositoryPubliclyAccessibleChanged(target));
					}
				}
				
			};
			
			link.add(new Icon("icon", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return operation.ordinal() > getTeamPermission(rowModel.getObject()).ordinal() ?
							"icon-checkbox-unchecked" : "icon-checkbox-checked";
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
		if (team == null || team.isAnonymous()) {
			send(this, Broadcast.BREADTH, new RepositoryPubliclyAccessibleChanged(target));
		}
		
		target.add(get("teamstable"));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	protected String getPageTitle() {
		return "Permissions - " + getRepository();
	}
}
