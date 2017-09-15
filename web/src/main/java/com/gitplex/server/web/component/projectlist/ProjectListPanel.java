package com.gitplex.server.web.component.projectlist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.datatable.HistoryAwareNavToolbar;
import com.gitplex.server.web.component.link.UserLink;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;
import com.gitplex.server.web.util.DateUtils;
import com.gitplex.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class ProjectListPanel extends Panel {

	private final IModel<List<ProjectFacade>> projectsModel;
	
	private final PagingHistorySupport pagingHistorySupport;
	
	public ProjectListPanel(String id, IModel<List<ProjectFacade>> projectsModel, 
			@Nullable PagingHistorySupport pagingHistorySupport) {
		super(id);
		this.projectsModel = projectsModel;
		this.pagingHistorySupport = pagingHistorySupport;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<ProjectFacade, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ProjectFacade, Void>(Model.of("Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectFacade>> cellItem, String componentId, 
					IModel<ProjectFacade> rowModel) {
				Fragment fragment = new Fragment(componentId, "projectFrag", ProjectListPanel.this);
				Project project = GitPlex.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				Link<Void> link = new BookmarkablePageLink<Void>("link", ProjectBlobPage.class, 
						ProjectBlobPage.paramsOf(project)); 
				link.add(new Label("name", project.getName()));
				fragment.add(link);
				cellItem.add(fragment);
				cellItem.add(AttributeAppender.append("class", "project"));
			}
		});

		columns.add(new AbstractColumn<ProjectFacade, Void>(Model.of("Last Author")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectFacade>> cellItem, String componentId, 
					IModel<ProjectFacade> rowModel) {
				Project project = GitPlex.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				RevCommit lastCommit = project.getLastCommit();
				if (lastCommit != null) {
					Fragment fragment = new Fragment(componentId, "authorFrag", ProjectListPanel.this);
					fragment.add(new AvatarLink("avatar", lastCommit.getAuthorIdent()));
					fragment.add(new UserLink("name", lastCommit.getAuthorIdent()));
					cellItem.add(fragment);
				} else {
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
				}
				cellItem.add(AttributeAppender.append("class", "author"));
			}
		});
		
		columns.add(new AbstractColumn<ProjectFacade, Void>(Model.of("Last Commit Message")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectFacade>> cellItem, String componentId, 
					IModel<ProjectFacade> rowModel) {
				Project project = GitPlex.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				RevCommit lastCommit = project.getLastCommit();
				if (lastCommit != null) {
					Fragment fragment = new Fragment(componentId, "commitMessageFrag", ProjectListPanel.this);
					PageParameters params = CommitDetailPage.paramsOf(project, lastCommit.name());
					Link<Void> link = new BookmarkablePageLink<Void>("link", CommitDetailPage.class, params);
					link.add(new Label("message", lastCommit.getShortMessage()));
					fragment.add(link);
					cellItem.add(fragment);
				} else {
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
				}
				cellItem.add(AttributeAppender.append("class", "commit-message"));
			}
		});
		
		columns.add(new AbstractColumn<ProjectFacade, Void>(Model.of("Last Commit Date")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectFacade>> cellItem, String componentId, 
					IModel<ProjectFacade> rowModel) {
				Project project = GitPlex.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				RevCommit lastCommit = project.getLastCommit();
				if (lastCommit != null) {
					cellItem.add(new Label(componentId, DateUtils.formatAge(lastCommit.getCommitterIdent().getWhen())));
				} else {
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
				}
				cellItem.add(AttributeAppender.append("class", "commit-date"));
			}
		});
		
		SortableDataProvider<ProjectFacade, Void> dataProvider = new SortableDataProvider<ProjectFacade, Void>() {

			@Override
			public Iterator<? extends ProjectFacade> iterator(long first, long count) {
				List<ProjectFacade> projects;
				projects = projectsModel.getObject();
				if (first + count <= projects.size())
					return projects.subList((int)first, (int)(first+count)).iterator();
				else
					return projects.subList((int)first, projects.size()).iterator();
			}

			@Override
			public long size() {
				return projectsModel.getObject().size();
			}

			@Override
			public IModel<ProjectFacade> model(ProjectFacade object) {
				return Model.of(object);
			}
		};
		
		DataTable<ProjectFacade, Void> projectsTable = 
				new DataTable<ProjectFacade, Void>("projects", columns, dataProvider, WebConstants.PAGE_SIZE);		
		
		if (pagingHistorySupport != null)
			projectsTable.setCurrentPage(pagingHistorySupport.getCurrentPage());
		
		projectsTable.addBottomToolbar(new HistoryAwareNavToolbar(projectsTable, pagingHistorySupport));
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
