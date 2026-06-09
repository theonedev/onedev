package io.onedev.server.web.component.issue.workspaces;

import javax.inject.Inject;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.IssueService;
import io.onedev.server.web.component.workspace.CreateWorkspaceLink;

abstract class NoBranchWorkspacesPanel extends Panel {

    @Inject
    private IssueService issueService;

    private final IModel<String> branchModel = new LoadableDetachableModel<>() {

        @Override
        protected String load() {
            return issueService.ensureBranch(SecurityUtils.getSubject(), getIssue());
        }

    };

    public NoBranchWorkspacesPanel(String id) {
        super(id);
    }

	@Override
	protected void onInitialize() {
		super.onInitialize();

		RepeatingView specsView = new RepeatingView("specs");
		add(specsView);
		for (WorkspaceSpec spec : getIssue().getProject().getHierarchyWorkspaceSpecs()) {
			WebMarkupContainer specItem = new WebMarkupContainer(specsView.newChildId());
			specItem.add(new Label("name", spec.getName()));

			specItem.add(new CreateWorkspaceLink("create") {

				@Override
				protected Project getProject() {
					return getIssue().getProject();
				}

				@Override
				protected WorkspaceSpec getSpec() {
					return spec;
				}

				@Override
				protected String getBranch() {
					return branchModel.getObject();
				}

				@Override
				protected ObjectId getCommitId() {
					return getIssue().getProject().getObjectId(getBranch(), true);
				}
				
			});
			specItem.add(new Label("description", spec.getDescription()).setVisible(spec.getDescription() != null));

			specItem.setOutputMarkupId(true);
			specsView.add(specItem);
		}
	}    

    @Override
    protected void onDetach() {
        branchModel.detach();
        super.onDetach();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new NoBranchWorkspacesCssResourceReference()));
    }

    protected abstract Issue getIssue();

}
