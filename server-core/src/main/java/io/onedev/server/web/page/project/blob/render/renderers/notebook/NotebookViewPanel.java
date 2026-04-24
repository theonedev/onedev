package io.onedev.server.web.page.project.blob.render.renderers.notebook;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;
import io.onedev.server.web.resource.RawBlobResource;
import io.onedev.server.web.resource.RawBlobResourceReference;

public class NotebookViewPanel extends BlobViewPanel {

	private static final long serialVersionUID = 1L;

	public NotebookViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebMarkupContainer("notebook").setOutputMarkupId(true));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new NotebookViewResourceReference()));

		String notebookUrl = RequestCycle.get().urlFor(new RawBlobResourceReference(),
				RawBlobResource.paramsOf(context.getProject(), context.getBlobIdent())).toString();
		String directoryUrl = context.getDirectoryUrl();

		String script = String.format("onedev.server.notebookView.render('%s', '%s', '%s');",
				get("notebook").getMarkupId(),
				JavaScriptEscape.escapeJavaScript(notebookUrl),
				JavaScriptEscape.escapeJavaScript(directoryUrl));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected boolean isEditSupported() {
		return true;
	}

}
