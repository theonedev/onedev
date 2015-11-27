package com.pmease.commons.wicket.behavior.inputassist;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.assets.caret.CaretResourceReference;
import com.pmease.commons.wicket.assets.doneevents.DoneEventsResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.assets.scrollintoview.ScrollIntoViewResourceReference;
import com.pmease.commons.wicket.assets.textareacaretposition.TextareaCaretPositionResourceReference;
import com.pmease.commons.wicket.component.floating.AlignPlacement;
import com.pmease.commons.wicket.component.floating.AlignTarget;
import com.pmease.commons.wicket.component.floating.ComponentTarget;
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

	private int getLine(String content, int charIndex) {
		int line = 0;
		if (charIndex >= content.length())
			charIndex = content.length()-1;
		for (int i=0; i<=charIndex; i++) {
			if (content.charAt(i) == '\n')
				line++;
		}
		return line;
	}
	
	private List<InputError> normalizeErrors(String inputContent, List<InputError> errors) {
		Collections.sort(errors, new Comparator<InputError>() {

			@Override
			public int compare(InputError o1, InputError o2) {
				return o1.getFrom() - o2.getFrom();
			}
			
		});
		List<InputError> joinedErrors = new ArrayList<>();
		InputError currentError = null;
		for (InputError error: errors) {
			if (currentError == null) {
				currentError = error;
			} else if (error.getFrom() <= currentError.getTo()) {
				currentError = new InputError(currentError.getFrom(), Math.max(currentError.getTo(), error.getTo()));
			} else {
				joinedErrors.add(currentError);
				currentError = error;
			}
		}
		if (currentError != null)
			joinedErrors.add(currentError);
		
		List<InputError> normalizedErrors = new ArrayList<>();
		
		List<String> lines = Splitter.on('\n').splitToList(inputContent);
		for (InputError error: joinedErrors) {
			int fromLine = getLine(inputContent, error.getFrom());
			int toLine = getLine(inputContent, error.getTo());
			if (fromLine != toLine) {
				int index = getCharIndex(inputContent, fromLine, lines.get(fromLine).length()-1);
				if (index >= error.getFrom())
					normalizedErrors.add(new InputError(error.getFrom(), index));
				index = getCharIndex(inputContent, toLine, 0);
				if (index <= error.getTo())
					normalizedErrors.add(new InputError(index, error.getTo()));
				for (int i=fromLine+1; i<toLine; i++) {
					String line = lines.get(i);
					if (line.length() != 0) {
						int from = getCharIndex(inputContent, i, 0);
						normalizedErrors.add(new InputError(from, from+line.length()-1));
					}
				}
			} else {
				normalizedErrors.add(error);
			}
		}

		return normalizedErrors;
	}
	
	protected int getCharIndex(String content, int line, int charIndexInLine) {
		List<String> lines = Splitter.on('\n').splitToList(content);
		int index = 0;
		for (int i=0; i<line; i++)
			index += lines.get(i).length()+1;
		return index + charIndexInLine;
	}		

	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
		String inputContent = params.getParameterValue("input").toString();
		Integer inputCaret = params.getParameterValue("caret").toOptionalInteger();

		Preconditions.checkArgument(inputContent.indexOf('\r') == -1);
		
		List<InputError> errors = normalizeErrors(inputContent, getErrors(inputContent));
		String json;
		try {
			json = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(errors);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		String script = String.format("pmease.commons.inputassist.markErrors('%s', %s);", 
				getComponent().getMarkupId(), json);
		target.appendJavaScript(script);
		
		final List<InputSuggestion> suggestions;
		
		long time = System.currentTimeMillis();
		if (inputCaret != null)
			suggestions = getSuggestions(new InputStatus(inputContent, inputCaret));
		else
			suggestions = new ArrayList<>();
		
		System.out.println(System.currentTimeMillis()-time);

		if (!suggestions.isEmpty()) {
			int anchor = getAnchor(inputContent.substring(0, inputCaret));
			if (dropdown == null) {
				dropdown = new FloatingPanel(target, new ComponentTarget(getComponent(), anchor), AlignPlacement.bottom(0)) {

					@Override
					protected Component newContent(String id) {
						return new AssistPanel(id, suggestions);
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
				Component newContent = new AssistPanel(content.getId(), suggestions);
				content.replaceWith(newContent);
				target.add(newContent);

				AlignTarget alignTarget = new ComponentTarget(getComponent(), anchor);
				script = String.format("$('#%s').data('alignment').target=%s;", dropdown.getMarkupId(), alignTarget);
				target.prependJavaScript(script);
				
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

		response.render(JavaScriptHeaderItem.forReference(ScrollIntoViewResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CaretResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(DoneEventsResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(TextareaCaretPositionResourceReference.INSTANCE));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(AssistPanel.class, "input-assist.css")));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(InputAssistBehavior.class, "input-assist.js")));
		
		String script = String.format("pmease.commons.inputassist.init('%s', %s);", 
				getComponent().getMarkupId(true), 
				getCallbackFunction(explicit("input"), explicit("caret")));
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract List<InputSuggestion> getSuggestions(InputStatus inputStatus);
	
	protected abstract List<InputError> getErrors(String inputContent);
	
	protected abstract int getAnchor(String content);
	
}