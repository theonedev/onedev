package com.gitplex.server.web.component.diff.blob.image;

import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.git.BlobChange;

@SuppressWarnings("serial")
public class ImageDiffPanel extends Panel {

	public ImageDiffPanel(String id, BlobChange change) {
		super(id);
	}

}
