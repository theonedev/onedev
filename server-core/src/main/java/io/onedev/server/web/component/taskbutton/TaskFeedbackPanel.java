package io.onedev.server.web.component.taskbutton;

import static io.onedev.server.web.translation.Translation._T;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;

abstract class TaskFeedbackPanel extends Panel {

	private final String title;
	
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
							return _T("Ok");
						else
							return _T("Cancel");
					}
					
				};
			}
			
		}.add(AttributeModifier.replace("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getResult() != null? "btn btn-primary": "btn btn-secondary";
			}
			
		})));

		Component resultLabel = new Label("result", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getResult().getFeedback();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getResult() != null);
			}
			
		}.setEscapeModelStrings(false).setOutputMarkupPlaceholderTag(true);
		
		add(resultLabel);
		
		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				if (getResult() != null) {
					target.add(closeButton);
					target.add(resultLabel);
				}
				target.appendJavaScript(
						String.format("onedev.server.taskFeedback.processData('%s', %s, %s);", 
						getMarkupId(), getCallbackFunction(), getData()));
			}

			private String getData() {
				Map<String, Object> data = new HashMap<>();
				data.put("logEntries", getLogEntries());
				data.put("finished", getResult() != null);
				
				try {
					return OneDev.getInstance(ObjectMapper.class).writeValueAsString(data);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("onedev.server.taskFeedback.processData('%s', %s, %s);", 
						getMarkupId(), getCallbackFunction(), getData());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TaskFeedbackResourceReference()));
	}

	protected abstract void onClose(AjaxRequestTarget target);
	
	protected abstract List<JobLogEntryEx> getLogEntries();
	
	@Nullable
	protected abstract TaskResult getResult();
	
}
