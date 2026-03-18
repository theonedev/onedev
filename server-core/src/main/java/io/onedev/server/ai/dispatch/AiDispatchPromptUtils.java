package io.onedev.server.ai.dispatch;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Issue;

public class AiDispatchPromptUtils {

	private AiDispatchPromptUtils() {
	}

	public static String buildIssuePrompt(Issue issue, @Nullable String operatorPrompt) {
		return buildIssuePrompt(issue.getReference().toString(issue.getProject()),
				issue.getTitle(), issue.getDescription(), operatorPrompt);
	}

	public static String buildIssuePrompt(String issueReference, String issueTitle,
										  @Nullable String issueDescription, @Nullable String operatorPrompt) {
		var builder = new StringBuilder();
		builder.append("Issue ").append(issueReference).append(": ").append(issueTitle);
		if (StringUtils.isNotBlank(issueDescription))
			builder.append("\n\nIssue description:\n").append(issueDescription.strip());
		if (StringUtils.isNotBlank(operatorPrompt))
			builder.append("\n\nOperator request:\n").append(operatorPrompt.strip());
		return builder.toString();
	}

}
