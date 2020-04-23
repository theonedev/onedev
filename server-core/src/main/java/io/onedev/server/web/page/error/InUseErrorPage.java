package io.onedev.server.web.page.error;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.util.usage.InUseException;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class InUseErrorPage extends BasePage {
	
	private final InUseException exception;
			
	public InUseErrorPage(InUseException exception) {
		super(new PageParameters());
		this.exception = exception;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("inUseDetail", HtmlUtils.formatAsHtml(exception.getMessage())).setEscapeModelStrings(false));
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
