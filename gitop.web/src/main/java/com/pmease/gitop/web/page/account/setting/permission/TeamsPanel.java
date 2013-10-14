package com.pmease.gitop.web.page.account.setting.permission;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.bootstrap.AjaxIconLink;
import com.pmease.gitop.web.common.bootstrap.IconType;
import com.pmease.gitop.web.common.component.datagrid.DataGrid;
import com.pmease.gitop.web.common.component.datagrid.event.SearchStringChanged;
import com.pmease.gitop.web.common.component.datagrid.hibernate.HibernateDataProvider;
import com.pmease.gitop.web.common.component.datagrid.toolbar.SearchNavToolbar;
import com.pmease.gitop.web.common.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.util.WicketUtils;

@SuppressWarnings("serial")
public class TeamsPanel extends Panel {

	public TeamsPanel(String id, IModel<User> user) {
		super(id, user);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new BookmarkablePageLink<Void>("addTeam", AddTeamPage.class));
		add(createTeamPermissionsTable("teams"));
	}

	private Component createTeamPermissionsTable(String id) {
		List<IColumn<Team, String>> columns = Lists.newArrayList();
		columns.add(new AbstractColumn<Team, String>(Model.of("Name"), "name") {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem,
					String componentId, IModel<Team> rowModel) {
				Team team = rowModel.getObject();
				Fragment frag = new Fragment(componentId, "namefrag",
						TeamsPanel.this);
				frag.add(new Label("name", team.getName()));
				frag.add(new Label("members", team.getMemberships().size()));
				cellItem.add(frag);
			}
		});

		columns.add(new TeamPermissionColumn(Model.of("Read"), GeneralOperation.READ));
		columns.add(new TeamPermissionColumn(Model.of("Write"), GeneralOperation.WRITE));
		columns.add(new TeamPermissionColumn(Model.of("Admin"), GeneralOperation.ADMIN));

		columns.add(new AbstractColumn<Team, String>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem,
					String componentId, final IModel<Team> rowModel) {
				Fragment frag = new Fragment(componentId, "ops", TeamsPanel.this);
				frag.add(new BookmarkablePageLink<Void>("edit",
						EditTeamPage.class, WicketUtils.newPageParams("teamId", rowModel.getObject().getId())));
				
				Team team = rowModel.getObject();
				frag.add(new AjaxConfirmLink<Void>("remove",
						Model.of("Are you sure you want to remove team " + team.getName())) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Team p = rowModel.getObject();
						Gitop.getInstance(TeamManager.class).delete(p);
						onTeamTableChanged(target);
					}
				});
				cellItem.add(frag);
			}

			@Override
			public String getCssClass() {
				return "operations-td";
			}
		});

		final List<Criterion> criterions = Lists.newArrayList();
		
		HibernateDataProvider<Team> teamProvider = new HibernateDataProvider<Team>() {

			@Override
			protected DetachedCriteria getCriteria() {
				DetachedCriteria criteria = DetachedCriteria.forClass(Team.class);
				criteria.add(Restrictions.eq("owner", getUser()));
				for (Criterion each : criterions) {
					criteria.add(each);
				}
				
				return criteria;
			}
		};

		DataGrid<Team> dataTable = new DataGrid<Team>(id, columns,
				teamProvider, 10) {
			@Override
			public void onEvent(IEvent<?> event) {
				if (event.getPayload() instanceof SearchStringChanged) {
					SearchStringChanged e = (SearchStringChanged) event.getPayload();
					
					criterions.clear();
					
					if (!Strings.isNullOrEmpty(e.getPattern())) {
						criterions.add(Restrictions.ilike("name", e.getPattern(), MatchMode.ANYWHERE));
					}

					e.getTarget().add(this);
				}
			}
		};

		dataTable.setOutputMarkupId(true);
		dataTable.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(dataTable, teamProvider));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable, Model.of("No Teams Found")));
		dataTable.addBottomToolbar(new SearchNavToolbar(dataTable));

		return dataTable;
	}

	private class TeamPermissionColumn extends AbstractColumn<Team, String> {

		private GeneralOperation expected;

		public TeamPermissionColumn(IModel<String> displayModel,
				GeneralOperation expected) {
			super(displayModel);
			this.expected = expected;
		}

		@Override
		public void populateItem(Item<ICellPopulator<Team>> cellItem,
				String componentId, final IModel<Team> rowModel) {
			IModel<IconType> iconModel = new AbstractReadOnlyModel<IconType>() {

				@Override
				public IconType getObject() {
					return rowModel.getObject().getAuthorizedOperation().can(expected) ? 
							IconType.CHECK : IconType.CHECK_EMPTY;
				}
			};

			cellItem.add(new AjaxIconLink(componentId, iconModel) {

				@Override
				protected AjaxLink<?> createLink(String id) {
					AjaxLink<?> link = new AjaxLink<Void>(id) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							GeneralOperation old = rowModel.getObject().getAuthorizedOperation();
							Team team = rowModel.getObject();
							if (old == expected) {
								int ordinal = expected.ordinal() - 1;
								if (ordinal < 0) {
									ordinal = 0;
								}

								team.setAuthorizedOperation(GeneralOperation.values()[ordinal]);
							} else {
								team.setAuthorizedOperation(expected);
							}

							Gitop.getInstance(TeamManager.class).save(team);
							onTeamTableChanged(target);
						}

					};

					link.add(AttributeAppender.append("class", "permission-link"));
					return link;
				}
			});
		}
	}

	private void onTeamTableChanged(AjaxRequestTarget target) {
		target.add(get("teams"));
	}

	private User getUser() {
		return (User) getDefaultModelObject();
	}

}
