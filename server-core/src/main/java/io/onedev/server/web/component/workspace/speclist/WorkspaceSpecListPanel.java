package io.onedev.server.web.component.workspace.speclist;

import static io.onedev.server.search.entity.workspace.WorkspaceQueryLexer.Is;
import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Workspace.Status;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.search.entity.workspace.BranchCriteria;
import io.onedev.server.search.entity.workspace.CommitCriteria;
import io.onedev.server.search.entity.workspace.IssueCriteria;
import io.onedev.server.search.entity.workspace.ProjectCriteria;
import io.onedev.server.search.entity.workspace.PullRequestCriteria;
import io.onedev.server.search.entity.workspace.SpecCriteria;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.workspace.CreateWorkspaceLink;
import io.onedev.server.web.component.workspace.status.WorkspaceStatusIcon;
import io.onedev.server.web.page.project.workspaces.ProjectWorkspacesPage;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.workspace.WorkspaceService;

public abstract class WorkspaceSpecListPanel extends Panel {

	@Inject
	private WorkspaceService workspaceService;

	public WorkspaceSpecListPanel(String id) {
		super(id);
	}

	protected abstract Project getProject();

	@Nullable
	protected abstract String getBranch();

	protected abstract ObjectId getCommitId();

	@Nullable
	protected Issue getIssue() {
		return null;
	}

	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}

	protected boolean isOnInfoVisible() {
		return true;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepeatingView specsView = new RepeatingView("specs");
		add(specsView);
		for (WorkspaceSpec spec : getProject().getHierarchyWorkspaceSpecs()) {
			WebMarkupContainer specItem = new WebMarkupContainer(specsView.newChildId());
			specItem.add(new Label("name", spec.getName()));

			var workspacesModel = new LoadableDetachableModel<List<Workspace>>() {

				@Override
				protected List<Workspace> load() {
					return getWorkspaces(spec.getName());
				}

			};

			String queryString = buildWorkspaceQueryString(spec.getName());
			specItem.add(new BookmarkablePageLink<Void>("showInList", ProjectWorkspacesPage.class,
					ProjectWorkspacesPage.paramsOf(getProject(), queryString, 0)) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!workspacesModel.getObject().isEmpty());
				}

			});	
			specItem.add(newDetail("detail", workspacesModel));

			specItem.add(new CreateWorkspaceLink("create") {

				@Override
				protected Project getProject() {
					return WorkspaceSpecListPanel.this.getProject();
				}

				@Override
				protected WorkspaceSpec getSpec() {
					return spec;
				}

				@Override
				protected Issue getIssue() {
					return WorkspaceSpecListPanel.this.getIssue();
				}

				@Override
				protected PullRequest getPullRequest() {
					return WorkspaceSpecListPanel.this.getPullRequest();
				}

				@Override
				protected String getBranch() {
					return WorkspaceSpecListPanel.this.getBranch();
				}

				@Override
				protected ObjectId getCommitId() {
					return WorkspaceSpecListPanel.this.getCommitId();
				}
				
			});
			specItem.add(new Label("description", spec.getDescription()).setVisible(spec.getDescription() != null));

			specItem.setOutputMarkupId(true);
			specsView.add(specItem);
		}
	}

	private Component newDetail(String componentId, IModel<List<Workspace>> workspacesModel) {
		if (!workspacesModel.getObject().isEmpty()) {
			Fragment fragment = new Fragment(componentId, "hasWorkspacesFrag", this);
			fragment.add(new ListView<Workspace>("workspaces", workspacesModel) {

				@Override
				protected void populateItem(ListItem<Workspace> item) {
					Workspace workspace = item.getModelObject();

					Link<Void> workspaceLink = new BookmarkablePageLink<Void>("workspace",
							WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(workspace));

					Long workspaceId = workspace.getId();
					workspaceLink.add(new WorkspaceStatusIcon("status", new LoadableDetachableModel<Status>() {

						@Override
						protected Status load() {
							return OneDev.getInstance(WorkspaceService.class).load(workspaceId).getStatus();
						}

					}));

					String title = "#" + workspace.getNumber() + " (" + workspace.getUser().getDisplayName();
					if (isOnInfoVisible())
						title += " on " + workspace.getOnDescription();
					title += ")";
					workspaceLink.add(new Label("title", title));
					item.add(workspaceLink);
				}

			});
			return fragment;
		} else {
			return new Label(componentId, _T("No workspaces"))
					.add(AttributeAppender.append("class", "workspace-spec-workspaces no-workspaces font-italic text-nowrap"));
		}
	}

	private Criteria<Workspace> buildWorkspaceCriteria(String specName) {
		var criterias = new ArrayList<Criteria<Workspace>>();
		criterias.add(new ProjectCriteria(getProject().getPath(), Is));
		criterias.add(new SpecCriteria(specName, Is));
		var issue = getIssue();
		if (issue != null) {
			criterias.add(new IssueCriteria(getProject(), issue, Is));
		} else {
			var pullRequest = getPullRequest();
			if (pullRequest != null) {
				criterias.add(new PullRequestCriteria(getProject(), pullRequest, Is));
			} else if (getBranch() != null) {
				var branch = getBranch();
				criterias.add(new BranchCriteria(branch, Is));
			} else {
				criterias.add(new CommitCriteria(getProject(), getCommitId(), Is));
			}
		}
		return Criteria.andCriterias(criterias);
	}

	private List<Workspace> getWorkspaces(String specName) {
		var issue = getIssue();
		if (issue != null) {
			return issue.getWorkspaces().stream()
					.filter(it -> it.getSpecName().equals(specName))
					.sorted(Comparator.comparing(Workspace::getNumber))
					.collect(Collectors.toList());
		} else {
			var pullRequest = getPullRequest();
			if (pullRequest != null) {
				return pullRequest.getWorkspaces().stream()
						.filter(it -> it.getSpecName().equals(specName))
						.sorted(Comparator.comparing(Workspace::getNumber))
						.collect(Collectors.toList());
			} else {
				var query = new WorkspaceQuery(buildWorkspaceCriteria(specName));
				var workspaces = workspaceService.query(SecurityUtils.getSubject(), getProject(), query, 0, Integer.MAX_VALUE);
				return workspaces.stream()
						.sorted(Comparator.comparing(Workspace::getNumber))
						.collect(Collectors.toList());
			}
		}
	}

	private String buildWorkspaceQueryString(String specName) {
		return new WorkspaceQuery(buildWorkspaceCriteria(specName)).toString();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!getProject().getHierarchyWorkspaceSpecs().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WorkspaceSpecListCssResourceReference()));
	}
	
}
