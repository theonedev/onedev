package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.markdown.MarkdownBehavior;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}		

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(MarkdownBehavior.class, "bootstrap-markdown.js")));
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(MarkdownBehavior.class, "bootstrap-markdown.min.css")));
	}
	
}
