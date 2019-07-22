package io.onedev.server.web.page.project.blob.render.renderers.noname;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;

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
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new NoNameFormComponent(componentId);
	}

}
