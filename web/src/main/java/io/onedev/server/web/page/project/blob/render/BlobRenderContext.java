package io.onedev.server.web.page.project.blob.render;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.jsymbol.TokenPosition;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.TextRange;
import io.onedev.server.search.hit.QueryHit;

public interface BlobRenderContext extends Serializable {

	public enum Mode {VIEW, BLAME, ADD, EDIT, DELETE}
	
	Project getProject();
	
	@Nullable
	PullRequest getPullRequest();

	BlobIdent getBlobIdent();
	
	@Nullable
	TextRange getMark();
	
	void onMark(AjaxRequestTarget target, TextRange mark);
	
	String getMarkUrl(TextRange mark);
	
	/**
	 * Base path of current blob, which can be used to calculate relative paths 
	 * for other files referenced by current blob
	 * 
	 * @return
	 * 			base path of current blob for relative path calculation
	 * 
	 * @return
	 */
	@Nullable
	String getBasePath();
	
	/**
	 * Base url of current blob, which can be used to calculate relative urls 
	 * for other files referenced by current blob
	 * 
	 * @return
	 * 			base url of current blob for relative url calculation
	 */
	String getBaseUrl();
	
	String getRootUrl();
	
	Mode getMode();

	boolean isOnBranch();
	
	void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable TokenPosition tokenPos);
	
	void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
	
	void onModeChange(AjaxRequestTarget target, Mode mode);
	
	void onCommitted(AjaxRequestTarget target, RefUpdated refUpdated);
	
	void onCommentOpened(AjaxRequestTarget target, @Nullable CodeComment comment);

	void onAddComment(AjaxRequestTarget target, TextRange mark);
	
	RefUpdated uploadFiles(Collection<FileUpload> uploads, @Nullable String directory, String commitMessage);
	
	@Nullable
	CodeComment getOpenComment();
	
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
	
}
