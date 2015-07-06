package com.pmease.gitplex.web.component.blobview;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.lang.TokenPosition;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.page.repository.file.HistoryState;

public abstract class BlobViewContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final HistoryState state;
	
	public BlobViewContext(HistoryState state) {
		this.state = state;
	}
	
	public abstract Repository getRepository();
	
	public abstract void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, TokenPosition tokenPos);
	
	public abstract void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
	
	public abstract void onBlameChange(AjaxRequestTarget target);
	
	public abstract void onEdit(AjaxRequestTarget target, BlobNameChangeCallback callback);
	
	public abstract void onEditDone(AjaxRequestTarget target);
	
	public HistoryState getState() {
		return state;
	}
	
	public Blob getBlob() {
		return getRepository().getBlob(state.file);
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
