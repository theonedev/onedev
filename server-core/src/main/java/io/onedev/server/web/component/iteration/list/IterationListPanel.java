package io.onedev.server.web.component.iteration.list;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IterationService;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IterationAndIssueState;
import io.onedev.server.util.IterationSort;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.iteration.IterationDateLabel;
import io.onedev.server.web.component.iteration.actions.IterationActionsPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.page.project.issues.iteration.IterationIssuesPage;
import io.onedev.server.web.page.project.issues.iteration.NewIterationPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;

public class IterationListPanel extends GenericPanel<Project> {

	private static final int MAX_DESC_DISP_LEN = 64;

	private boolean closed;
	
	private IterationSort sort;
	
	private final PagingHistorySupport pagingHistorySupport;
	
	private final IModel<Collection<IterationAndIssueState>> iterationAndStatesModel =
			new LoadableDetachableModel<>() {

				@Override
				protected Collection<IterationAndIssueState> load() {
					List<Iteration> iterations = new ArrayList<>();
					for (Component row : (WebMarkupContainer) iterationsTable.get("body").get("rows")) {
						Iteration iteration = (Iteration) row.getDefaultModelObject();
						iterations.add(iteration);
					}
					return OneDev.getInstance(IssueService.class).queryIterationAndIssueStates(getProject(), iterations);
				}

			}; 
	
	private DataTable<Iteration, Void> iterationsTable;					
	
	private EntityCriteria<Iteration> getCriteria(boolean closed) {
		EntityCriteria<Iteration> criteria = EntityCriteria.of(Iteration.class);
		criteria.add(Restrictions.in("project", getProject().getSelfAndAncestors()));
		criteria.add(Restrictions.eq("closed", closed));
		return criteria;
	}
	
	public IterationListPanel(String id, IModel<Project> model, boolean closed, IterationSort sort,
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
				target.add(iterationsTable);
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
				target.add(iterationsTable);
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
				for (IterationSort sort: IterationSort.values()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return sort.toString();
						}

						@Override
						public String getIconHref() {
							if (sort == IterationListPanel.this.sort)
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
									target.add(iterationsTable);
									IterationListPanel.this.sort = sort;
									onSortChanged(target, sort);
								}
								
							};
							link.add(AttributeAppender.append("class", "iteration-sort"));
							if (sort == IterationListPanel.this.sort)
								link.add(AttributeAppender.append("class", "active"));
							return link;
						}
						
					});
				}
				return menuItems;
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("newIteration", NewIterationPage.class, NewIterationPage.paramsOf(getProject())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageIssues(getProject()));
			}
			
		});
		
		List<IColumn<Iteration, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of(_T("Name"))) {

			@Override
			public String getCssClass() {
				return "name align-middle";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Iteration>> cellItem, String componentId,
                                     IModel<Iteration> rowModel) {
				Iteration iteration = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "nameFrag", IterationListPanel.this);
				WebMarkupContainer link = new ActionablePageLink("link", IterationIssuesPage.class,
						IterationIssuesPage.paramsOf(getProject(), iteration, null)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								getPage().getClass(), getPage().getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Iteration.class, redirectUrlAfterDelete);
					}

				};
				link.add(new Label("label", iteration.getName()));
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

		columns.add(new AbstractColumn<>(Model.of(_T("Start/Due Date"))) {

			@Override
			public String getCssClass() {
				return "due-date d-none d-lg-table-cell align-middle";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Iteration>> cellItem, String componentId,
                                     IModel<Iteration> rowModel) {
				cellItem.add(new IterationDateLabel(componentId, rowModel));
			}

		});

		columns.add(new AbstractColumn<>(Model.of(_T("Description"))) {

			@Override
			public String getCssClass() {
				return "description d-none d-lg-table-cell align-middle";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Iteration>> cellItem, String componentId,
                                     IModel<Iteration> rowModel) {
				cellItem.add(new Label(componentId, StringUtils.abbreviate(rowModel.getObject().getDescription(), MAX_DESC_DISP_LEN)));
			}

		});
		
		columns.add(new AbstractColumn<>(Model.of(_T("Issue Stats"))) {

			@Override
			public String getCssClass() {
				return "issue-stats align-middle";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Iteration>> cellItem, String componentId,
                                     IModel<Iteration> rowModel) {
				Fragment fragment = new Fragment(componentId, "issueStatsFrag", IterationListPanel.this) {

					@Override
					protected void onBeforeRender() {
						/*
						 * Create StateStatsBar here as it requires to access the iterationAndStatsModel which can
						 * only be calculated correctly after the iteration table is initialized
						 */
						addOrReplace(new StateStatsBar("content", new LoadableDetachableModel<Map<String, Integer>>() {

							@Override
							protected Map<String, Integer> load() {
								Map<String, Integer> stateStats = new HashMap<>();
								for (IterationAndIssueState iterationAndState : iterationAndStatesModel.getObject()) {
									if (iterationAndState.getIterationId().equals(rowModel.getObject().getId())) {
										Integer count = stateStats.get(iterationAndState.getIssueState());
										if (count != null)
											count++;
										else
											count = 1;
										stateStats.put(iterationAndState.getIssueState(), count);
									}
								}
								return stateStats;
							}

						}) {

							@Override
							protected Link<Void> newStateLink(String componentId, String state) {
								String query = new IssueQuery(new StateCriteria(state, IssueQueryLexer.Is)).toString();
								PageParameters params = IterationIssuesPage.paramsOf(getProject(), rowModel.getObject(), query);
								return new ViewStateAwarePageLink<Void>(componentId, IterationIssuesPage.class, params);
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
				public void populateItem(Item<ICellPopulator<Iteration>> cellItem, String componentId,
                                         IModel<Iteration> rowModel) {
					if (rowModel.getObject().getProject().equals(getProject())) {
						cellItem.add(new IterationActionsPanel(componentId, rowModel) {

							@Override
							protected void onUpdated(AjaxRequestTarget target) {
								target.add(iterationsTable);
							}

							@Override
							protected void onDeleted(AjaxRequestTarget target) {
								target.add(iterationsTable);
							}

						});
					} else {
						cellItem.add(new Label(componentId, "&nbsp;").setEscapeModelStrings(false));
					}
				}

			});
		}
		
		SortableDataProvider<Iteration, Void> dataProvider = new LoadableDetachableDataProvider<>() {

			@Override
			public Iterator<? extends Iteration> iterator(long first, long count) {
				EntityCriteria<Iteration> criteria = getCriteria(closed);
				criteria.addOrder(sort.getOrder(closed));
				return OneDev.getInstance(Dao.class).query(criteria, (int) first, (int) count).iterator();
			}

			@Override
			public long calcSize() {
				return OneDev.getInstance(Dao.class).count(getCriteria(closed));
			}

			@Override
			public IModel<Iteration> model(Iteration object) {
				Long id = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected Iteration load() {
						return OneDev.getInstance(IterationService.class).load(id);
					}

				};
			}
		};
		
		add(iterationsTable = new DefaultDataTable<>("iterations", columns, dataProvider,
				WebConstants.PAGE_SIZE, pagingHistorySupport));		
		iterationsTable.setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		iterationAndStatesModel.detach();
		super.onDetach();
	}
	
	private Project getProject() {
		return getModelObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IterationListCssResourceReference()));
	}

	protected void onSortChanged(AjaxRequestTarget target, IterationSort sort) {
	}
	
	protected void onStateChanged(AjaxRequestTarget target, boolean closed) {
	}
	
}
