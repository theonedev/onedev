package com.gitplex.server.web.page.depot.blob.render.renderers.noname;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.edit.BlobEditPanel;

@SuppressWarnings("serial")
public class NoNameEditPanel extends BlobEditPanel {

	public NoNameEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new NoNameCssResourceReference()));
	}

	@Override
	protected FormComponentPanel<byte[]> newContentEditor(String componentId, byte[] initialContent) {
		return new NoNameFormComponent(componentId);
	}

}
