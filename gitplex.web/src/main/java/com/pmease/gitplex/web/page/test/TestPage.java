package com.pmease.gitplex.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.pmease.commons.wicket.assets.align.AlignResourceReference;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String lines = "";
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DropdownLink<Void>("test", new Alignment(0, 50, 100, 50, 8, true)) {

			@Override
			protected Component newContent(String id) {
				final Fragment fragment = new Fragment(id, "contentFrag", TestPage.this);
				fragment.add(new AjaxLink<Void>("more") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						lines += "hello world<br>";
						target.add(fragment);
					}
					
				});
				fragment.add(new Label("lines", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return lines;
					}
					
				}).setEscapeModelStrings(false));
				fragment.setOutputMarkupId(true);
				return fragment;
			}

		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(AlignResourceReference.INSTANCE));
	}		

}
