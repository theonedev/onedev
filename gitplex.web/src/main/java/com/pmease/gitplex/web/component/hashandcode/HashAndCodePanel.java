package com.pmease.gitplex.web.component.hashandcode;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;

@SuppressWarnings("serial")
public class HashAndCodePanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String hash;
	
	public HashAndCodePanel(String id, IModel<Repository> repoModel, String hash) {
		super(id);
		
		this.repoModel = repoModel;
		this.hash = hash;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new CommitHashPanel("hash", Model.of(hash)));
		
		RepoFileState state = new RepoFileState();
		state.blobIdent.revision = hash;
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
