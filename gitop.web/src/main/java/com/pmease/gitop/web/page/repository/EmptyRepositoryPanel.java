package com.pmease.gitop.web.page.repository;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.page.repository.source.component.RepositoryPanel;
import com.pmease.gitop.web.util.UrlUtils;

@SuppressWarnings("serial")
public class EmptyRepositoryPanel extends RepositoryPanel {

	public EmptyRepositoryPanel(String id, IModel<Repository> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("new", Model.of(getRepoUrl())));
		add(new Label("existing", Model.of(getRepoUrl())));
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		this.setVisibilityAllowed(!getRepository().git().hasCommits());
	}
	
	private String getRepoUrl() {
		Repository repository = getRepository();
		return UrlUtils.concatSegments(Gitop.getInstance().guessServerUrl(), repository.getPathName() + ".git");
	}
}
