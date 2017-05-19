package com.gitplex.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.gitplex.codeassist.InputCompletion;
import com.gitplex.codeassist.InputStatus;
import com.gitplex.codeassist.InputSuggestion;
import com.gitplex.jsymbol.Range;
import com.gitplex.server.model.Depot;
import com.gitplex.server.web.behavior.inputassist.InputAssistBehavior;
import com.gitplex.server.web.util.SuggestionUtils;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class TagPatternAssistBehavior extends InputAssistBehavior {

	private final IModel<Depot> depotModel;
	
	public TagPatternAssistBehavior(IModel<Depot> depotModel) {
		this.depotModel = depotModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		depotModel.detach();
	}

	@Override
	protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
		List<InputCompletion> completions = new ArrayList<>();
		for (InputSuggestion suggestion: SuggestionUtils.suggestTag(depotModel.getObject(), 
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
		return Lists.newArrayList("Use * to match any part of the tag");
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
