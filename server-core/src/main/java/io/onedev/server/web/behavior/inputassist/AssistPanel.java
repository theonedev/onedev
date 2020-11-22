package io.onedev.server.web.behavior.inputassist;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.OneDev;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;

@SuppressWarnings("serial")
abstract class AssistPanel extends Panel {

	private static final int PAGE_SIZE = 25;
	
	private final Component input;
	
	private final List<String> hints;

	private final List<InputCompletion> suggestions;
	
	public AssistPanel(String id, Component input, List<InputCompletion> suggestions, List<String> hints) {
		super(id);
		this.input = input;
		this.suggestions = suggestions;
		this.hints = hints.stream()
				.map(it->it.replace("$docRoot", OneDev.getInstance().getDocRoot()))
				.collect(Collectors.toList());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer suggestionsContainer = new WebMarkupContainer("suggestions");
		add(suggestionsContainer);
		RepeatingView suggestionsView = new RepeatingView("suggestions");
		suggestionsContainer.add(suggestionsView);
		int count = 0;
		for (InputCompletion suggestion: suggestions) {
			if (++count <= PAGE_SIZE)
				suggestionsView.add(newSuggestionItem(suggestionsView.newChildId(), suggestion));
		}
		
		suggestionsContainer.add(new InfiniteScrollBehavior(PAGE_SIZE) {

			@Override
			protected String getItemSelector() {
				return ">li";
			}

			@Override
			protected void appendMore(AjaxRequestTarget target, int offset, int count) {
				for (int i=offset; i<offset+count; i++) {
					if (i < suggestions.size()) {
						InputCompletion suggestion = suggestions.get(i);
						Component suggestionItem = newSuggestionItem(suggestionsView.newChildId(), suggestion);
						suggestionsView.add(suggestionItem);
						String script = String.format("$('#%s .suggestions').append('<li id=\"%s\"></li>');", 
								AssistPanel.this.getMarkupId(), suggestionItem.getMarkupId());
						target.prependJavaScript(script);
						target.add(suggestionItem);
						script = String.format("$('#%s').click(function() {$('#%s').data('update')($(this));});", 
								suggestionItem.getMarkupId(), input.getMarkupId());
						target.appendJavaScript(script);
					} else {
						break;
					}
				}
			}

		});

		RepeatingView hintsView = new RepeatingView("hints");
		add(hintsView);
		for (String hint: hints) 
			hintsView.add(new Label(hintsView.newChildId(), hint).setEscapeModelStrings(false));
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onClose(AjaxRequestTarget target);
	
	private Component newSuggestionItem(String itemId, InputCompletion suggestion) {
		WebMarkupContainer item = new WebMarkupContainer(itemId);
		LinearRange match = suggestion.getMatch();
		String label = suggestion.getLabel();
		if (match != null) {
			String prefix = StringEscapeUtils.escapeHtml4(label.substring(0, match.getFrom()));
			String suffix = StringEscapeUtils.escapeHtml4(label.substring(match.getTo()));
			String matched = StringEscapeUtils.escapeHtml4(label.substring(match.getFrom(), match.getTo()));
			item.add(new Label("content", prefix + "<b>" + matched + "</b>" + suffix).setEscapeModelStrings(false));
		} else {
			item.add(new Label("content", label));
		}
		
		if (suggestion.getDescription() != null)
			item.add(new Label("description", suggestion.getDescription()));
		else
			item.add(new Label("description"));
		
		String content = suggestion.getContent();
		item.add(AttributeAppender.append("data-content", content));
		item.add(AttributeAppender.append("data-caret", suggestion.getCaret()));
		item.setOutputMarkupId(true);
		return item;
	}

}
