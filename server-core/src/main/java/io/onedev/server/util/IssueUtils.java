package io.onedev.server.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unbescape.java.JavaEscape;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.validation.ProjectPathValidator;

public class IssueUtils {

	private static final List<String> ISSUE_FIX_WORDS = Lists.newArrayList(
			"fix", "fixed", "fixes", "fixing", 
			"resolve", "resolved", "resolves", "resolving", 
			"close", "closed", "closes", "closing");
	
    private static final Pattern ISSUE_FIX_PATTERN;
    
    static {
    	StringBuilder builder = new StringBuilder("(^|[\\W|/]+)(");
    	builder.append(StringUtils.join(ISSUE_FIX_WORDS, "|"));
    	builder.append(")\\s+issue\\s+(");
    	builder.append(JavaEscape.unescapeJava(ProjectPathValidator.PATTERN.pattern()));
    	builder.append(")?#(\\d+)(?=$|[\\W|/]+)");
    	ISSUE_FIX_PATTERN = Pattern.compile(builder.toString());
    }
    
	public static Collection<Long> parseFixedIssueNumbers(Project project, String commitMessage) {
		Collection<Long> issueNumbers = new HashSet<>();

		// Skip unmatched commit message quickly 
		boolean fixWordsFound = false;
		String lowerCaseCommitMessage = commitMessage.toLowerCase();
		for (String word: ISSUE_FIX_WORDS) {
			if (lowerCaseCommitMessage.indexOf(word) != -1) {
				fixWordsFound = true;
				break;
			}
		}
		
		if (fixWordsFound 
				&& lowerCaseCommitMessage.contains("#") 
				&& lowerCaseCommitMessage.contains("issue")) {
			Matcher matcher = ISSUE_FIX_PATTERN.matcher(lowerCaseCommitMessage);
			
			while (matcher.find()) {
				String projectPath = matcher.group(3);
				if (projectPath == null || projectPath.equals(project.getPath()))
					issueNumbers.add(Long.parseLong(matcher.group(matcher.groupCount())));
			}
		}
		
		return issueNumbers;
	}
	
}
