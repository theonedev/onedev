package io.onedev.server.web.component.workspace.speclist;

import static io.onedev.server.search.entity.workspace.WorkspaceQueryLexer.Is;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.search.entity.workspace.BranchCriteria;
import io.onedev.server.search.entity.workspace.CommitCriteria;
import io.onedev.server.search.entity.workspace.ProjectCriteria;
import io.onedev.server.search.entity.workspace.SpecCriteria;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.workspace.CreateWorkspaceLink;
import io.onedev.server.web.component.workspace.minilist.MiniWorkspaceListPanel;
import io.onedev.server.web.page.project.workspaces.ProjectWorkspacesPage;
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

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var branch = getBranch();
		var commitId = getCommitId();
		
		RepeatingView specsView = new RepeatingView("specs");
		add(specsView);
		for (WorkspaceSpec spec : getProject().getHierarchyWorkspaceSpecs()) {
			WebMarkupContainer specItem = new WebMarkupContainer(specsView.newChildId());
			specItem.add(new Label("name", spec.getName()));

			var workspacesModel = new LoadableDetachableModel<List<Workspace>>() {

				@Override
				protected List<Workspace> load() {
					return queryWorkspaces(commitId, branch, spec.getName());
				}

			};

			String queryString = buildWorkspaceQueryString(commitId, branch, spec.getName());
			specItem.add(new BookmarkablePageLink<Void>("showInList", ProjectWorkspacesPage.class,
					ProjectWorkspacesPage.paramsOf(getProject(), queryString, 0)) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!workspacesModel.getObject().isEmpty());
				}

			});	
			specItem.add(new MiniWorkspaceListPanel("detail", workspacesModel));	

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
				protected String getBranch() {
					return branch;
				}

				@Override
				protected ObjectId getCommitId() {
					return commitId;
				}
				
			});
			specItem.add(new Label("description", spec.getDescription()).setVisible(spec.getDescription() != null));

			specItem.setOutputMarkupId(true);
			specsView.add(specItem);
		}
	}

	private Criteria<Workspace> buildWorkspaceCriteria(ObjectId commitId, @Nullable String branch, String specName) {
		var criterias = new ArrayList<Criteria<Workspace>>();
		criterias.add(new ProjectCriteria(getProject().getPath(), Is));
		criterias.add(new SpecCriteria(specName, Is));
		if (branch != null) 
			criterias.add(new BranchCriteria(branch, Is));
		else 
			criterias.add(new CommitCriteria(getProject(), commitId, Is));
		return Criteria.andCriterias(criterias);
	}

	private List<Workspace> queryWorkspaces(ObjectId commitId, @Nullable String branch, String specName) {
		var query = new WorkspaceQuery(buildWorkspaceCriteria(commitId, branch, specName));
		var workspaces = new ArrayList<>(workspaceService.query(
				SecurityUtils.getSubject(), getProject(), query, 0, Integer.MAX_VALUE));
		workspaces.sort(Comparator.comparing(Workspace::getNumber));
		return workspaces;
	}

	private String buildWorkspaceQueryString(ObjectId commitId, @Nullable String branch, String specName) {
		return new WorkspaceQuery(buildWorkspaceCriteria(commitId, branch, specName)).toString();
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
