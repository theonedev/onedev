package com.gitplex.server.web.page.account.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.gitplex.commons.hibernate.AbstractEntity;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.commons.wicket.component.datatable.DefaultDataTable;
import com.gitplex.commons.wicket.component.datatable.EntityDataProvider;
import com.gitplex.commons.wicket.component.datatable.SelectionColumn;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestTask;
import com.gitplex.server.web.Constants;
import com.gitplex.server.web.component.BranchLink;
import com.gitplex.server.web.page.account.AccountLayoutPage;
import com.gitplex.server.web.page.account.overview.AccountOverviewPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.gitplex.server.web.util.DateUtils;

@SuppressWarnings("serial")
public class TaskListPage extends AccountLayoutPage {

	public TaskListPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(!getAccount().isOrganization());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		EntityDataProvider<PullRequestTask> dataProvider = new EntityDataProvider<PullRequestTask>(
				PullRequestTask.class, new SortParam<String>("date", false)) {

				@Override
				protected void restrict(EntityCriteria<? extends AbstractEntity> criteria) {
					criteria.add(Restrictions.eq("user", getAccount()));
				}
			
		};
		
		SelectionColumn<PullRequestTask, String> selectionColumn;
		
		List<IColumn<PullRequestTask, String>> columns = new ArrayList<>();
		selectionColumn = new SelectionColumn<PullRequestTask, String>();
		if (getAccount().equals(getLoginUser()))
			columns.add(selectionColumn);
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("Pull Request"), "request") {

			@Override
			public void populateItem(
					Item<ICellPopulator<PullRequestTask>> cellItem,
					String componentId, IModel<PullRequestTask> rowModel) {
				PullRequestTask task = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "requestFrag", TaskListPage.this);
				fragment.add(new BookmarkablePageLink<Void>("link", RequestOverviewPage.class, 
						RequestOverviewPage.paramsOf(task.getRequest())) {

					@Override
					public IModel<?> getBody() {
						return Model.of(rowModel.getObject().getRequest().getTitle());
					}
					
				});
				cellItem.add(fragment);
			}
			
		});
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("To Branch")) {

			@Override
			public void populateItem(
					Item<ICellPopulator<PullRequestTask>> cellItem,
					String componentId, IModel<PullRequestTask> rowModel) {
				PullRequest request = rowModel.getObject().getRequest();
				cellItem.add(new BranchLink(componentId, request.getTarget()) {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("a");
					}
					
				});
			}
			
		});
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("From Branch")) {

			@Override
			public void populateItem(
					Item<ICellPopulator<PullRequestTask>> cellItem,
					String componentId, IModel<PullRequestTask> rowModel) {
				PullRequest request = rowModel.getObject().getRequest();
				if (request.getSource() != null) {
					cellItem.add(new BranchLink(componentId, request.getSource()) {
	
						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("a");
						}
						
					});
				} else {
					cellItem.add(new Label(componentId, request.getSource().getFQN()));
				}
			}
			
		});
		columns.add(new AbstractColumn<PullRequestTask, String>(Model.of("Type"), "type") {

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
				new DefaultDataTable<PullRequestTask, String>("tasks", columns, dataProvider, Constants.DEFAULT_PAGE_SIZE);
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
				setResponsePage(TaskListPage.class, TaskListPage.paramsOf(getAccount()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getAccount().equals(getLoginUser()) && dataTable.getRowCount() != 0);
			}
			
		});
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
		else
			setResponsePage(TaskListPage.class, paramsOf(account));
	}

}