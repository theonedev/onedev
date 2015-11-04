package com.pmease.gitplex.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Fragment;

import com.pmease.commons.wicket.component.modal.ModalPanel;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("test") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ModalPanel(target, ModalPanel.Size.SMALL) {

					@Override
					protected Component newContent(String id) {
						Fragment fragment = new Fragment(id, "contentFrag", TestPage.this);
						fragment.add(new AjaxLink<Void>("close") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								close(target);
							}
							
						});
						return fragment;
					}
					
				};
			}
			
		});
	}

}
