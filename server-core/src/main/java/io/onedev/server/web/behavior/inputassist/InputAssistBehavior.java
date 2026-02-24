package io.onedev.server.web.behavior.inputassist;

import static io.onedev.server.web.translation.Translation._T;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;
import static org.unbescape.javascript.JavaScriptEscape.escapeJavaScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.RangeUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.AlignTarget;
import io.onedev.server.web.component.floating.Alignment;
import io.onedev.server.web.component.floating.ComponentTarget;
import io.onedev.server.web.component.floating.FloatingPanel;

public abstract class InputAssistBehavior extends AbstractPostAjaxBehavior {

	private static final Logger logger = LoggerFactory.getLogger(InputAssistBehavior.class);	

	public static final int MAX_SUGGESTIONS = 1000;
	
	private FloatingPanel dropdown;
	
	@Override
	protected void onBind() {
		super.onBind();
		
		Component input = getComponent();
		input.add(AttributeAppender.append("class", "input-assist"));
		input.setOutputMarkupId(true);
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
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setChannel(new AjaxChannel("input-assist", AjaxChannel.Type.DROP));
	}

	private List<LinearRange> normalizeErrors(String inputContent, List<LinearRange> errors) {
		List<LinearRange> normalizedErrors = new ArrayList<>();
		
		List<String> lines = Splitter.on('\n').splitToList(inputContent);
		for (LinearRange error: RangeUtils.merge(errors)) {
			int fromLine = getLine(inputContent, error.getFrom());
			int toLine = getLine(inputContent, error.getTo());
			if (fromLine != toLine) {
				int index = getCharIndex(inputContent, fromLine, lines.get(fromLine).length()-1);
				if (index >= error.getFrom())
					normalizedErrors.add(new LinearRange(error.getFrom(), index));
				index = getCharIndex(inputContent, toLine, 0);
				if (index <= error.getTo())
					normalizedErrors.add(new LinearRange(index, error.getTo()));
				for (int i=fromLine+1; i<toLine; i++) {
					String line = lines.get(i);
					if (line.length() != 0) {
						int from = getCharIndex(inputContent, i, 0);
						normalizedErrors.add(new LinearRange(from, from+line.length()-1));
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
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		String type = params.getParameterValue("type").toString();
		if (type.equals("close")) {
			if (dropdown != null)
				dropdown.close();
		} else if (type.equals("translate")) {
			String toTranslate = params.getParameterValue("input").toString();
			String translated;
			try {
				translated = getNaturalLanguageTranslator().translate(toTranslate);
			} catch (Exception e) {
				translated = toTranslate;
				var explicitException = ExceptionUtils.find(e, ExplicitException.class);
				if (explicitException != null) {
					Session.get().error(explicitException.getMessage());
				} else {
					logger.error("Error translating natural language input", e);
					Session.get().error("Error translating natural language input. Check server log for details");
				}
			}
			target.appendJavaScript(
				String.format("onedev.server.inputassist.naturalLanguageTranslated('%s', '%s');", 
				getComponent().getMarkupId(), escapeJavaScript(translated)));
		} else {
			String inputContent = params.getParameterValue("input").toString();
			int inputCaret = params.getParameterValue("caret").toInt();

			Preconditions.checkArgument(inputContent.indexOf('\r') == -1);
			
			List<LinearRange> errors = getErrors(inputContent);
			if (errors == null)
				errors = new ArrayList<>();
			List<LinearRange> normalizedErrors = normalizeErrors(inputContent, errors);
			String json;
			try {
				json = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(normalizedErrors);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			String script = String.format("onedev.server.inputassist.markErrors('%s', %s);", 
					getComponent().getMarkupId(), json);
			target.appendJavaScript(script);
			
			if (inputCaret != -1) {
				InputStatus inputStatus = new InputStatus(inputContent, inputCaret);
				List<InputCompletion> suggestions = new ArrayList<>();				
				ComponentContext.push(new ComponentContext(getComponent()));
				try {
					for (InputCompletion suggestion: getSuggestions(new InputStatus(inputContent, inputCaret))) {
						suggestions.add(suggestion);
						if (suggestions.size() >= MAX_SUGGESTIONS)
							break;
					}
				} finally {
					ComponentContext.pop();
				}
				if (!suggestions.isEmpty()) {
					boolean hasOtherSuggestions = false;
					boolean hasAppendSpaceSuggestions = false;
					if ("assist".equals(params.getParameterValue("event").toString()) 
							&& !inputContent.endsWith(" ") 
							&& inputCaret == inputContent.length()) {
						for (var suggestion: suggestions) {
							if (suggestion.getContent().equals(inputContent + " ") && suggestion.getCaret() == inputContent.length() + 1) 
								hasAppendSpaceSuggestions = true;
							else if (!suggestion.getContent().equals(inputContent) && !isFuzzySuggestion(suggestion)) 
								hasOtherSuggestions = true;
						}
					}
	
					if (hasAppendSpaceSuggestions && !hasOtherSuggestions) {
						target.appendJavaScript(
								String.format("onedev.server.inputassist.appendSpace('%s');", 
								getComponent().getMarkupId()));
					} else {
						addTranslationSuggestion(inputStatus, suggestions);
						showSuggestions(target, inputStatus, suggestions);
					}
				} else if (addTranslationSuggestion(inputStatus, suggestions)) {
					showSuggestions(target, inputStatus, suggestions);
				} else if (dropdown != null) {
					dropdown.close();
				}
			} else if (dropdown != null) {
				dropdown.close();
			}

			onInput(target, inputContent);
		}
	}

	private boolean addTranslationSuggestion(InputStatus status, List<InputCompletion> suggestions) {
		var queryTranslator = getNaturalLanguageTranslator();
		if (queryTranslator != null) {
			var contentBeforeCaret = status.getContentBeforeCaret();
			if (StringUtils.isNotBlank(contentBeforeCaret) && suggestions.stream().noneMatch(it->!isFuzzySuggestion(it))) {
				contentBeforeCaret += "🤖";
				suggestions.add(0, new InputCompletion(contentBeforeCaret, contentBeforeCaret + status.getContentAfterCaret(), contentBeforeCaret.length(), _T("Natural language query via AI"), null));
				return true;
			}
		}
		return false;
	}

	private void showSuggestions(AjaxRequestTarget target, InputStatus status, List<InputCompletion> suggestions) {
		int anchor = getAnchor(status.getContent().substring(0, status.getCaret()));
		if (dropdown == null) {
			dropdown = new FloatingPanel(target, new Alignment(new ComponentTarget(getComponent(), anchor), AlignPlacement.bottom(0))) {

				@Override
				protected Component newContent(String id) {
					return new AssistPanel(id, getComponent(), suggestions, getHints(status)) {

						@Override
						protected void onClose(AjaxRequestTarget target) {
							close();
						}

					};
				}

				@Override
				protected void onClosed() {
					super.onClosed();
					dropdown = null;
				}

			};
			var script = String.format("onedev.server.inputassist.assistOpened('%s', '%s', '%s');",
					getComponent().getMarkupId(), dropdown.getMarkupId(), JavaScriptEscape.escapeJavaScript(status.getContent()));
			target.appendJavaScript(script);
		} else {
			Component content = dropdown.getContent();
			Component newContent = new AssistPanel(content.getId(), getComponent(), suggestions, getHints(status)) {

				@Override
				protected void onClose(AjaxRequestTarget target) {
					close();
				}

			};
			content.replaceWith(newContent);
			target.add(newContent);

			AlignTarget alignTarget = new ComponentTarget(getComponent(), anchor);
			var script = String.format("$('#%s').data('alignment').target=%s;", dropdown.getMarkupId(), alignTarget);
			target.prependJavaScript(script);

			script = String.format("onedev.server.inputassist.assistUpdated('%s', '%s', '%s');",
					getComponent().getMarkupId(), dropdown.getMarkupId(), JavaScriptEscape.escapeJavaScript(status.getContent()));
			target.appendJavaScript(script);
		}		
	}
	
	public void close() {
		if (dropdown != null)
			dropdown.close();
	}
	
	protected void onInput(AjaxRequestTarget target, String content) {
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(new InputAssistResourceReference()));
		
		var translations = new HashMap<String, String>();
		translations.put("activeHelp", _T("<span class='keycap'>Tab</span> or <span class='keycap'>Enter</span> to complete."));
		translations.put("inactiveHelp", _T("<span class='keycap'>Tab</span> to complete."));
		String script;
		try {
			script = String.format("onedev.server.inputassist.onDomReady('%s', %s, %b, %s, %b);", 
					getComponent().getMarkupId(true), 
					getCallbackFunction(explicit("type"), explicit("input"), explicit("caret"), explicit("event")),
					getNaturalLanguageTranslator() != null, AppLoader.getInstance(ObjectMapper.class).writeValueAsString(translations),
					isSelectOnFocus());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected abstract List<InputCompletion> getSuggestions(InputStatus inputStatus);

	protected List<String> getHints(InputStatus inputStatus) {
		return new ArrayList<>();
	}
	
	protected abstract List<LinearRange> getErrors(String inputContent);
	
	/**
	 * Given an input content, anchor is index of the char at which place to display left side 
	 * of the suggestion window. This makes it possible to make the suggestion window moves 
	 * as one types, instead of always staying at a fixed place  
	 *  
	 * @param inputContent
	 * @return
	 */
	protected abstract int getAnchor(String inputContent);

	protected boolean isFuzzySuggestion(InputCompletion suggestion) {
		return false;
	}
	
	@Nullable
	protected NaturalLanguageTranslator getNaturalLanguageTranslator() {
		return null;
	}

	protected boolean isSelectOnFocus() {
		return true;
	}

}