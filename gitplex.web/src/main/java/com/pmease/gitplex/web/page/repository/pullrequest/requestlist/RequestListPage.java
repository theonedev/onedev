package com.pmease.gitplex.web.page.repository.pullrequest.requestlist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.branchlink.BranchLink;
import com.pmease.gitplex.web.component.pullrequest.requestlink.RequestLink;
import com.pmease.gitplex.web.component.pullrequest.requeststatus.RequestStatusPanel;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.pullrequest.PullRequestPage;
import com.pmease.gitplex.web.page.repository.pullrequest.newrequest.NewRequestPage;

@SuppressWarnings("serial")
public class RequestListPage extends PullRequestPage {

	private DisplayOption displayOption = new DisplayOption();
	
	public RequestListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("newRequest") {

			@Override
			public void onClick() {
				RepositoryPage page = (RepositoryPage) getPage();
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(page.getRepository()));
			}
			
		});
		
		List<IColumn<PullRequest, Void>> columns = new ArrayList<>();
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Id")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				cellItem.add(new Label(componentId, "#" + request.getId()));
				cellItem.add(AttributeAppender.append("class", "narrow"));
			}
			
		});
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Title")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				cellItem.add(new RequestLink(componentId, rowModel));
			}
			
		});
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("To Branch")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, final IModel<PullRequest> rowModel) {
				cellItem.add(new BranchLink(componentId, new LoadableDetachableModel<RepoAndBranch>() {

					@Override
					protected RepoAndBranch load() {
						return rowModel.getObject().getTarget();
					}
					
				}));
			}
			
		});
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("From Branch")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, final IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				if (request.getSourceRepo() != null) {
					cellItem.add(new BranchLink(componentId, new LoadableDetachableModel<RepoAndBranch>(){

						@Override
						protected RepoAndBranch load() {
							return rowModel.getObject().getSource();
						}
						
					}));
				} else {
					cellItem.add(new Label(componentId, "unknown repository"));
				}
			}
			
		});
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Status")) {

			@Override
			public Component getHeader(String componentId) {
				Fragment fragment = new Fragment(componentId, "statusHeaderFrag", RequestListPage.this);
				DropdownPanel helpPanel = new DropdownPanel("helpDropdown", false) {

					@Override
					protected Component newContent(String id) {
						return new Fragment(id, "helpFrag", RequestListPage.this);
					}
					
				};
				fragment.add(helpPanel);
				fragment.add(new WebMarkupContainer("helpDropdownTrigger") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(displayOption.isOpen());
					}
					
				}.add(new DropdownBehavior(helpPanel)));
				return fragment;
			}

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				cellItem.add(new RequestStatusPanel(componentId, rowModel));
				cellItem.add(AttributeAppender.append("class", "narrow"));
			}
			
		});
		IDataProvider<PullRequest> dataProvider = new IDataProvider<PullRequest>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				RepositoryPage page = (RepositoryPage) getPage();
				return GitPlex.getInstance(Dao.class).query(displayOption.getCriteria(page.getRepository(), true), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				RepositoryPage page = (RepositoryPage) getPage();
				return GitPlex.getInstance(Dao.class).count(displayOption.getCriteria(page.getRepository(), false));
			}

			@Override
			public IModel<PullRequest> model(PullRequest object) {
				final Long pullRequestId = object.getId();
				return new LoadableDetachableModel<PullRequest>() {

					@Override
					protected PullRequest load() {
						return GitPlex.getInstance(Dao.class).load(PullRequest.class, pullRequestId);
					}
					
				};
			}
			
		};
		DataTable<PullRequest, Void> dataTable = new DataTable<>("pullRequests", columns, 
				dataProvider, Constants.DEFAULT_PAGE_SIZE);
		dataTable.addTopToolbar(new NavigationToolbar(dataTable));
		dataTable.addTopToolbar(new HeadersToolbar<>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable));
		add(dataTable);		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(RequestListPage.class, "request-list.css")));
	}

}
