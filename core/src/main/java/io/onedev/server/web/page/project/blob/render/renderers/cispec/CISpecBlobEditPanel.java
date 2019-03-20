package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import io.onedev.server.ci.CISpec;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;

@SuppressWarnings("serial")
public class CISpecBlobEditPanel extends BlobEditPanel {

	public CISpecBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CISpecResourceReference()));
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new CISpecEditPanel(componentId, context, initialContent);
	}

	@Override
	protected PlainEditSupport getPlainEditSupport() {
		return new PlainEditSupport() {

			@Override
			public FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
				return new PlainEditPanel(componentId, CISpec.BLOB_PATH, initialContent);
			}
			
		};
	}

}
