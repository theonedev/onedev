package io.onedev.server.web.page.simple.error;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.util.usage.InUseException;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class InUseErrorPage extends SimplePage {
	
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
	protected String getTitle() {
		return "Unable To Delete Right Now";
	}

	@Override
	protected String getSubTitle() {
		return "The object you are deleting is still being used";
	}

	@Override
	protected String getLogoHref() {
		return "sad-panda";
	}

}
