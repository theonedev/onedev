package com.pmease.gitplex.web.component.blobview;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.lang.TokenPosition;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.hit.QueryHit;

public abstract class BlobViewContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/*
	 * Store blobIdent in view context so that we can compare it with 
	 * selected file to avoid re-rendering source view if file is not 
	 * changed 
	 */
	private final BlobIdent blobIdent;
	
	private TokenPosition tokenPosition;
	
	private boolean blame;
	
	public BlobViewContext(BlobIdent blobIdent) {
		this.blobIdent = blobIdent;
	}
	
	public abstract Repository getRepository();
	
	@Nullable
	public TokenPosition getTokenPosition() {
		return tokenPosition;
	}

	public void setTokenPosition(@Nullable TokenPosition tokenPosition) {
		this.tokenPosition = tokenPosition;
	}

	public boolean isBlame() {
		return blame;
	}

	public void setBlame(boolean blame) {
		this.blame = blame;
	}

	public abstract void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, TokenPosition tokenPos);
	
	public abstract void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);

	public BlobIdent getBlobIdent() {
		return blobIdent;
	}
	
	public Blob getBlob() {
		return getRepository().getBlob(blobIdent);
	}
	
	public BlobViewPanel render(String panelId) {
		for (BlobRenderer renderer: GitPlex.getExtensions(BlobRenderer.class)) {
			BlobViewPanel panel = renderer.render(panelId, this);
			if (panel != null)
				return panel;
		}
				
		throw new IllegalStateException("No applicable blob renderer found for current context.");
	}
	
}
