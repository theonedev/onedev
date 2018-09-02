package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.onedev.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.utils.Range;
import io.onedev.utils.stringmatch.PatternApplied;
import io.onedev.utils.stringmatch.WildcardUtils;

public class SuggestionUtils {
	
	public static List<InputSuggestion> suggestBranch(Project project, String matchWith, @Nullable String escapeChars) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (RefInfo ref: project.getBranches()) {
			String branch = GitUtils.ref2branch(ref.getRef().getName());
			int index = branch.toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range match = new Range(index, index+lowerCaseMatchWith.length());
				InputSuggestion suggestion = new InputSuggestion(branch, match);
				if (escapeChars != null)
					suggestion = suggestion.escape(escapeChars);
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestTag(Project project, String matchWith, @Nullable String escapeChars) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (RefInfo ref: project.getTags()) {
			String tag = GitUtils.ref2tag(ref.getRef().getName());
			int index = tag.toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range match = new Range(index, index+lowerCaseMatchWith.length());
				InputSuggestion suggestion = new InputSuggestion(tag, match); 
				if (escapeChars != null)
					suggestions.add(suggestion.escape(escapeChars));
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestUser(Project project, ProjectPrivilege privilege, String matchWith, @Nullable String escapeChars) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (UserFacade user: SecurityUtils.getAuthorizedUsers(project.getFacade(), privilege)) {
			String name = user.getName();
			int index = name.toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range match = new Range(index, index+lowerCaseMatchWith.length());
				InputSuggestion suggestion = new InputSuggestion(name, match); 
				if (escapeChars != null)
					suggestion = suggestion.escape(escapeChars);
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestTeam(Project project, String matchWith, @Nullable String escapeChars) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Team team: project.getTeams()) {
			String name = team.getName();
			int index = name.toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1) {
				Range match = new Range(index, index+lowerCaseMatchWith.length());
				InputSuggestion suggestion = new InputSuggestion(name, match);
				if (escapeChars != null)
					suggestion = suggestion.escape(escapeChars);
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestPath(Project project, String matchWith, @Nullable String escapeChars) {
		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		return suggestPath(commitInfoManager.getFiles(project), matchWith, escapeChars);
	}
	
	private static Set<String> getChildren(List<PatternApplied> allApplied, String path) {
		Set<String> children = new HashSet<>();
		for (PatternApplied applied: allApplied) {
			if (applied.getText().startsWith(path)) {
				String suffix = applied.getText().substring(path.length());
				int index = suffix.indexOf('/');
				if (index != -1)
					children.add(suffix.substring(0, index+1));
				else
					children.add(suffix);
			}
		}
		return children;
	}
	
	public static List<InputSuggestion> suggestPath(List<String> files, String matchWith, @Nullable String escapeChars) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		List<PatternApplied> allApplied = new ArrayList<>();
		for (String path: files) {
			PatternApplied applied = WildcardUtils.applyPattern(lowerCaseMatchWith, path, false);
			if (applied != null) 
				allApplied.add(applied);
		}
		allApplied.sort((o1, o2) -> o1.getMatch().getFrom() - o2.getMatch().getFrom());

		Map<String, Set<String>> childrenCache = new HashMap<>();
		Map<String, Range> suggestedInputs = new LinkedHashMap<>();
		for (PatternApplied applied: allApplied) {
			Range match = applied.getMatch();
			String suffix = applied.getText().substring(match.getTo());
			int index = suffix.indexOf('/');
			String suggestedInput = applied.getText().substring(0, match.getTo());
			if (index != -1) {
				suggestedInput += suffix.substring(0, index) + "/";
				while (true) {
					Set<String> children = childrenCache.get(suggestedInput);
					if (children == null) {
						children = getChildren(allApplied, suggestedInput);
						childrenCache.put(suggestedInput, children);
					}
					Preconditions.checkState(!children.isEmpty());
					if (children.size() > 1) {
						break;
					} else {
						String child = children.iterator().next();
						suggestedInput += child;
						if (!suggestedInput.endsWith("/"))
							break;
					}
				}
			} else {
				suggestedInput += suffix;
			}
			suggestedInputs.put(suggestedInput, match);
			if (suggestedInputs.size() == InputAssistBehavior.MAX_SUGGESTIONS)
				break;
		}
		
		for (Map.Entry<String, Range> entry: suggestedInputs.entrySet()) { 
			String text = entry.getKey();
			int caret;
			if (text.endsWith("/"))
				caret = text.length();
			else
				caret = -1;
			InputSuggestion suggestion = new InputSuggestion(text, caret, null, entry.getValue()); 
			if (escapeChars != null)
				suggestion = suggestion.escape(escapeChars);
			suggestions.add(suggestion);
		}
		
		return suggestions;		
	}

}
