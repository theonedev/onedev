package com.pmease.commons.wicket.behavior.inputassist;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.assets.caret.CaretResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.component.floating.AlignWithComponent;
import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class InputAssistBehavior extends AbstractDefaultAjaxBehavior {

	private FloatingPanel dropdown;
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		
		attributes.setChannel(new AjaxChannel(AjaxChannel.DEFAULT_NAME, AjaxChannel.Type.DROP));
	}

	@Override
	protected void onBind() {
		super.onBind();
		
		Component inputField = getComponent();
		inputField.setOutputMarkupId(true);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
		String input = params.getParameterValue("input").toString();
		int cursor = params.getParameterValue("cursor").toInt();
		final InputAssist assist = assist(input, cursor);
		
		String errorsJSON;
		try {
			errorsJSON = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(assist.getInputErrors());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		String script = String.format("pmease.commons.inputassist.markErrors('%s', %s);", 
				getComponent().getMarkupId(), errorsJSON);
		target.appendJavaScript(script);

		final List<String> recentInputs = getRecentInputs(input, cursor);
		
		if (!assist.getAssistItems().isEmpty() || !recentInputs.isEmpty()) {
			if (dropdown == null) {
				dropdown = new FloatingPanel(target, new AlignWithComponent(getComponent()), Alignment.bottom(0)) {

					@Override
					protected Component newContent(String id) {
						return new AssistPanel(id, assist.getAssistItems(), recentInputs);
					}

					@Override
					protected void onClosed(AjaxRequestTarget target) {
						super.onClosed(target);
						dropdown = null;
					}
					
				};
				script = String.format("pmease.commons.inputassist.assistOpened('%s', '%s');", 
						getComponent().getMarkupId(), dropdown.getMarkupId());
				target.appendJavaScript(script);
			} else {
				Component content = dropdown.getContent();
				Component newContent = new AssistPanel(content.getId(), assist.getAssistItems(), recentInputs);
				content.replaceWith(newContent);
				target.add(newContent);

				script = String.format("pmease.commons.inputassist.assistUpdated('%s', '%s');", 
						getComponent().getMarkupId(), dropdown.getMarkupId());
				target.appendJavaScript(script);
			}
		} else if (dropdown != null) {
			dropdown.close(target);
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(CaretResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(InputAssistBehavior.class, "input-assist.js")));
		
		String script = String.format("pmease.commons.inputassist.init('%s', %s);", 
				getComponent().getMarkupId(true), 
				getCallbackFunction(explicit("input"), explicit("cursor")));
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract InputAssist assist(String input, int cursor);

	protected abstract List<String> getRecentInputs(String input, int cursor);
	
}