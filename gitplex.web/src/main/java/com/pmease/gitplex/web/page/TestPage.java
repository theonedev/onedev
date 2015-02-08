package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.pmease.commons.wicket.CommonPage;
import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
	}

}
