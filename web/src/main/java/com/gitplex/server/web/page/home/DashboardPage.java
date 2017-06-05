package com.gitplex.server.web.page.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.MembershipFacade;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserAuthorizationFacade;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.link.UserLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.page.layout.NewProjectPage;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;
import com.gitplex.server.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DashboardPage extends LayoutPage {

	private final IModel<List<ProjectFacade>> projectsModel = new LoadableDetachableModel<List<ProjectFacade>>() {

		@Override
		protected List<ProjectFacade> load() {
			CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
			List<ProjectFacade> projects = new ArrayList<>();
			
			if (SecurityUtils.isAdministrator()) {
				projects.addAll(cacheManager.getProjects().values());
			} else {
				for (ProjectFacade project: cacheManager.getProjects().values()) {
					if (project.isPublicRead())
						projects.add(project);
				}
				User user = getLoginUser();
				if (user != null) {
					Collection<Long> groupIds = new HashSet<>();
					for (MembershipFacade membership: cacheManager.getMemberships().values()) {
						if (membership.getUserId().equals(user.getId())) 
							groupIds.add(membership.getGroupId());
					}
					for (GroupAuthorizationFacade authorization: cacheManager.getGroupAuthorizations().values()) {
						if (groupIds.contains(authorization.getGroupId()))
							projects.add(cacheManager.getProject(authorization.getProjectId()));
					}
					for (UserAuthorizationFacade authorization: cacheManager.getUserAuthorizations().values()) {
						if (authorization.getUserId().equals(user.getId()))
							projects.add(cacheManager.getProject(authorization.getProjectId()));
					}
				}
			}
			
			for (Iterator<ProjectFacade> it = projects.iterator(); it.hasNext();) {
				if (!it.next().matchesQuery(searchInput))
					it.remove();
			}
			projects.sort(ProjectFacade::compareLastVisit);
			return projects;
		}
		
	};
	
	private DataTable<ProjectFacade, Void> projectsTable;
	
	private String searchInput;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("filterProjects", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(projectsTable);
			}

		});
		add(new BookmarkablePageLink<Void>("createProject", NewProjectPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canCreateProjects());
			}
			
		});
		
		List<IColumn<ProjectFacade, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ProjectFacade, Void>(Model.of("Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectFacade>> cellItem, String componentId, 
					IModel<ProjectFacade> rowModel) {
				Fragment fragment = new Fragment(componentId, "projectFrag", DashboardPage.this);
				Project project = GitPlex.getInstance(ProjectManager.class).load(rowModel.getObject().getId());
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
						ProjectBlobPage.class, ProjectBlobPage.paramsOf(project)); 
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
					Fragment fragment = new Fragment(componentId, "authorFrag", DashboardPage.this);
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
					Fragment fragment = new Fragment(componentId, "commitMessageFrag", DashboardPage.this);
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
				List<ProjectFacade> projects = projectsModel.getObject();
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
		
		projectsTable = new DataTable<ProjectFacade, Void>("projects", columns, dataProvider, WebConstants.PAGE_SIZE);		
		projectsTable.addBottomToolbar(new AjaxNavigationToolbar(projectsTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new BootstrapAjaxPagingNavigator(navigatorId, table);
			}
			
		});
		projectsTable.addBottomToolbar(new NoRecordsToolbar(projectsTable, Model.of("No Projects Found")));
		projectsTable.setOutputMarkupId(true);
		add(projectsTable);
	}

	@Override
	protected void onDetach() {
		projectsModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DashboardResourceReference()));
	}

	@Override
	protected Component newContextHead(String componentId) {
		return new Label(componentId, "Projects");
	}

}
