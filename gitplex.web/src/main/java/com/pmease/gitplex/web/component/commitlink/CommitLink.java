package com.pmease.gitplex.web.component.commitlink;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitPage;

@SuppressWarnings("serial")
public class CommitLink extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String hash;
	
	public CommitLink(String id, IModel<Repository> repoModel, String hash) {
		super(id);
		this.repoModel = repoModel;
		this.hash = hash;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<?> link = new BookmarkablePageLink<Void>("link",
				RepoCommitPage.class,
				RepoCommitPage.paramsOf(repoModel.getObject(), hash));
		link.add(new Label("label", GitUtils.abbreviateSHA(hash)));
		add(link);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		super.onDetach();
	}

}
