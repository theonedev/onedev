package io.onedev.server.web.component.job;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRendererProvider;

@SuppressWarnings("serial")
public abstract class JobDefLink extends BookmarkablePageLink<Void> {

	private final ObjectId commitId;
	
	private final String jobName;
	
	public JobDefLink(String id, ObjectId commitId, String jobName) {
		super(id, ProjectBlobPage.class);
		this.commitId = commitId;
		this.jobName = jobName;
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
	
	@Override
	public PageParameters getPageParameters() {
		ProjectBlobPage.State state = new ProjectBlobPage.State();
		state.blobIdent = new BlobIdent(commitId.name(), BuildSpec.BLOB_PATH, FileMode.REGULAR_FILE.getBits()); 
		if (getProject().getBlob(state.blobIdent, false) == null)
			state.blobIdent = new BlobIdent(commitId.name(), ".onedev-buildspec", FileMode.REGULAR_FILE.getBits());
		state.position = BuildSpecRendererProvider.getPosition(Job.SELECTION_PREFIX + jobName);
		return ProjectBlobPage.paramsOf(getProject(), state);
	}

}
