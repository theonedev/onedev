package com.gitplex.server.web.page.user;

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

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.AbstractEntity;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestTask;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.datatable.DefaultDataTable;
import com.gitplex.server.web.component.datatable.EntityDataProvider;
import com.gitplex.server.web.component.datatable.SelectionColumn;
import com.gitplex.server.web.component.link.BranchLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.gitplex.server.web.util.DateUtils;

@SuppressWarnings("serial")
public class TaskListPage extends UserPage {

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
		
		DataTable<PullRequestTask, String> dataTable = 
				new DefaultDataTable<PullRequestTask, String>("tasks", columns, dataProvider, WebConstants.PAGE_SIZE);
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
						GitPlex.getInstance(Dao.class).remove(model.getObject());
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