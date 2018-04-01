package io.onedev.server.web.component.link;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class EmailLink extends WebMarkupContainer {

	private final IModel<String> emailModel;
	
	public EmailLink(String id, IModel<String> emailModel) {
		super(id);
		this.emailModel = emailModel;
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		replaceComponentTagBody(markupStream, openTag, emailModel.getObject());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(AttributeAppender.append("href", "mailto:" + emailModel.getObject()));
	}

	@Override
	protected void onDetach() {
		emailModel.detach();
		super.onDetach();
	}
	
}
