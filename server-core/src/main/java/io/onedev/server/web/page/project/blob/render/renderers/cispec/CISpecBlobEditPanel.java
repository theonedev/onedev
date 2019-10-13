package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import org.apache.wicket.markup.html.form.FormComponentPanel;

import io.onedev.server.ci.CISpec;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;

@SuppressWarnings("serial")
public class CISpecBlobEditPanel extends BlobEditPanel implements PlainEditSupport {

	public CISpecBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new CISpecEditPanel(componentId, context, initialContent);
	}

	@Override
	public FormComponentPanel<byte[]> newPlainEditor(String componentId, byte[] initialContent) {
		return new PlainEditPanel(componentId, CISpec.BLOB_PATH, initialContent);
	}

}
