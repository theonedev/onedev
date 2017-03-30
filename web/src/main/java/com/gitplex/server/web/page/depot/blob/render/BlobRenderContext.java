package com.gitplex.server.web.page.depot.blob.render;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.TextRange;
import com.gitplex.server.search.hit.QueryHit;

public interface BlobRenderContext extends Serializable {

	public enum Mode {VIEW, BLAME, ADD, EDIT, DELETE}
	
	Depot getDepot();

	BlobIdent getBlobIdent();
	
	@Nullable
	TextRange getMark();
	
	@Nullable
	String getAnchor();
	
	void onMark(AjaxRequestTarget target, TextRange mark);
	
	String getMarkUrl(TextRange mark);
	
	Mode getMode();

	boolean isOnBranch();
	
	void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable TokenPosition tokenPos);
	
	void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
	
	void onModeChange(AjaxRequestTarget target, Mode mode);
	
	void onCommitted(AjaxRequestTarget target, ObjectId oldCommit, ObjectId newCommit);
	
	void onCommentOpened(AjaxRequestTarget target, @Nullable CodeComment comment);

	void onAddComment(AjaxRequestTarget target, TextRange mark);
	
	@Nullable
	CodeComment getOpenComment();
	
	RevCommit getCommit();
	
	/**
	 * Get new path of the file being added or edited. 
	 * 
	 * @return
	 * 			new path of the file being added/edited, or <tt>null</tt> if new path is not specified yet
	 * @throws
	 * 			IllegalStateException if call this method if file is not being added or edited
	 */
	@Nullable
	String getNewPath();
	
	String getAutosaveKey();
}
