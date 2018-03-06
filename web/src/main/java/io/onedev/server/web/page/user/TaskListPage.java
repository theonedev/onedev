package io.onedev.server.web.page.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestTask;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.datatable.EntityDataProvider;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.SelectionColumn;
import io.onedev.server.web.component.link.BranchLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequest.requestdetail.overview.RequestOverviewPage;
import io.onedev.server.web.util.DateUtils;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class TaskListPage extends UserPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	public TaskListPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		EntityDataProvider<PullRequestTask> dataProvider = new EntityDataProvider<PullRequestTask>(
				PullRequestTask.class, new SortParam<String>("date", false)) {

				@Override
				protected void restrict(EntityCriteria<? extends AbstractEntity> criteria) {
					criteria.add(Restrictions.eq("user", getUser()));
				}
			
		};
		
		SelectionColumn<PullRequestTask, String> selectionColumn;
		
		List<IColumn<PullRequestTask, String>> columns = new ArrayList<>();
		selectionColumn = new SelectionColumn<PullRequestTask, String>();
		if (getUser().equals(getLoginUser()))
			columns.add(selectionColumn);
		
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("Pull Request"), "request") {

			@Override
			public void populateItem(
					Item<ICellPopulator<PullRequestTask>> cellItem,
					String componentId, IModel<PullRequestTask> rowModel) {
				PullRequestTask task = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "linkFrag", TaskListPage.this);
				fragment.add(new ViewStateAwarePageLink<Void>("link", RequestOverviewPage.class, 
						RequestOverviewPage.paramsOf(task.getRequest())) {

					@Override
					public IModel<?> getBody() {
						return Model.of(rowModel.getObject().getRequest().getTitle());
					}
					
				});
				cellItem.add(fragment);
			}
			
		});
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("Target Branch")) {

			@Override
			public void populateItem(
					Item<ICellPopulator<PullRequestTask>> cellItem,
					String componentId, IModel<PullRequestTask> rowModel) {
				PullRequest request = rowModel.getObject().getRequest();
				Fragment fragment = new Fragment(componentId, "linkFrag", TaskListPage.this);
				fragment.add(new BranchLink("link", request.getTarget(), null));
				cellItem.add(fragment);
			}
			
		});
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("Source Branch")) {

			@Override
			public void populateItem(
					Item<ICellPopulator<PullRequestTask>> cellItem,
					String componentId, IModel<PullRequestTask> rowModel) {
				PullRequest request = rowModel.getObject().getRequest();
				if (request.getSource() != null) {
					Fragment fragment = new Fragment(componentId, "linkFrag", TaskListPage.this);
					fragment.add(new BranchLink("link", request.getSource(), request));
					cellItem.add(fragment);
				} else {
					cellItem.add(new Label(componentId, request.getSource().getFQN()));
				}
			}
			
		});
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("Task"), "type") {

			@Override
			public void populateItem(
					Item<ICellPopulator<PullRequestTask>> cellItem,
					String componentId, IModel<PullRequestTask> rowModel) {
				PullRequestTask task = rowModel.getObject();
				cellItem.add(new Label(componentId, task.getType().toString()));
			}
			
		});
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("When"), "date") {

			@Override
			public void populateItem(
					Item<ICellPopulator<PullRequestTask>> cellItem,
					String componentId, IModel<PullRequestTask> rowModel) {
				PullRequestTask type = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatAge(type.getDate())));
			}
			
		});
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getUser());
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		DataTable<PullRequestTask, String> dataTable = new HistoryAwareDataTable<PullRequestTask, String>("tasks", columns, 
				dataProvider, WebConstants.PAGE_SIZE, pagingHistorySupport);
		dataTable.setVisible(dataTable.getRowCount() != 0);
		add(dataTable);
		
		add(new WebMarkupContainer("noTasks").setVisible(dataTable.getRowCount() == 0));
		
		add(new Link<Void>("deleteSelected") {

			@Override
			public void onClick() {
				if (selectionColumn.getSelections().isEmpty()) {
					getSession().warn("Please select tasks to delete");
				} else {
					for (IModel<PullRequestTask> model: selectionColumn.getSelections())
						OneDev.getInstance(Dao.class).remove(model.getObject());
				}
				setResponsePage(TaskListPage.class, TaskListPage.paramsOf(getUser()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().equals(getLoginUser()) && dataTable.getRowCount() != 0);
			}
			
		});
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getUser());
	}

}