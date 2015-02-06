package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.pmease.commons.wicket.CommonPage;
import com.pmease.commons.wicket.assets.ace.HighlightResourceReference;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(HighlightResourceReference.INSTANCE));
		response.render(OnDomReadyHeaderItem.forScript("$('.lang-java').highlight({theme: 'ace/theme/github'}).addClass('hello');"));
	}

}
