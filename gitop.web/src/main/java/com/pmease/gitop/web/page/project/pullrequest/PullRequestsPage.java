package com.pmease.gitop.web.page.project.pullrequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class PullRequestsPage extends ProjectCategoryPage {

	private DisplayOption displayOption = new DisplayOption();
	
	public PullRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		Fragment fragment = new Fragment("content", "listFrag", this);
		fragment.add(new Link<Void>("newPullRequest") {

			@Override
			public void onClick() {
				Branch target, source = null;
				BranchManager branchManager = Gitop.getInstance(BranchManager.class);
				if (getProject().getForkedFrom() != null) {
					target = branchManager.findDefault(getProject().getForkedFrom());
					source = branchManager.findDefault(getProject());
				} else {
					target = branchManager.findDefault(getProject());
					for (Branch each: getProject().getBranches()) {
						if (!each.equals(target)) {
							source = each;
							break;
						}
					}
					if (source == null)
						source = target;
				}
				User currentUser = AppLoader.getInstance(UserManager.class).getCurrent();
				
				PullRequestsPage.this.replace(new NewPullRequestPanel("content", target, source, currentUser));
			}
			
		});
		AbstractColumn<PullRequest, Void> column = new AbstractColumn<PullRequest, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getTitle()));
			}
			
		};
		List<IColumn<PullRequest, Void>> columns = new ArrayList<>();
		columns.add(column);
		IDataProvider<PullRequest> dataProvider = new IDataProvider<PullRequest>() {

			@Override
			public void detach() {
			}

			@SuppressWarnings("unchecked")
			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				return (Iterator<? extends PullRequest>) Gitop.getInstance(GeneralDao.class).query(
						displayOption.getCriteria(getProject()), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return Gitop.getInstance(GeneralDao.class).count(displayOption.getCriteria(getProject()));
			}

			@Override
			public IModel<PullRequest> model(PullRequest object) {
				final Long pullRequestId = object.getId();
				return new LoadableDetachableModel<PullRequest>() {

					@Override
					protected PullRequest load() {
						return Gitop.getInstance(PullRequestManager.class).load(pullRequestId);
					}
					
				};
			}
			
		};
		DataTable<PullRequest, Void> dataTable = new DataTable<>("pullRequests", columns, dataProvider, Constants.DEFAULT_PAGE_SIZE);
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable));
		fragment.add(dataTable);
		
		add(fragment);
	}

}