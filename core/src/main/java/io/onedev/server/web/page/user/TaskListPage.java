package io.onedev.server.web.page.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
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
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Task;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.RequestStatusLabel;
import io.onedev.server.web.component.datatable.EntityDataProvider;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.SelectionColumn;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.overview.RequestOverviewPage;
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

		EntityDataProvider<Task> dataProvider = new EntityDataProvider<Task>(
				Task.class, new SortParam<String>("date", false)) {

				@Override
				protected void restrict(EntityCriteria<? extends AbstractEntity> criteria) {
					criteria.add(Restrictions.eq("user", getUser()));
				}
			
		};
		
		SelectionColumn<Task, String> selectionColumn;
		
		List<IColumn<Task, String>> columns = new ArrayList<>();
		selectionColumn = new SelectionColumn<Task, String>();
		if (getUser().equals(getLoginUser()))
			columns.add(selectionColumn);
		
		columns.add(new AbstractColumn<Task, String>(Model.of("Project"), "project") {

			@Override
			public void populateItem(Item<ICellPopulator<Task>> cellItem, String componentId, IModel<Task> rowModel) {
				Task task = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "projectFrag", TaskListPage.this);
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", 
						ProjectBlobPage.class, ProjectBlobPage.paramsOf(task.getProject()));
				link.add(new Label("label", task.getProject().getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
			
		});
		columns.add(new AbstractColumn<Task, String>(Model.of("Source"), null) {

			@Override
			public void populateItem(Item<ICellPopulator<Task>> cellItem, String componentId, IModel<Task> rowModel) {
				Task task = rowModel.getObject();
				if (task.getType().equals(Task.TYPE_PULLREQUEST)) {
					PullRequest request = OneDev.getInstance(PullRequestManager.class)
							.find(task.getProject(), Long.valueOf(task.getSource())); 
					if (request != null) {
						Long requestId = request.getId();
						Fragment fragment = new Fragment(componentId, "pullRequestFrag", TaskListPage.this);
						fragment.add(new RequestStatusLabel("status", new LoadableDetachableModel<PullRequest>() {

							@Override
							protected PullRequest load() {
								return OneDev.getInstance(PullRequestManager.class).load(requestId);
							}
							
						}));
						Link<Void> link = new BookmarkablePageLink<Void>("link", 
								RequestOverviewPage.class, RequestOverviewPage.paramsOf(request));
						link.add(new Label("label", "pull request #" + request.getNumberStr() + ": " + request.getTitle()));
						fragment.add(link);
						cellItem.add(fragment);
					} else {
						cellItem.add(new Label(componentId, "pull request #" + task.getSource()));
					}
				} else if (task.getType().equals(Task.TYPE_ISSUE)) {
					Issue issue = OneDev.getInstance(IssueManager.class)
							.find(task.getProject(), Long.valueOf(task.getSource())); 
					if (issue != null) {
						Long issueId = issue.getId();
						Fragment fragment = new Fragment(componentId, "issueFrag", TaskListPage.this);
						fragment.add(new IssueStateLabel("state", new LoadableDetachableModel<Issue>() {

							@Override
							protected Issue load() {
								return OneDev.getInstance(IssueManager.class).load(issueId);
							}
							
						}));
						Link<Void> link = new BookmarkablePageLink<Void>("link", 
								IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue, null));
						link.add(new Label("label", "issue #" + issue.getNumberStr() + ": " + issue.getTitle()));
						fragment.add(link);
						cellItem.add(fragment);
					} else {
						cellItem.add(new Label(componentId, "pull request #" + task.getSource()));
					}
				} else {
					throw new RuntimeException("Unexpected task type: " + task.getType());
				}
			}
			
		});
		columns.add(new AbstractColumn<Task, String>(Model.of("Description"), "description") {

			@Override
			public void populateItem(Item<ICellPopulator<Task>> cellItem, String componentId, IModel<Task> rowModel) {
				Task task = rowModel.getObject();
				cellItem.add(new Label(componentId, task.getDescription()));
			}
			
		});
		columns.add(new AbstractColumn<Task, String>(Model.of("When"), "date") {

			@Override
			public void populateItem(Item<ICellPopulator<Task>> cellItem, String componentId, IModel<Task> rowModel) {
				Task task = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatAge(task.getDate())));
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
		
		DataTable<Task, String> dataTable = new HistoryAwareDataTable<Task, String>("tasks", columns, 
				dataProvider, WebConstants.PAGE_SIZE, pagingHistorySupport);
		add(dataTable);
		
		add(new Link<Void>("deleteSelected") {

			@Override
			public void onClick() {
				if (selectionColumn.getSelections().isEmpty()) {
					getSession().warn("Please select tasks to delete");
				} else {
					for (IModel<Task> model: selectionColumn.getSelections())
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