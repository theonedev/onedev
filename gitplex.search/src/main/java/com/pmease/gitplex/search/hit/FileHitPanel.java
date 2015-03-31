package com.pmease.gitplex.search.hit;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class FileHitPanel extends Panel {

	private final FileHit hit;
	
	public FileHitPanel(String id, FileHit hit) {
		super(id);
		
		this.hit = hit;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("path", hit.getBlobPath()));
	}

}
