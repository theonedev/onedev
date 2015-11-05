package com.pmease.commons.wicket.behavior.inputassist;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.assets.caret.CaretResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.component.floating.AlignWithComponent;
import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class InputAssistBehavior extends AbstractDefaultAjaxBehavior {

	private FloatingPanel dropdown;
	
	@Override
	protected void onBind() {
		super.onBind();
		
		Component inputField = getComponent();
		inputField.setOutputMarkupId(true);
	}

	protected abstract InputAssist assist(String input, int cursor);

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

		final List<String> recentInputs = getRecentInputs();
		
		if (!assist.getAssistItems().isEmpty() || !recentInputs.isEmpty()) {
			if (dropdown == null) {
				dropdown = new FloatingPanel(target, new AlignWithComponent(getComponent()), Alignment.bottom(0)) {

					@Override
					protected Component newContent(String id) {
						return new AssistPanel(id, assist.getAssistItems(), recentInputs) {

							@Override
							protected void onSelect(AjaxRequestTarget target, AssistItem assistItem) {
								InputAssistBehavior.this.onSelect(target, assistItem);
							}

							@Override
							protected void onSelect(AjaxRequestTarget target, String recentInput) {
								InputAssistBehavior.this.onSelect(target, recentInput);
							}
							
						};
					}

					@Override
					protected void onClosed(AjaxRequestTarget target) {
						super.onClosed(target);
						dropdown = null;
					}
					
				};
			} else {
				Component content = dropdown.getContent();
				Component newContent = new AssistPanel(content.getId(), assist.getAssistItems(), recentInputs) {

					@Override
					protected void onSelect(AjaxRequestTarget target, AssistItem assistItem) {
						InputAssistBehavior.this.onSelect(target, assistItem);
					}

					@Override
					protected void onSelect(AjaxRequestTarget target, String recentInput) {
						InputAssistBehavior.this.onSelect(target, recentInput);
					}
					
				};
				content.replaceWith(newContent);
				target.add(newContent);
			}
		} else if (dropdown != null) {
			dropdown.close(target);
		}
	}

	protected void onSelect(AjaxRequestTarget target, AssistItem assistItem) {
		
	}
	
	protected void onSelect(AjaxRequestTarget target, String recentInput) {
		
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(CaretResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		
		String script = String.format("pmease.commons.inputassist.init('#%s');", 
				getComponent().getMarkupId(true), 
				getCallbackFunction(explicit("input"), explicit("cursor")));
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract List<String> getRecentInputs();
	
}