package com.gitplex.web.component.diff.blob.image;

import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.commons.git.BlobChange;

@SuppressWarnings("serial")
public class ImageDiffPanel extends Panel {

	public ImageDiffPanel(String id, BlobChange change) {
		super(id);
	}

}
