package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import org.apache.wicket.markup.html.form.FormComponentPanel;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;

@SuppressWarnings("serial")
public class BuildSpecBlobEditPanel extends BlobEditPanel implements PlainEditSupport {

	public BuildSpecBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new BuildSpecEditPanel(componentId, context, initialContent);
	}

	@Override
	public FormComponentPanel<byte[]> newPlainEditor(String componentId, byte[] initialContent) {
		return new PlainEditPanel(componentId, BuildSpec.BLOB_PATH, initialContent);
	}

}
