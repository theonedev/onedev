package io.onedev.server.web.component.taskbutton;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;
import org.unbescape.html.HtmlEscape;

@SuppressWarnings("serial")
abstract class TaskFeedbackPanel extends Panel {

	private final String title;
	
	private String prevMessages;
	
	public TaskFeedbackPanel(String id, String title) {
		super(id);
		this.title = title;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", title));
		
		add(new AjaxLink<Void>("closeIcon") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		Component closeButton;
		add(closeButton = new AjaxLink<Void>("closeButton") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}

			@Override
			public IModel<?> getBody() {
				return new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (getResult() != null)
							return "Ok";
						else
							return "Cancel";
					}
					
				};
			}
			
		}.add(AttributeModifier.replace("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getResult() != null? "btn btn-primary": "btn btn-secondary";
			}
			
		})));

		Component messagesLabel = new Label("messages", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				List<String> messages = getMessages();
				StringBuilder builder = new StringBuilder();
				for (String message: messages)
					builder.append(HtmlEscape.escapeHtml5(message)).append("<br>");
				return builder.toString();
			}
			
		}) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				String script = String.format("$('#%s').scrollTop($('#%s')[0].scrollHeight);", 
						getMarkupId(), getMarkupId());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

			@Override
			protected void onBeforeRender() {
				prevMessages = getDefaultModelObjectAsString();
				super.onBeforeRender();
			}
			
		}.setEscapeModelStrings(false).setOutputMarkupId(true);
		
		add(messagesLabel);

		Component resultLabel = new Label("result", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getResult();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getResult() != null);
			}
			
		}.setEscapeModelStrings(false).setOutputMarkupPlaceholderTag(true);
		
		add(resultLabel);
		
		add(new AbstractAjaxTimerBehavior(Duration.ONE_SECOND) {

			@Override
			protected void onTimer(AjaxRequestTarget target) {
				if (getResult() != null) {
					target.add(closeButton);
					target.add(resultLabel);
					stop(target);
				}
				if (!messagesLabel.getDefaultModelObjectAsString().equals(prevMessages)) 
					target.add(messagesLabel);
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TaskFeedbackCssResourceReference()));
	}

	protected abstract void onClose(AjaxRequestTarget target);
	
	protected abstract List<String> getMessages();
	
	@Nullable
	protected abstract String getResult();
	
}
