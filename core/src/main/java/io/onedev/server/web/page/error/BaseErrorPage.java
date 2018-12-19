package io.onedev.server.web.page.error;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public abstract class BaseErrorPage extends BasePage {
	
	public BaseErrorPage(PageParameters params) {
		super(params);
	}

	@Override
	public boolean isErrorPage() {
		return true;
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		target.appendJavaScript("location.reload();");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ErrorPageResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("$('html,body').addClass('error');"));
	}
	
}
