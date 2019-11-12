package io.onedev.server.web.component.branch;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class BranchLink extends ViewStateAwarePageLink<Void> {

	private final ProjectAndBranch projectAndBranch;
	
	public BranchLink(String id, ProjectAndBranch projectAndBranch, @Nullable PullRequest request) {
		super(id, ProjectBlobPage.class, paramsOf(projectAndBranch, request));
		this.projectAndBranch = projectAndBranch;
	}
	
	private static PageParameters paramsOf(ProjectAndBranch projectAndBranch, PullRequest request) {
		BlobIdent blobIdent = new BlobIdent(projectAndBranch.getBranch(), null, FileMode.TREE.getBits());
		ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
		state.requestId = PullRequest.idOf(request);
		return ProjectBlobPage.paramsOf(projectAndBranch.getProject(), state);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.canReadCode(projectAndBranch.getProject()) 
				&& projectAndBranch.getObjectName(false) != null);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		configure();
		if (!isEnabled()) {
			tag.setName("span");
		}
	}

	@Override
	public IModel<?> getBody() {
		String label;
		if (getPage() instanceof ProjectPage) {
			ProjectPage page = (ProjectPage) getPage();
			if (page.getProject().equals(projectAndBranch.getProject())) 
				label = projectAndBranch.getBranch();
			else 
				label = projectAndBranch.getFQN();
		} else {
			label = projectAndBranch.getFQN();
		}
		return Model.of(label);
	}

}
