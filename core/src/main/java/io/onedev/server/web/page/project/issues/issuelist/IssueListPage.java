package io.onedev.server.web.page.project.issues.issuelist;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;

@SuppressWarnings("serial")
public class IssueListPage extends ProjectPage {

	private DataTable<Issue, Void> issuesTable;
	
	public IssueListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())));
		
		List<IColumn<Issue, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Issue, Void>(Model.of("Id")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId, IModel<Issue> rowModel) {
				cellItem.add(new Label(componentId, "#" + rowModel.getObject().getId()));
			}
		});		
		
		columns.add(new AbstractColumn<Issue, Void>(Model.of("Title")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId, IModel<Issue> rowModel) {
				Fragment fragment = new Fragment(componentId, "titleFrag", IssueListPage.this);
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueDetailPage.class, IssueDetailPage.paramsOf(rowModel.getObject()));
				link.add(new Label("label", Model.of(rowModel.getObject().getTitle())));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});		
		
		columns.add(new AbstractColumn<Issue, Void>(Model.of("State")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId, IModel<Issue> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getState()));
			}
		});		
		
		IDataProvider<Issue> dataProvider = new ListDataProvider<Issue>() {

			@Override
			protected List<Issue> getData() {
				return OneDev.getInstance(IssueManager.class).findAll();
			}

		};
		
		add(issuesTable = new DataTable<Issue, Void>("issues", columns, dataProvider, WebConstants.PAGE_SIZE));
		issuesTable.addTopToolbar(new HeadersToolbar<Void>(issuesTable, null));
		issuesTable.addBottomToolbar(new NoRecordsToolbar(issuesTable));
		issuesTable.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListResourceReference()));
	}
	
}
