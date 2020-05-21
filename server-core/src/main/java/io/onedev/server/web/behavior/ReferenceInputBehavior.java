package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.validation.ProjectNameValidator;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

public abstract class ReferenceInputBehavior extends InputAssistBehavior {

	private static final long serialVersionUID = 1L;
	
	private static final int REFERENCE_SUGGESTION_LIMIT = 5;
	
	private static final Pattern REFERENCE_PATTERN = Pattern.compile("(^|\\W+)((pull\\s*request|issue|build)\\s+)?(" + ProjectNameValidator.PATTERN.pattern() + ")?#(\\S*)$", 
			Pattern.CASE_INSENSITIVE);
	
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
	
	private final boolean appendTitle;
	
	public ReferenceInputBehavior(boolean appendTitle) {
		this.appendTitle = appendTitle;
	}
	
	@Override
	protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
		List<InputCompletion> completions = new ArrayList<>();
		String contentBeforeCaret = inputStatus.getContentBeforeCaret();
		Matcher matcher = REFERENCE_PATTERN.matcher(contentBeforeCaret);
		if (!contentBeforeCaret.endsWith("\n") && matcher.find()) {
			String matchWith = matcher.group(6);
			String projectName = matcher.group(4);
			Project project;
			if (projectName != null) 
				project = OneDev.getInstance(ProjectManager.class).find(projectName);
			else 
				project = getProject();
			if (project != null) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				String referenceType = matcher.group(3);
				if (referenceType != null)
					referenceType = SPACE_PATTERN.matcher(referenceType).replaceAll("").toLowerCase();
				else
					referenceType = "";
				switch (referenceType) {
				case "issue":
					suggestions = SuggestionUtils.suggestIssues(project, matchWith, REFERENCE_SUGGESTION_LIMIT*3);
					break;
				case "build":
					suggestions = SuggestionUtils.suggestBuilds(project, matchWith, REFERENCE_SUGGESTION_LIMIT*3);
					break;
				case "pullrequest":
					suggestions = SuggestionUtils.suggestPullRequests(project, matchWith, REFERENCE_SUGGESTION_LIMIT*3);
					break;
				default:
					if (projectName == null) { 
						for (InputSuggestion suggestion: SuggestionUtils.suggestIssues(project, matchWith, REFERENCE_SUGGESTION_LIMIT)) {
							suggestions.add(new InputSuggestion("issue " + suggestion.getContent(), -1, 
									suggestion.getDescription(), null));
						}
						for (InputSuggestion suggestion: SuggestionUtils.suggestPullRequests(project, matchWith, REFERENCE_SUGGESTION_LIMIT)) {
							suggestions.add(new InputSuggestion("pull request " + suggestion.getContent(), -1, 
									suggestion.getDescription(), null));
						}
						for (InputSuggestion suggestion: SuggestionUtils.suggestBuilds(project, matchWith, REFERENCE_SUGGESTION_LIMIT)) {
							suggestions.add(new InputSuggestion("build " + suggestion.getContent(), -1, 
									suggestion.getDescription(), null));
						}
					}
				}
				for (InputSuggestion suggestion: suggestions) {
					int hashIndex = matcher.start(6)-1;
					InputCompletion completion;
					if (appendTitle) {
						String content = contentBeforeCaret.substring(0, hashIndex) + suggestion.getContent() 
								+ " - " + suggestion.getDescription(); 
						completion = new InputCompletion(suggestion.getContent(), 
								content + inputStatus.getContentAfterCaret(), content.length(), 
								suggestion.getDescription(), suggestion.getMatch());
					} else {
						String content = contentBeforeCaret.substring(0, hashIndex) + suggestion.getContent(); 
						completion = new InputCompletion(suggestion.getContent(), 
								content + inputStatus.getContentAfterCaret(), content.length(), 
								suggestion.getDescription(), suggestion.getMatch());
					}
					completions.add(completion);
				}
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
