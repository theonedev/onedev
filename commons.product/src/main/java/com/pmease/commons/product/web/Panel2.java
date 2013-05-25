package com.pmease.commons.product.web;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public class Panel2 extends Panel {

	public Panel2(String id) {
		super(id);
	}

	@SuppressWarnings("serial")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("link2") {

			@Override
			public void onClick() {
				System.out.println("panel2");
			}
			
		});
	}

	private static final long serialVersionUID = 1L;

}
