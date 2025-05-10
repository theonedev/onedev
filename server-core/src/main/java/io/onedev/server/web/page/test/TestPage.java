package io.onedev.server.web.page.test;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.base.BasePage;

public class TestPage extends BasePage {
		
	public TestPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("test") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				System.out.println(_T("hello world"));
			}	
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.test.onDomReady();"));
	}		

}
