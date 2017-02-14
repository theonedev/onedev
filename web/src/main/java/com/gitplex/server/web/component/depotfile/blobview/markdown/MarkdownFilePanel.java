package com.gitplex.server.web.component.depotfile.blobview.markdown;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;

import com.gitplex.commons.git.Blob;
import com.gitplex.commons.wicket.component.PreventDefaultAjaxLink;
import com.gitplex.commons.wicket.component.markdown.MarkdownPanel;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewContext;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewPanel;

@SuppressWarnings("serial")
public class MarkdownFilePanel extends BlobViewPanel {

	public MarkdownFilePanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		add(new MarkdownPanel("markdown", Model.of(blob.getText().getContent()), null));
	}
	
	@Override
	protected WebMarkupContainer newAdditionalActions(String id) {
		WebMarkupContainer actions = new Fragment(id, "actionsFrag", this);
		actions.add(new PreventDefaultAjaxLink<Void>("blame") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				context.onBlameChange(target, true);									
			}
			
		});
		
		return actions;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new MarkdownFileResourceReference()));
	}

}
