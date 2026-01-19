package io.onedev.server.web.page.project.blob.render.renderers.pdf;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.web.asset.pdfview.PdfViewResourceReference;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;
import io.onedev.server.web.resource.RawBlobResource;
import io.onedev.server.web.resource.RawBlobResourceReference;

public class PdfViewPanel extends BlobViewPanel {

	public PdfViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("pdf").setOutputMarkupId(true));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new PdfViewResourceReference()));
		
		String pdfUrl = RequestCycle.get().urlFor(new RawBlobResourceReference(), 
				RawBlobResource.paramsOf(context.getProject(), context.getBlobIdent())).toString();
		
		String script = String.format("onedev.server.pdfView.render('%s', '%s');", 
				get("pdf").getMarkupId(), JavaScriptEscape.escapeJavaScript(pdfUrl));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected boolean isEditSupported() {
		return false;
	}

}
