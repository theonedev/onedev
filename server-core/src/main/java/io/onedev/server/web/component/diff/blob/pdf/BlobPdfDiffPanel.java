package io.onedev.server.web.component.diff.blob.pdf;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.web.asset.pdfview.PdfViewResourceReference;
import io.onedev.server.web.resource.RawBlobResource;
import io.onedev.server.web.resource.RawBlobResourceReference;

public class BlobPdfDiffPanel extends Panel {

	private final BlobChange change;

	public BlobPdfDiffPanel(String id, BlobChange change) {
		super(id);
		this.change = change;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String oldContainerId = "pdf-diff-old-" + getMarkupId();
		String newContainerId = "pdf-diff-new-" + getMarkupId();

		WebMarkupContainer oldContainer = new WebMarkupContainer("old");
		oldContainer.setMarkupId(oldContainerId);
		oldContainer.setOutputMarkupId(true);
		oldContainer.setVisible(isPresent(change.getOldBlobIdent()));
		add(oldContainer);

		WebMarkupContainer newContainer = new WebMarkupContainer("new");
		newContainer.setMarkupId(newContainerId);
		newContainer.setOutputMarkupId(true);
		newContainer.setVisible(isPresent(change.getNewBlobIdent()));
		add(newContainer);

		Label oldPlaceholder = new Label("oldPlaceholder", _T("Not available"));
		oldPlaceholder.setVisible(!oldContainer.isVisible());
		add(oldPlaceholder);

		Label newPlaceholder = new Label("newPlaceholder", _T("Not available"));
		newPlaceholder.setVisible(!newContainer.isVisible());
		add(newPlaceholder);

		add(AttributeAppender.append("class", "border border-top-0 rounded-bottom blob-pdf-diff d-flex"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new PdfViewResourceReference()));

		BlobIdent oldBlobIdent = change.getOldBlobIdent();
		if (isPresent(oldBlobIdent)) {
			String oldContainerId = "pdf-diff-old-" + getMarkupId();
			String oldPdfUrl = RequestCycle.get().urlFor(new RawBlobResourceReference(),
					RawBlobResource.paramsOf(change.getProject(), oldBlobIdent)).toString();
			String script = String.format("onedev.server.pdfView.render('%s', '%s');",
					oldContainerId, JavaScriptEscape.escapeJavaScript(oldPdfUrl));
			response.render(OnDomReadyHeaderItem.forScript(script));
		}

		BlobIdent newBlobIdent = change.getNewBlobIdent();
		if (isPresent(newBlobIdent)) {
			String newContainerId = "pdf-diff-new-" + getMarkupId();
			String newPdfUrl = RequestCycle.get().urlFor(new RawBlobResourceReference(),
					RawBlobResource.paramsOf(change.getProject(), newBlobIdent)).toString();
			String script = String.format("onedev.server.pdfView.render('%s', '%s');",
					newContainerId, JavaScriptEscape.escapeJavaScript(newPdfUrl));
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

	private boolean isPresent(BlobIdent blobIdent) {
		return blobIdent != null && blobIdent.path != null;
	}

}
