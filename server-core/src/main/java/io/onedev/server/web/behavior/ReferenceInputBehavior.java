package io.onedev.server.web.behavior;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.model.Project;
import io.onedev.server.validation.validator.ProjectKeyValidator;
import io.onedev.server.validation.validator.ProjectPathValidator;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

public abstract class ReferenceInputBehavior extends InputAssistBehavior {

	private static final long serialVersionUID = 1L;
	
	private static final int REFERENCE_SUGGESTION_LIMIT = 5;
	
	private static final Pattern REFERENCE_PATTERN = Pattern.compile(
 			format("(^|\\W+)((?<type>pull\\s*request|pr|issue|build)\\s+)?(?<query>((%s)?#|(%s)-)\\S*)$", ProjectPathValidator.PATTERN.pattern(), ProjectKeyValidator.PATTERN.pattern()), 
			Pattern.CASE_INSENSITIVE);

	@Override
	protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
		List<InputCompletion> completions = new ArrayList<>();
		String contentBeforeCaret = inputStatus.getContentBeforeCaret();
		Matcher matcher = REFERENCE_PATTERN.matcher(contentBeforeCaret);
		if (!contentBeforeCaret.endsWith("\n") && matcher.find()) {
			String query = matcher.group("query");
			List<InputSuggestion> suggestions = new ArrayList<>();
			String type = matcher.group("type");
			if (type != null)
				type = deleteWhitespace(type);
			else
				type = "";
			switch (type) {
				case "":
				case "issue":
					suggestions = SuggestionUtils.suggestIssues(getProject(), query, REFERENCE_SUGGESTION_LIMIT * 3);
					break;
				case "build":
					suggestions = SuggestionUtils.suggestBuilds(getProject(), query, REFERENCE_SUGGESTION_LIMIT * 3);
					break;
				case "pullrequest":
				case "pr":
					suggestions = SuggestionUtils.suggestPullRequests(getProject(), query, REFERENCE_SUGGESTION_LIMIT * 3);
					break;
			}
			for (InputSuggestion suggestion : suggestions) {
				int index = matcher.start("query");
				String content = contentBeforeCaret.substring(0, index) + suggestion.getContent() + " ";
				var completion = new InputCompletion(suggestion.getContent(),
						content + inputStatus.getContentAfterCaret(), content.length(),
						suggestion.getDescription(), suggestion.getMatch());
				completions.add(completion);
			}
		}
		return completions;
	}

	@Override
	protected List<LinearRange> getErrors(String inputContent) {
		return new ArrayList<>();
	}

	@Override
	protected int getAnchor(String inputContent) {
		return inputContent.lastIndexOf('#');
	}

	protected abstract Project getProject();
}
