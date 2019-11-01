package io.onedev.server.web.component.project.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class ProjectListPanel extends Panel {

	private final IModel<List<Project>> projectsModel;
	
	private final PagingHistorySupport pagingHistorySupport;
	
	public ProjectListPanel(String id, IModel<List<Project>> projectsModel, 
			@Nullable PagingHistorySupport pagingHistorySupport) {
		super(id);
		this.projectsModel = projectsModel;
		this.pagingHistorySupport = pagingHistorySupport;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<Project, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Fragment fragment = new Fragment(componentId, "projectFrag", ProjectListPanel.this);
				Project project = OneDev.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", ProjectDashboardPage.class, 
						ProjectDashboardPage.paramsOf(project));
				link.add(new ProjectAvatar("avatar", project));
				link.add(new Label("name", project.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "project";
			}
			
		});

		columns.add(new AbstractColumn<Project, Void>(Model.of("Last Author")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Project project = OneDev.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				RevCommit lastCommit = project.getLastCommit();
				if (lastCommit != null) {
					UserIdent userIdent = UserIdent.of(lastCommit.getAuthorIdent());
					cellItem.add(new UserIdentPanel(componentId, userIdent, UserIdentPanel.Mode.AVATAR_AND_NAME));
				} else {
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
				}
			}

			@Override
			public String getCssClass() {
				return "author expanded";
			}
			
		});
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Last Commit Message")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Project project = OneDev.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				RevCommit lastCommit = project.getLastCommit();
				if (lastCommit != null) 
					cellItem.add(new Label(componentId, lastCommit.getShortMessage()));
				else 
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
			}

			@Override
			public String getCssClass() {
				return "commit-message";
			}
			
		});
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Last Commit Date")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Project project = OneDev.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				RevCommit lastCommit = project.getLastCommit();
				if (lastCommit != null) {
					cellItem.add(new Label(componentId, DateUtils.formatAge(lastCommit.getCommitterIdent().getWhen())));
				} else {
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
				}
			}
			
			@Override
			public String getCssClass() {
				return "commit-date expanded";
			}
			
		});
		
		SortableDataProvider<Project, Void> dataProvider = new LoadableDetachableDataProvider<Project, Void>() {

			@Override
			public Iterator<? extends Project> iterator(long first, long count) {
				List<Project> projects;
				projects = projectsModel.getObject();
				if (first + count <= projects.size())
					return projects.subList((int)first, (int)(first+count)).iterator();
				else
					return projects.subList((int)first, projects.size()).iterator();
			}

			@Override
			public long calcSize() {
				return projectsModel.getObject().size();
			}

			@Override
			public IModel<Project> model(Project object) {
				return Model.of(object);
			}
		};
		
		DataTable<Project, Void> projectsTable = 
				new DataTable<Project, Void>("projects", columns, dataProvider, WebConstants.PAGE_SIZE);		
		
		if (pagingHistorySupport != null)
			projectsTable.setCurrentPage(pagingHistorySupport.getCurrentPage());
		
		projectsTable.addBottomToolbar(new NavigationToolbar(projectsTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new HistoryAwarePagingNavigator(navigatorId, table, pagingHistorySupport);
			}
			
		});
		projectsTable.addBottomToolbar(new NoRecordsToolbar(projectsTable, Model.of("No Projects Found")));
		add(projectsTable);
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onDetach() {
		projectsModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectListResourceReference()));
	}

}
