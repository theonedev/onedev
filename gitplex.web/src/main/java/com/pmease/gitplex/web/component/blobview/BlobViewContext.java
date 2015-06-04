package com.pmease.gitplex.web.component.blobview;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.hit.QueryHit;

public abstract class BlobViewContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract Repository getRepository();
	
	public abstract BlobIdent getBlobIdent();
	
	public abstract int getLine();
	
	public abstract void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, int line);
	
	public abstract void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
	
	public Blob getBlob() {
		return getRepository().getBlob(getBlobIdent());
	}
	
	public Panel render(String panelId) {
		for (BlobRenderer renderer: GitPlex.getExtensions(BlobRenderer.class)) {
			Panel panel = renderer.render(panelId, this);
			if (panel != null)
				return panel;
		}
		
		throw new IllegalStateException("No applicable blob renderer found for current context.");
	}
	
}
