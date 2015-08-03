package com.pmease.gitplex.web.component.diff.blob.image;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.git.BlobChange;

@SuppressWarnings("serial")
public class ImageDiffPanel extends Panel {

	public ImageDiffPanel(String id, BlobChange change) {
		super(id);
	}

}
