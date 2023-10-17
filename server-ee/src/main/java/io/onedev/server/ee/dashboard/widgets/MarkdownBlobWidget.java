package io.onedev.server.ee.dashboard.widgets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.BlobChoice;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.annotation.RevisionChoice;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.Widget;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.upload.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Editable(name="Markdown from file", order=10010)
public class MarkdownBlobWidget extends Widget {

	private static final long serialVersionUID = 1L;
	
	private String projectPath;
	
	private String revision;
	
	private String filePath;

	@Editable(order=100, name="Project")
	@ProjectChoice("getPermittedProjects")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	@SuppressWarnings("unused")
	private static List<Project> getPermittedProjects() {
		List<Project> projects = new ArrayList<>(getProjectManager().getPermittedProjects(new ReadCode()));
		Collections.sort(projects, getProjectManager().cloneCache().comparingPath());
		return projects;
	}

	@Editable(order=200)
	@RevisionChoice("getCurrentProject")
	@NotEmpty
	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
	
	private static Project getCurrentProject() {
		String projectPath = (String) EditContext.get().getInputValue("projectPath");
		if (projectPath != null)
			return getProjectManager().findByPath(projectPath);
		else
			return null;
	}

	@Editable(order=300)
	@BlobChoice(commitProvider="getCurrentCommit", patterns="**/*.md **/*.MD")
	@NotEmpty
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	@SuppressWarnings("unused")
	private static ProjectScopedCommit getCurrentCommit() {
		Project project = getCurrentProject();
		if (project != null) {
			String revision = (String) EditContext.get().getInputValue("revision");
			if (revision != null) 
				return new ProjectScopedCommit(project, project.getRevCommit(revision, true));
			else
				return null;
		} else {
			return null;
		}
	}

	private static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@Override
	protected Component doRender(String componentId) {
		Project project = getProjectManager().findByPath(projectPath);
		if (project == null)
			throw new ExplicitException("Project not found: " + projectPath);
		else if (!SecurityUtils.canReadCode(project))
			throw new ExplicitException("Permission denied");
		
		Long projectId = project.getId();
		
		BlobIdent blobIdent = new BlobIdent(revision, filePath); 
		Blob blob = project.getBlob(blobIdent, false);
		if (blob == null) {
			String message = String.format("File not found (revision: %s, file: %s)", revision, filePath);
			throw new ExplicitException(message);
		}
		
		return new MarkdownViewer(componentId, Model.of(blob.getText().getContent()), null) {

			private static final long serialVersionUID = 1L;

			@Override
			protected BlobRenderContext getRenderContext() {
				return new BlobRenderContext() {

					private static final long serialVersionUID = 1L;

					@Override
					public Project getProject() {
						return getProjectManager().load(projectId);
					}

					@Override
					public BlobIdent getBlobIdent() {
						return blobIdent;
					}

					@Override
					public String getPosition() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getCoverageReport() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getProblemReport() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onPosition(AjaxRequestTarget target, String position) {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getPositionUrl(String position) {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getDirectory() {
						if (blobIdent.path.contains("/")) 
							return StringUtils.substringBeforeLast(blobIdent.path, "/");
						else
							return null;
					}
					
					@Override
					public String getDirectoryUrl() {
						BlobIdent blobIdent = new BlobIdent("main", getDirectory(), FileMode.TREE.getBits());
						ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
						return urlFor(ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject(), state)).toString();
					}

					@Override
					public String getRootDirectoryUrl() {
						throw new UnsupportedOperationException();
					}

					@Override
					public Mode getMode() {
						return Mode.VIEW;
					}

					@Override
					public boolean isViewPlain() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getUrlBeforeEdit() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getUrlAfterEdit() {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean isOnBranch() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getRefName() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void pushState(AjaxRequestTarget target, BlobIdent blobIdent, String position) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void replaceState(AjaxRequestTarget target, BlobIdent blobIdent, String position) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, String position) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onModeChange(AjaxRequestTarget target, Mode mode, String newPath) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onModeChange(AjaxRequestTarget target, Mode mode, boolean viewPlain, String newPath) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onCommitted(AjaxRequestTarget target, ObjectId commitId) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onCommentOpened(AjaxRequestTarget target, CodeComment comment, PlanarRange range) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onCommentClosed(AjaxRequestTarget target) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void onAddComment(AjaxRequestTarget target, PlanarRange range) {
						throw new UnsupportedOperationException();
					}

					@Override
					public ObjectId uploadFiles(FileUpload upload, String directory, String commitMessage) {
						throw new UnsupportedOperationException();
					}

					@Override
					public CodeComment getOpenComment() {
						throw new UnsupportedOperationException();
					}

					@Override
					public RevCommit getCommit() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getNewPath() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getInitialNewPath() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String appendRaw(String url) {
						return ProjectBlobPage.doAppendRaw(url);
					}

					@Override
					public PullRequest getPullRequest() {
						throw new UnsupportedOperationException();
					}
					
				};
			}
			
		};
	}

}
