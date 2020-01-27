package io.onedev.server.web.page.project.issues.milestones;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.MilestoneSort;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.milestone.MilestoneDueLabel;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class MilestoneListPage extends ProjectIssuesPage {

	private static final String PARAM_STATE = "state";
	
	private static final String PARAM_SORT = "sort";
	
	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private final boolean closed;
	
	private final MilestoneSort sort;
	
	private Component milestonesTable;
	
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
		
		add(new Link<Void>("newMilestone") {

			@Override
			public void onClick() {
				setResponsePage(NewMilestonePage.class, NewMilestonePage.paramsOf(getProject()));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageIssues(getProject()));
			}
			
		});
		
		List<IColumn<Milestone, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Milestone, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
					IModel<Milestone> rowModel) {
				Milestone milestone = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "nameFrag", MilestoneListPage.this);
				Link<Void> link = new BookmarkablePageLink<Void>("link", MilestoneDetailPage.class, 
						MilestoneDetailPage.paramsOf(milestone, null));
				link.add(new Label("label", milestone.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Milestone, Void>(Model.of("Due Date")) {

			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
					IModel<Milestone> rowModel) {
				cellItem.add(new MilestoneDueLabel(componentId, rowModel));
			}
			
		});
		
		columns.add(new AbstractColumn<Milestone, Void>(Model.of("Issues")) {

			@Override
			public String getCssClass() {
				return "issues";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId, 
					IModel<Milestone> rowModel) {
				cellItem.add(new IssueStatsPanel(componentId, rowModel));
			}
		});
		
		if (SecurityUtils.canManage(getProject())) {
			columns.add(new AbstractColumn<Milestone, Void>(Model.of("")) {

				@Override
				public String getCssClass() {
					return "actions";
				}

				@Override
				public void populateItem(Item<ICellPopulator<Milestone>> cellItem, String componentId,
						IModel<Milestone> rowModel) {
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
				}
				
			});
		}
		
		SortableDataProvider<Milestone, Void> dataProvider = new LoadableDetachableDataProvider<Milestone, Void>() {

			@Override
			public Iterator<? extends Milestone> iterator(long first, long count) {
				EntityCriteria<Milestone> criteria = getCriteria(closed);
				criteria.addOrder(sort.getOrder());
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
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(milestonesTable = new DefaultDataTable<Milestone, Void>("milestones", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));		
		milestonesTable.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MilestonesResourceReference()));
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
	
}
