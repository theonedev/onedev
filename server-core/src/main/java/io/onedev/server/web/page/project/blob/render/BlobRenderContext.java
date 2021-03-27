package io.onedev.server.web.page.project.blob.render;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.code.hit.QueryHit;

public interface BlobRenderContext extends Serializable {

	public enum Mode {
		VIEW, 
		BLAME, 
		ADD, 
		EDIT, 
		DELETE}
	
	Project getProject();
	
	@Nullable
	PullRequest getPullRequest();

	BlobIdent getBlobIdent();
	
	@Nullable
	String getPosition();
	
	@Nullable
	String getCoverageReport();
	
	@Nullable
	String getProblemReport();
	
	void onPosition(AjaxRequestTarget target, String position);
	
	String getPositionUrl(String position);
	
	/**
	 * Get directory of the blob. If the blob itself is a directory, the blob path will be returned instead
	 * 
	 * @return
	 * 			directory of current blob, or <tt>null</tt> for repository root 
	 * 
	 * @return
	 */
	@Nullable
	String getDirectory();
	
	/**
	 * Url to directory of the blob. Refer to {@link #getDirectory()}
	 * 
	 * @return
	 * 			url to directory of the blob
	 */
	String getDirectoryUrl();
	
	String getRootDirectoryUrl();
	
	Mode getMode();
	
	boolean isViewPlain();
	
	@Nullable
	String getUrlBeforeEdit();
	
	@Nullable
	String getUrlAfterEdit();
	
	boolean isOnBranch();
	
	@Nullable
	String getRefName();
	
	void pushState(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable String position);
	
	void replaceState(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable String position);
	
	void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable String position);
	
	void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
	
	void onModeChange(AjaxRequestTarget target, Mode mode, @Nullable String newPath);
	
	void onModeChange(AjaxRequestTarget target, Mode mode, boolean viewPlain, @Nullable String newPath);
	
	void onCommitted(@Nullable AjaxRequestTarget target, RefUpdated refUpdated);
	
	void onCommentOpened(AjaxRequestTarget target, CodeComment comment, PlanarRange range);

	void onCommentClosed(AjaxRequestTarget target);
	
	void onAddComment(AjaxRequestTarget target, PlanarRange range);
	
	RefUpdated uploadFiles(Collection<FileUpload> uploads, @Nullable String directory, String commitMessage);
	
	@Nullable
	CodeComment getOpenComment();
	
	/**
	 * @return
	 * 			null when there is no commit yet
	 */
	@Nullable
	RevCommit getCommit();
	
	/**
	 * Get new path of the file being added or edited. 
	 * 
	 * @return
	 * 			new path of the file being added/edited, or <tt>null</tt> if new path is not specified yet
	 * @throws
	 * 			IllegalStateException if file is not being added/edited
	 */
	@Nullable
	String getNewPath();
	
	/**
	 * Get initial new path when add a file
	 * 
	 * @return
	 * 			initial path of the file being added, or <tt>null</tt> if no initial path is specified
	 */
	@Nullable
	String getInitialNewPath();
	
	String getAutosaveKey();
	
	String appendRaw(String url);
}
