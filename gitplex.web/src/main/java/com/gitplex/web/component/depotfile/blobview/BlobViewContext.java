package com.gitplex.web.component.depotfile.blobview;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.core.entity.CodeComment;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.support.TextRange;
import com.gitplex.search.hit.QueryHit;
import com.gitplex.commons.git.BlobIdent;
import com.gitplex.commons.lang.extractors.TokenPosition;

public interface BlobViewContext extends Serializable {

	public enum Mode {BLAME, EDIT, DELETE}
	
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
	
	void onBlameChange(AjaxRequestTarget target, boolean blame);
	
	void onDelete(AjaxRequestTarget target);
	
	void onEdit(AjaxRequestTarget target);
	
	void onCommentOpened(AjaxRequestTarget target, @Nullable CodeComment comment);

	void onAddComment(AjaxRequestTarget target, TextRange mark);
	
	@Nullable
	CodeComment getOpenComment();
	
	RevCommit getCommit();
	
}
