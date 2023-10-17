package io.onedev.server.web.component.milestone.list;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.MilestoneAndIssueState;
import io.onedev.server.util.MilestoneSort;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.milestone.MilestoneDateLabel;
import io.onedev.server.web.component.milestone.actions.MilestoneActionsPanel;
import io.onedev.server.web.page.project.issues.milestones.MilestoneIssuesPage;
import io.onedev.server.web.page.project.issues.milestones.NewMilestonePage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("serial")
public class MilestoneListPanel extends GenericPanel<Project> {

	private boolean closed;
	
	private MilestoneSort sort;
	
	private final PagingHistorySupport pagingHistorySupport;
	
	private final IModel<Collection<MilestoneAndIssueState>> milestoneAndStatesModel =
			new LoadableDetachableModel<>() {

				@Override
				protected Collection<MilestoneAndIssueState> load() {
					List<Milestone> milestones = new ArrayList<>();
					for (Component row : (WebMarkupContainer) milestonesTable.get("body").get("rows")) {
						Milestone milestone = (Milestone) row.getDefaultModelObject();
						milestones.add(milestone);
					}
					return OneDev.getInstance(IssueManager.class).queryMilestoneAndIssueStates(getProject(), milestones);
				}

			}; 
	
	private DataTable<Milestone, Void> milestonesTable;					
	
	private EntityCriteria<Milestone> getCriteria(boolean closed) {
		EntityCriteria<Milestone> criteria = EntityCriteria.of(Milestone.class);
		criteria.add(Restrictions.in("project", getProject().getSelfAndAncestors()));
		criteria.add(Restrictions.eq("closed", closed));
		return criteria;
	}
	
