package io.onedev.server.web.page.project.issues.milestones;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.MilestoneAndState;
import io.onedev.server.util.MilestoneSort;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.milestone.MilestoneDueLabel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class MilestoneListPage extends ProjectPage {

	private static final String PARAM_STATE = "state";
	
	private static final String PARAM_SORT = "sort";
	
	private static final String PARAM_PAGE = "page";
	
	private final boolean closed;
	
	private final MilestoneSort sort;
	
	private final IModel<Collection<MilestoneAndState>> milestoneAndStatesModel = 
			new LoadableDetachableModel<Collection<MilestoneAndState>>() {

		@Override
		protected Collection<MilestoneAndState> load() {
			List<Milestone> milestones = new ArrayList<>();
			for (Component row: (WebMarkupContainer)milestonesTable.get("body").get("rows")) {
				Milestone milestone = (Milestone) row.getDefaultModelObject();
				milestones.add(milestone);
			}
			return OneDev.getInstance(IssueManager.class).queryMilestoneAndStates(getProject(), milestones);
		}
		
	}; 
	
	private DataTable<Milestone, Void> milestonesTable;					
	
	private EntityCriteria<Milestone> getCriteria(boolean closed) {
		EntityCriteria<Milestone> criteria = EntityCriteria.of(Milestone.class);
		criteria.add(Restrictions.eq("project", getProject()));
		criteria.add(Restrictions.eq("closed", closed));
		return criteria;
	}
	
	public MilestoneListPage(PageParameters params) {
		super(params);
		
		String state = params.get(PARAM_STATE).toString();
		if (state == null)
			closed = false;
		else 
			closed = state.toLowerCase().equals("closed");
			
		String sortString = params.get(PARAM_SORT).toString();
		if (sortString != null)
			sort = MilestoneSort.valueOf(sortString.toUpperCase());
		else
			sort = MilestoneSort.CLOSEST_DUE_DATE;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<Void> openLink = new BookmarkablePageLink<Void>("open", 
				MilestoneListPage.class, paramsOf(getProject(), false, sort));
		openLink.setOutputMarkupId(true);
		if (!closed)
			openLink.add(AttributeAppender.append("class", "active"));
		add(openLink);
		
		Link<Void> closeLink = new BookmarkablePageLink<Void>("closed", 
				MilestoneListPage.class, paramsOf(getProject(), true, sort));
		closeLink.setOutputMarkupId(true);
		if (closed)
			closeLink.add(AttributeAppender.append("class", "active"));
		add(closeLink);
		
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
							if (sort == MilestoneListPage.this.sort)
								return "tick";
							else
								return null;
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							PageParameters params = paramsOf(getProject(), closed, sort);
							Link<Void> link = new BookmarkablePageLink<Void>(id, MilestoneListPage.class, params);
							link.add(AttributeAppender.append("class", "milestone-sort"));
							if (sort == MilestoneListPage.this.sort)
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
		
		columns.add(new AbstractColumn<Milestone, Void>(Model.of("Name")) {

			@Override
			public String getCssClass() {
				return "name align-middle";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
					IModel<Milestone> rowModel) {
				Milestone milestone = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "nameFrag", MilestoneListPage.this);
				WebMarkupContainer link = new ActionablePageLink<Void>("link", MilestoneDetailPage.class, 
						MilestoneDetailPage.paramsOf(milestone, null)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								MilestoneListPage.class, getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Milestone.class, redirectUrlAfterDelete);
					}
					
				};
				link.add(new Label("label", milestone.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
			
		});
		
		columns.add(new AbstractColumn<Milestone, Void>(Model.of("Due Date")) {

			@Override
			public String getCssClass() {
				return "due-date d-none d-lg-table-cell align-middle";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
					IModel<Milestone> rowModel) {
				cellItem.add(new MilestoneDueLabel(componentId, rowModel));
			}
			
		});
		
		columns.add(new AbstractColumn<Milestone, Void>(Model.of("Issue Stats")) {

			@Override
			public String getCssClass() {
				return "issue-stats align-middle";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
					IModel<Milestone> rowModel) {
				Fragment fragment = new Fragment(componentId, "issueStatsFrag", MilestoneListPage.this) {

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
								for (MilestoneAndState milestoneAndState: milestoneAndStatesModel.getObject()) {
									if (milestoneAndState.getMilestoneId().equals(rowModel.getObject().getId())) {
										Integer count = stateStats.get(milestoneAndState.getIssueState());
										if (count != null)
											count ++;
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
								String query = new IssueQuery(new StateCriteria(state)).toString();
								PageParameters params = MilestoneDetailPage.paramsOf(rowModel.getObject(), query);
								return new ViewStateAwarePageLink<Void>(componentId, MilestoneDetailPage.class, params);
							}
							
						});								
						super.onBeforeRender();
					}
					
				};
				cellItem.add(fragment);
			}
			
		});
		
		if (SecurityUtils.canManageIssues(getProject())) {
			columns.add(new AbstractColumn<Milestone, Void>(Model.of("Actions")) {

				@Override
				public String getCssClass() {
					return "d-none d-lg-table-cell actions align-middle";
				}

				@Override
				public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
						IModel<Milestone> rowModel) {
					cellItem.add(new MilestoneActionsPanel(componentId, rowModel, false) {

						@Override
						protected void onUpdated(AjaxRequestTarget target) {
							target.add(milestonesTable);
						}

						@Override
						protected void onDeleted(AjaxRequestTarget target) {
							target.add(milestonesTable);
						}
						
					});
				}
				
			});
		}
		
		SortableDataProvider<Milestone, Void> dataProvider = new LoadableDetachableDataProvider<Milestone, Void>() {

			@Override
			public Iterator<? extends Milestone> iterator(long first, long count) {
				EntityCriteria<Milestone> criteria = getCriteria(closed);
				criteria.addOrder(sort.getOrder(closed));
				return OneDev.getInstance(Dao.class).query(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long calcSize() {
				return OneDev.getInstance(Dao.class).count(getCriteria(closed));
			}

			@Override
			public IModel<Milestone> model(Milestone object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Milestone>() {

					@Override
					protected Milestone load() {
						return OneDev.getInstance(MilestoneManager.class).load(id);
					}
					
				};
			}
		};
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), closed, sort);
				params.add(PARAM_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
		add(milestonesTable = new OneDataTable<Milestone, Void>("milestones", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));		
		milestonesTable.setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		milestoneAndStatesModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Project project, boolean closed, @Nullable MilestoneSort sort) {
		PageParameters params = paramsOf(project);
		if (closed)
			params.add(PARAM_STATE, "closed");
		else
			params.add(PARAM_STATE, "open");
			
		if (sort != null)
			params.add(PARAM_SORT, sort.name().toLowerCase());
		return params;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Milestones");
	}

	@Override
	protected String getPageTitle() {
		return "Milestones - " + getProject().getName();
	}
	
}
