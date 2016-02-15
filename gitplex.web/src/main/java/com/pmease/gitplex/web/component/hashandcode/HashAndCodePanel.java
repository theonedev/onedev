package com.pmease.gitplex.web.component.hashandcode;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;

@SuppressWarnings("serial")
public class HashAndCodePanel extends Panel {

	private final IModel<Depot> repoModel;
	
	private final Long requestId;
	
	private final String hash;
	
	private final String path;
	
	public HashAndCodePanel(String id, IModel<Depot> repoModel, String hash) {
		this(id, repoModel, hash, null);
	}
	
	public HashAndCodePanel(String id, IModel<Depot> repoModel, String hash, 
			@Nullable String path) {
		this(id, repoModel, hash, path, null);
	}

	public HashAndCodePanel(String id, IModel<Depot> repoModel, String hash, 
			@Nullable String path, @Nullable Long requestId) {
		super(id);
		
		this.repoModel = repoModel;
		this.requestId = requestId;
		this.hash = hash;
		this.path = path;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new CommitHashPanel("hash", hash));
		
		RepoFileState state = new RepoFileState();
		state.requestId = requestId;
		state.blobIdent.revision = hash;
		state.blobIdent.path = path;
		add(new BookmarkablePageLink<Void>("code", RepoFilePage.class, 
				RepoFilePage.paramsOf(repoModel.getObject(), state)));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				HashAndCodePanel.class, "hash-and-code.css")));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
