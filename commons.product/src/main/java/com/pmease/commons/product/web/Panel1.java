package com.pmease.commons.product.web;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public class Panel1 extends Panel {

	public Panel1(String id) {
		super(id);
	}

	@SuppressWarnings("serial")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("link1") {

			@Override
			public void onClick() {
				System.out.println("panel1");
			}
			
		});
	}

	private static final long serialVersionUID = 1L;

}
