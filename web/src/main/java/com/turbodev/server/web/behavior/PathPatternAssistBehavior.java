package com.turbodev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.turbodev.codeassist.InputCompletion;
import com.turbodev.codeassist.InputStatus;
import com.turbodev.codeassist.InputSuggestion;
import com.turbodev.utils.Range;
import com.google.common.collect.Lists;
import com.turbodev.server.model.Project;
import com.turbodev.server.web.behavior.inputassist.InputAssistBehavior;
import com.turbodev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class PathPatternAssistBehavior extends InputAssistBehavior {

	private final IModel<Project> projectModel;
	
	public PathPatternAssistBehavior(IModel<Project> projectModel) {
		this.projectModel = projectModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		projectModel.detach();
	}

	@Override
	protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
		List<InputCompletion> completions = new ArrayList<>();
		for (InputSuggestion suggestion: SuggestionUtils.suggestPath(projectModel.getObject(), 
				inputStatus.getContentBeforeCaret().trim())) {
			int caret = suggestion.getCaret();
			if (caret == -1)
				caret = suggestion.getContent().length();
			InputCompletion completion = new InputCompletion(0, inputStatus.getContent().length(), 
					suggestion.getContent(), caret, suggestion.getLabel(), 
					null, suggestion.getMatchRange());
			completions.add(completion);
		}
		return completions;
	}
	
	@Override
	protected List<String> getHints(InputStatus inputStatus) {
		return Lists.newArrayList("Use * to match any part of the path");
	}

	@Override
	protected List<Range> getErrors(String inputContent) {
		return null;
	}
	
	@Override
	protected int getAnchor(String content) {
		for (int i=0; i<content.length(); i++) {
			if (!Character.isWhitespace(content.charAt(i)))
				return i;
		}
		return content.length();
	}
	
}
