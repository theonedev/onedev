package io.onedev.server.web.page.project.issues.issuedetail.reviewrequests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.RequestStatusLabel;
import io.onedev.server.web.component.link.BranchLink;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.activities.RequestActivitiesPage;

@SuppressWarnings("serial")
public class ReviewRequestsPage extends IssueDetailPage {

	private final IModel<List<PullRequest>> requestsModel = new LoadableDetachableModel<List<PullRequest>>() {

		@Override
		protected List<PullRequest> load() {
			List<PullRequest> requests = new ArrayList<>();
			for (Long requestId: getPullRequestIds()) {
				PullRequest request = OneDev.getInstance(PullRequestManager.class).get(requestId);
				if (request != null && !requests.contains(request))
					requests.add(request);
			}
			Collections.sort(requests, new Comparator<PullRequest>() {

				@Override
				public int compare(PullRequest o1, PullRequest o2) {
					return o2.getId().compareTo(o1.getId());
				}
				
			});
			return requests;
		}
		
	};
	
	public ReviewRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		requestsModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<PullRequest, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("#")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId,
					IModel<PullRequest> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getNumber()));
			}
		});
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Title")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId,
					IModel<PullRequest> rowModel) {
				Fragment fragment = new Fragment(componentId, "titleFrag", ReviewRequestsPage.this);
				Link<Void> link = new BookmarkablePageLink<Void>("link", RequestActivitiesPage.class, 
						RequestActivitiesPage.paramsOf(rowModel.getObject(), null));
				link.add(new Label("label", rowModel.getObject().getTitle()));
				fragment.add(link);
				
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("State")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId,
					IModel<PullRequest> rowModel) {
				cellItem.add(new RequestStatusLabel(componentId, rowModel));
			}
			
		});
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Source")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				if (rowModel.getObject().getSource() != null) {
					Fragment fragment = new Fragment(componentId, "sourceFrag", ReviewRequestsPage.this);
					fragment.add(new BranchLink("link", rowModel.getObject().getSource(), rowModel.getObject()));
					cellItem.add(fragment);
				} else {
					cellItem.add(new Label(componentId, "<i>Unknown</i>").setEscapeModelStrings(false));
				}
			}

		});
		
		SortableDataProvider<PullRequest, Void> dataProvider = new SortableDataProvider<PullRequest, Void>() {

			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				return requestsModel.getObject().iterator();
			}

			@Override
			public long size() {
				return requestsModel.getObject().size();
			}

			@Override
			public IModel<PullRequest> model(PullRequest object) {
				Long id = object.getId();
				return new LoadableDetachableModel<PullRequest>() {

					@Override
					protected PullRequest load() {
						return OneDev.getInstance(PullRequestManager.class).load(id);
					}
					
				};
			}
		};
		
		DataTable<PullRequest, Void> buildsTable = new DataTable<PullRequest, Void>("requests", columns, dataProvider, Integer.MAX_VALUE);
		buildsTable.addTopToolbar(new AjaxFallbackHeadersToolbar<Void>(buildsTable, dataProvider));
		buildsTable.addBottomToolbar(new NoRecordsToolbar(buildsTable));
		add(buildsTable);
	}

}
