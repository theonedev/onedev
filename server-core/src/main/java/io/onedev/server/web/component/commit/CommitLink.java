package io.onedev.server.web.component.commit;

import javax.inject.Inject;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

public class CommitLink extends ViewStateAwarePageLink<Void> {

	@Inject
	private ProjectService projectService;

	private final Long projectId;

	private final ObjectId commitId;

	public CommitLink(String id, Project project, ObjectId commitId) {
		super(id, ProjectBlobPage.class, paramsOf(project, commitId));
		this.projectId = project.getId();
		this.commitId = commitId;
	}
	
	private static PageParameters paramsOf(Project project, ObjectId commitId) {
		BlobIdent blobIdent = new BlobIdent(commitId.name(), null, FileMode.TREE.getBits());
		ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
		return ProjectBlobPage.paramsOf(project, state);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		var project = projectService.load(projectId);
		setEnabled(SecurityUtils.canReadCode(project));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		configure();
		if (!isEnabled())
			tag.setName("span");
	}

	@Override
	public IModel<?> getBody() {
		return Model.of(GitUtils.abbreviateSHA(commitId.name()));
	}

}
