package com.pmease.gitop.web.page.home;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.web.common.component.modal.Modal;
import com.pmease.gitop.web.page.BasePage;

@SuppressWarnings("serial")
public class TestPanel extends Panel {

	public TestPanel(String id) {
		super(id);
		
		add(new Label("sen", "I love this game"));
		add(new AjaxLink<Void>("submit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Modal modal = ((BasePage) getPage()).getModal();
				modal.hide(target);
				
//				modal.setContent(new Label(Modal.CONTENT_ID, "I like it"));
//				modal.show(target);
			}
			
		});
	}

}
