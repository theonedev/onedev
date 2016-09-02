package com.pmease.gitplex.web.component.commithash;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class CommitHashPanel extends Panel {

	private final String commitHash;
	
	public CommitHashPanel(String id, String commitHash) {
		super(id);
		
		this.commitHash = commitHash;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new TextField<String>("hash", Model.of(commitHash)));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CommitHashResourceReference()));
	}

}
