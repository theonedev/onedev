package io.onedev.server.web.component.job;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.renderers.cispec.CISpecRendererProvider;

@SuppressWarnings("serial")
public class JobDefLink extends BookmarkablePageLink<Void> {

	private final IModel<Project> projectModel;
	
	private final ObjectId commitId;
	
	private final String jobName;
	
	public JobDefLink(String id, Project project, ObjectId commitId, String jobName) {
		super(id, ProjectBlobPage.class);
		this.projectModel = new EntityModel<Project>(project);
		this.commitId = commitId;
		this.jobName = jobName;
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.canReadCode(projectModel.getObject()));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		if (!isEnabled())
			tag.setName("span");
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	@Override
	public PageParameters getPageParameters() {
		ProjectBlobPage.State state = new ProjectBlobPage.State();
		state.blobIdent = new BlobIdent(commitId.name(), CISpec.BLOB_PATH, FileMode.REGULAR_FILE.getBits()); 
		state.position = CISpecRendererProvider.getPosition(Job.SELECTION_PREFIX + jobName);
		return ProjectBlobPage.paramsOf(projectModel.getObject(), state);
	}

}
