package io.onedev.server.web.page.project.blob.render.renderers.markdown;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import io.onedev.server.web.component.diff.text.PlainTextDiffPanel;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;

public class MarkdownBlobEditPanel extends BlobEditPanel {

	public MarkdownBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MarkdownBlobCssResourceReference()));
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new MarkdownBlobEditor(componentId, context, initialContent);
	}

	@Override
	protected Component newChangesViewer(String componentId, byte[] initialContent, byte[] editingContent) {
		var oldLines = Arrays.asList(new String(initialContent, StandardCharsets.UTF_8).split("\n"));
		var newLines = Arrays.asList(new String(editingContent, StandardCharsets.UTF_8).split("\n"));
		return new PlainTextDiffPanel(componentId, oldLines, newLines, true, context.getBlobIdent().path);
	}

}
