package io.onedev.server.web.component.diff.blob.pdf;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.git.BlobChange;
import io.onedev.server.web.component.diff.blob.DiffRenderer;
import io.onedev.server.web.component.diff.revision.DiffViewMode;

public class PdfDiffRenderer implements DiffRenderer {

	@Override
	public Panel render(String panelId, MediaType mediaType, BlobChange change, DiffViewMode viewMode) {
		if (mediaType.equals(MediaType.application("pdf")))
			return new BlobPdfDiffPanel(panelId, change);
		else
			return null;
	}

}
