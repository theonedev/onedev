package com.pmease.commons.wicket.behavior.inputassist;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.util.Range;
import com.pmease.commons.wicket.behavior.AbstractPostAjaxBehavior;

@SuppressWarnings("serial")
class AssistPanel extends Panel {

	private final InputAssistBehavior assistBehavior;
	
	private final InputStatus inputStatus;
	
	private List<InputCompletion> suggestions;
	
	private List<String> hints;
	
	private RepeatingView suggestionsView;
	
	private int page = 1;
	
	public AssistPanel(String id, InputAssistBehavior assistBehavior, InputStatus inputStatus, 
			List<InputCompletion> suggestions, List<String> hints) {
		super(id);
		this.assistBehavior = assistBehavior;
		this.inputStatus = inputStatus;
		this.suggestions = suggestions;
		this.hints = hints;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		suggestionsView = new RepeatingView("suggestions");
		add(suggestionsView);
		for (InputCompletion suggestion: suggestions) 
			suggestionsView.add(newSuggestionItem(suggestionsView.newChildId(), suggestion));

		RepeatingView hintsView = new RepeatingView("hints");
		add(hintsView);
		for (String hint: hints) 
			hintsView.add(new Label(hintsView.newChildId(), hint).setEscapeModelStrings(false));
		
		add(new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				List<InputCompletion> suggestions = assistBehavior.getSuggestions(inputStatus, (++page)*InputAssistBehavior.PAGE_SIZE);
				if (suggestions.size() > AssistPanel.this.suggestions.size()) {
					List<InputCompletion> additionalSuggestions = 
							suggestions.subList(AssistPanel.this.suggestions.size(), suggestions.size());
					for (InputCompletion suggestion: additionalSuggestions) {
						Component suggestionItem = newSuggestionItem(suggestionsView.newChildId(), suggestion);
						suggestionsView.add(suggestionItem);
						String script = String.format("$('#%s .suggestions>table>tbody').append('<tr id=\"%s\"></tr>');", 
								AssistPanel.this.getMarkupId(), suggestionItem.getMarkupId());
						target.prependJavaScript(script);
						target.add(suggestionItem);
						script = String.format("$('#%s').click(function() {$('#%s').data('update')($(this));});", 
								suggestionItem.getMarkupId(), assistBehavior.getInputField().getMarkupId());
						target.appendJavaScript(script);
					}
					AssistPanel.this.suggestions = suggestions;
				}
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String script = String.format("pmease.commons.inputassist.initInfiniteScroll('%s', %s);", 
						getMarkupId(true), getCallbackFunction());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
		setOutputMarkupId(true);
	}
	
	private Component newSuggestionItem(String itemId, InputCompletion suggestion) {
		WebMarkupContainer item = new WebMarkupContainer(itemId);
		WebMarkupContainer link = new WebMarkupContainer("link");
		Range matchRange = suggestion.getMatchRange();
		String label = suggestion.getLabel();
		if (matchRange != null) {
			String prefix = StringEscapeUtils.escapeHtml4(label.substring(0, matchRange.getFrom()));
			String suffix = StringEscapeUtils.escapeHtml4(label.substring(matchRange.getTo()));
			String matched = StringEscapeUtils.escapeHtml4(label.substring(matchRange.getFrom(), matchRange.getTo()));
			link.add(new Label("label", prefix + "<b>" + matched + "</b>" + suffix).setEscapeModelStrings(false));
		} else {
			link.add(new Label("label", label));
		}
		item.add(link);
		if (suggestion.getDescription() != null)
			item.add(new Label("description", suggestion.getDescription()));
		else
			item.add(new Label("description").setVisible(false));
		item.add(AttributeAppender.append("data-content", suggestion.complete(inputStatus).getContent()));
		item.add(AttributeAppender.append("data-caret", suggestion.getCaret()));
		item.setOutputMarkupId(true);
		return item;
	}

}