	public MilestoneListPanel(String id, IModel<Project> model, boolean closed, MilestoneSort sort, 
			@Nullable PagingHistorySupport pagingHistorySupport) {
		super(id, model);

		this.closed = closed;
		this.sort = sort;
		this.pagingHistorySupport = pagingHistorySupport;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer statesContainer = new WebMarkupContainer("states");
		statesContainer.setOutputMarkupId(true);
		add(statesContainer);
		
		AjaxLink<Void> openLink = new AjaxLink<Void>("open") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				closed = false;
				target.add(statesContainer);
				target.add(milestonesTable);
				onStateChanged(target, closed);
			}
			
		};
		openLink.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !closed? "active": "";
			}
			
		}));
		statesContainer.add(openLink);
		
		AjaxLink<Void> closedLink = new AjaxLink<Void>("closed") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				closed = true;
				target.add(statesContainer);
				target.add(milestonesTable);
				onStateChanged(target, closed);
			}
			
		};
		closedLink.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return closed? "active": "";
			}
			
		}));
		statesContainer.add(closedLink);
		
		add(new MenuLink("sort") {
			
			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				for (MilestoneSort sort: MilestoneSort.values()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return sort.toString();
						}

						@Override
						public String getIconHref() {
							if (sort == MilestoneListPanel.this.sort)
								return "tick";
							else
								return null;
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							AjaxLink<Void> link = new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									target.add(milestonesTable);
									MilestoneListPanel.this.sort = sort;
									onSortChanged(target, sort);
								}
								
							};
							link.add(AttributeAppender.append("class", "milestone-sort"));
							if (sort == MilestoneListPanel.this.sort)
								link.add(AttributeAppender.append("class", "active"));
							return link;
						}
						
					});
				}
				return menuItems;
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("newMilestone", NewMilestonePage.class, NewMilestonePage.paramsOf(getProject())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageIssues(getProject()));
			}
			
		});
		
		List<IColumn<Milestone, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of("Name")) {

			@Override
			public String getCssClass() {
				return "name align-middle";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
									 IModel<Milestone> rowModel) {
				Milestone milestone = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "nameFrag", MilestoneListPanel.this);
				WebMarkupContainer link = new ActionablePageLink("link", MilestoneIssuesPage.class,
						MilestoneIssuesPage.paramsOf(getProject(), milestone, null)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								getPage().getClass(), getPage().getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Milestone.class, redirectUrlAfterDelete);
					}

				};
				link.add(new Label("label", milestone.getName()));
				fragment.add(link);
				fragment.add(new WebMarkupContainer("inherited") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!rowModel.getObject().getProject().equals(getProject()));
					}

				});
				cellItem.add(fragment);
			}

		});

		columns.add(new AbstractColumn<>(Model.of("Due Date")) {

			@Override
			public String getCssClass() {
				return "due-date d-none d-lg-table-cell align-middle";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
									 IModel<Milestone> rowModel) {
				cellItem.add(new MilestoneDateLabel(componentId, rowModel));
			}

		});
		
		columns.add(new AbstractColumn<>(Model.of("Issue Stats")) {

			@Override
			public String getCssClass() {
				return "issue-stats align-middle";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
									 IModel<Milestone> rowModel) {
				Fragment fragment = new Fragment(componentId, "issueStatsFrag", MilestoneListPanel.this) {

					@Override
					protected void onBeforeRender() {
						/*
						 * Create StateStatsBar here as it requires to access the milestoneAndStatsModel which can
						 * only be calculated correctly after the milestone table is initialized
						 */
						addOrReplace(new StateStatsBar("content", new LoadableDetachableModel<Map<String, Integer>>() {

							@Override
							protected Map<String, Integer> load() {
								Map<String, Integer> stateStats = new HashMap<>();
								for (MilestoneAndIssueState milestoneAndState : milestoneAndStatesModel.getObject()) {
									if (milestoneAndState.getMilestoneId().equals(rowModel.getObject().getId())) {
										Integer count = stateStats.get(milestoneAndState.getIssueState());
										if (count != null)
											count++;
										else
											count = 1;
										stateStats.put(milestoneAndState.getIssueState(), count);
									}
								}
								return stateStats;
							}

						}) {

							@Override
							protected Link<Void> newStateLink(String componentId, String state) {
								String query = new IssueQuery(new StateCriteria(state, IssueQueryLexer.Is)).toString();
								PageParameters params = MilestoneIssuesPage.paramsOf(getProject(), rowModel.getObject(), query);
								return new ViewStateAwarePageLink<Void>(componentId, MilestoneIssuesPage.class, params);
							}

						});
						super.onBeforeRender();
					}

				};
				cellItem.add(fragment);
			}

		});
		
		if (SecurityUtils.canManageIssues(getProject())) {
			columns.add(new AbstractColumn<>(Model.of("")) {

				@Override
				public String getCssClass() {
					return "d-none d-lg-table-cell actions align-middle";
				}

				@Override
				public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
										 IModel<Milestone> rowModel) {
					if (rowModel.getObject().getProject().equals(getProject())) {
						cellItem.add(new MilestoneActionsPanel(componentId, rowModel) {

							@Override
							protected void onUpdated(AjaxRequestTarget target) {
								target.add(milestonesTable);
							}

							@Override
							protected void onDeleted(AjaxRequestTarget target) {
								target.add(milestonesTable);
							}

						});
					} else {
						cellItem.add(new Label(componentId, "&nbsp;").setEscapeModelStrings(false));
					}
				}

			});
		}
		
		SortableDataProvider<Milestone, Void> dataProvider = new LoadableDetachableDataProvider<>() {

			@Override
			public Iterator<? extends Milestone> iterator(long first, long count) {
				EntityCriteria<Milestone> criteria = getCriteria(closed);
				criteria.addOrder(sort.getOrder(closed));
				return OneDev.getInstance(Dao.class).query(criteria, (int) first, (int) count).iterator();
			}

			@Override
			public long calcSize() {
				return OneDev.getInstance(Dao.class).count(getCriteria(closed));
			}

			@Override
			public IModel<Milestone> model(Milestone object) {
				Long id = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected Milestone load() {
						return OneDev.getInstance(MilestoneManager.class).load(id);
					}

				};
			}
		};
		
		add(milestonesTable = new DefaultDataTable<>("milestones", columns, dataProvider,
				WebConstants.PAGE_SIZE, pagingHistorySupport));		
		milestonesTable.setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		milestoneAndStatesModel.detach();
		super.onDetach();
	}
	
	private Project getProject() {
		return getModelObject();
	}

	protected void onSortChanged(AjaxRequestTarget target, MilestoneSort sort) {
	}
	
	protected void onStateChanged(AjaxRequestTarget target, boolean closed) {
	}
	
}
