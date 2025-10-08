package io.onedev.server.web.component.link;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

public abstract class BuildSpecLink extends BookmarkablePageLink<Void> {

	private final ObjectId commitId;
	
	public BuildSpecLink(String id, ObjectId commitId) {
		super(id, ProjectBlobPage.class);
		this.commitId = commitId;
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.canReadCode(getProject()));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		if (!isEnabled())
			tag.setName("span");
	}

	protected abstract Project getProject();
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
	@Override
	public PageParameters getPageParameters() {
		ProjectBlobPage.State state = new ProjectBlobPage.State();
		state.blobIdent = new BlobIdent(commitId.name(), BuildSpec.BLOB_PATH, FileMode.REGULAR_FILE.getBits()); 
		state.requestId = PullRequest.idOf(getPullRequest());
		if (getProject().getBlob(state.blobIdent, false) == null)
			state.blobIdent = new BlobIdent(commitId.name(), ".onedev-buildspec", FileMode.REGULAR_FILE.getBits());
		return ProjectBlobPage.paramsOf(getProject(), state);
	}

}
