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
import org.jspecify.annotations.Nullable;

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

	public WorkspaceSpecListPanel(String id) {
		super(id);
	}

	protected abstract Project getProject();

	@Nullable
	protected abstract String getBranch(boolean createIfNotExist);

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var branch = getBranch(false);
		
		RepeatingView specsView = new RepeatingView("specs");
		add(specsView);
		for (WorkspaceSpec spec : getProject().getHierarchyWorkspaceSpecs()) {
			WebMarkupContainer specItem = new WebMarkupContainer(specsView.newChildId());
			specItem.add(new Label("name", spec.getName()));

			if (branch != null) {
				var workspacesModel = new LoadableDetachableModel<List<Workspace>>() {

					@Override
					protected List<Workspace> load() {
						return queryWorkspaces(branch, spec.getName());
					}
	
				};

				String queryString = buildWorkspaceQueryString(branch, spec.getName());
				specItem.add(new BookmarkablePageLink<Void>("showInList", ProjectWorkspacesPage.class,
						ProjectWorkspacesPage.paramsOf(getProject(), queryString, 0)) {
	
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!workspacesModel.getObject().isEmpty());
					}
	
				});	
				specItem.add(new MiniWorkspaceListPanel("detail", workspacesModel));	
			} else {
				specItem.add(new WebMarkupContainer("showInList").setVisible(false));
				specItem.add(new MiniWorkspaceListPanel("detail", new LoadableDetachableModel<List<Workspace>>() {
					@Override
					protected List<Workspace> load() {
						return new ArrayList<>();
					}
				}));
			}

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
					return WorkspaceSpecListPanel.this.getBranch(true);
				}

			});
			specItem.add(new Label("description", spec.getDescription()).setVisible(spec.getDescription() != null));

			specItem.setOutputMarkupId(true);
			specsView.add(specItem);
		}
	}

	private Criteria<Workspace> buildWorkspaceCriteria(String branch, String specName) {
		return Criteria.andCriterias(List.of(
				new ProjectCriteria(getProject().getPath(), Is),
				new SpecCriteria(specName, Is),
				new BranchCriteria(branch, Is)));
	}

	private List<Workspace> queryWorkspaces(String branch, String specName) {
		var query = new WorkspaceQuery(buildWorkspaceCriteria(branch, specName));
		var workspaces = new ArrayList<>(workspaceService.query(
				SecurityUtils.getSubject(), getProject(), query, 0, Integer.MAX_VALUE));
		workspaces.sort(Comparator.comparing(Workspace::getNumber));
		return workspaces;
	}

	private String buildWorkspaceQueryString(String branch, String specName) {
		return new WorkspaceQuery(buildWorkspaceCriteria(branch, specName)).toString();
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
