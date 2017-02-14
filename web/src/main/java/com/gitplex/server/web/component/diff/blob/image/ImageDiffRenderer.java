package com.gitplex.server.web.component.diff.blob.image;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.commons.git.BlobChange;
import com.gitplex.server.web.component.diff.DiffRenderer;

public class ImageDiffRenderer implements DiffRenderer {

	@Override
	public Panel render(String panelId, MediaType mediaType, BlobChange change) {
		if (mediaType.getType().equalsIgnoreCase("image"))
			return new ImageDiffPanel(panelId, change);
		else
			return null;
	}

}
