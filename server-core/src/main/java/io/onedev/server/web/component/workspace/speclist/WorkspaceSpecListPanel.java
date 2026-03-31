package io.onedev.server.web.component.workspace.speclist;

import static io.onedev.server.search.entity.workspace.WorkspaceQueryLexer.Is;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.search.entity.workspace.BranchCriteria;
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

	private final String branch;

	public WorkspaceSpecListPanel(String id, String branch) {
		super(id);
		this.branch = branch;
	}

	protected abstract Project getProject();

	@Override
	protected void onInitialize() {
		super.onInitialize();

		RepeatingView specsView = new RepeatingView("specs");
		add(specsView);
		for (WorkspaceSpec spec : getProject().getHierarchyWorkspaceSpecs()) {
			WebMarkupContainer specItem = new WebMarkupContainer(specsView.newChildId());
			specItem.add(new Label("name", spec.getName()));

			List<Workspace> workspaces = queryWorkspaces(spec.getName());

			String queryString = buildWorkspaceQueryString(spec.getName());
			specItem.add(new BookmarkablePageLink<Void>("showInList", ProjectWorkspacesPage.class,
					ProjectWorkspacesPage.paramsOf(getProject(), queryString, 0)) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!workspaces.isEmpty());
				}

			});

			specItem.add(new CreateWorkspaceLink("create", branch) {

				@Override
				protected Project getProject() {
					return WorkspaceSpecListPanel.this.getProject();
				}

				@Override
				protected WorkspaceSpec getSpec() {
					return spec;
				}

			});

			var workspacesModel = new LoadableDetachableModel<List<Workspace>>() {

				@Override
				protected List<Workspace> load() {
					return queryWorkspaces(spec.getName());
				}

			};
			specItem.add(new MiniWorkspaceListPanel("detail", workspacesModel));

			specItem.setOutputMarkupId(true);
			specsView.add(specItem);
		}
	}

	private Criteria<Workspace> buildWorkspaceCriteria(String specName) {
		return Criteria.andCriterias(List.of(
				new ProjectCriteria(getProject().getPath(), Is),
				new SpecCriteria(specName, Is),
				new BranchCriteria(branch, Is)));
	}

	private List<Workspace> queryWorkspaces(String specName) {
		var query = new WorkspaceQuery(buildWorkspaceCriteria(specName));
		var workspaces = new ArrayList<>(workspaceService.query(
				SecurityUtils.getSubject(), getProject(), query, 0, Integer.MAX_VALUE));
		workspaces.sort(Comparator.comparing(Workspace::getNumber));
		return workspaces;
	}

	private String buildWorkspaceQueryString(String specName) {
		return new WorkspaceQuery(buildWorkspaceCriteria(specName)).toString();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!getProject().getHierarchyWorkspaceSpecs().isEmpty());
	}

}
