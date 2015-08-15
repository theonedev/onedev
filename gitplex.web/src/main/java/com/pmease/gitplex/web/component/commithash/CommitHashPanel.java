package com.pmease.gitplex.web.component.commithash;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

@SuppressWarnings("serial")
public class CommitHashPanel extends Panel {

	private final IModel<String> commitHashModel;
	
	public CommitHashPanel(String id, IModel<String> commitHashModel) {
		super(id);
		
		this.commitHashModel = commitHashModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new TextField<String>("hash", commitHashModel));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(CommitHashPanel.class, "commit-hash.css")));
	}

	@Override
	protected void onDetach() {
		commitHashModel.detach();
		
		super.onDetach();
	}

}
