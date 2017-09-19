package com.gitplex.server.web.component.diff.blob.image;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.git.BlobChange;
import com.gitplex.server.web.component.diff.DiffRenderer;
import com.gitplex.server.web.component.diff.revision.DiffViewMode;

public class ImageDiffRenderer implements DiffRenderer {

	@Override
	public Panel render(String panelId, MediaType mediaType, BlobChange change, DiffViewMode viewMode) {
		if (mediaType.getType().equalsIgnoreCase("image"))
			return new ImageDiffPanel(panelId, change);
		else
			return null;
	}

}
