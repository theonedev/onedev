package com.gitplex.server.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gitplex.codeassist.InputSuggestion;
import com.gitplex.jsymbol.Range;
import com.gitplex.server.GitPlex;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.RefInfo;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.Group;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.stringmatch.PatternApplied;
import com.gitplex.server.util.stringmatch.WildcardUtils;
import com.gitplex.server.web.behavior.inputassist.InputAssistBehavior;
import com.google.common.base.Preconditions;

public class SuggestionUtils {
	
	public static List<InputSuggestion> suggestBranch(Project project, String matchWith) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (RefInfo ref: project.getBranches()) {
			String branch = GitUtils.ref2branch(ref.getRef().getName());
			int index = branch.toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range matchRange = new Range(index, index+lowerCaseMatchWith.length());
				suggestions.add(new InputSuggestion(branch, null, matchRange));
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestTag(Project project, String matchWith) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (RefInfo ref: project.getTags()) {
			String tag = GitUtils.ref2tag(ref.getRef().getName());
			int index = tag.toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range matchRange = new Range(index, index+lowerCaseMatchWith.length());
				suggestions.add(new InputSuggestion(tag, null, matchRange));
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestUser(Project project, ProjectPrivilege privilege, String matchWith) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (User user: SecurityUtils.findUsersCan(project, privilege)) {
			String name = user.getName();
			int index = name.toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range matchRange = new Range(index, index+lowerCaseMatchWith.length());
				suggestions.add(new InputSuggestion(name, matchRange));
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestGroup(String matchWith) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Group group: GitPlex.getInstance(GroupManager.class).findAll()) {
			String name = group.getName();
			int index = name.toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1) {
				Range matchRange = new Range(index, index+lowerCaseMatchWith.length());
				suggestions.add(new InputSuggestion(name, matchRange));
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestPath(Project project, String matchWith) {
		CommitInfoManager commitInfoManager = GitPlex.getInstance(CommitInfoManager.class);
		return suggestPath(commitInfoManager.getFiles(project), matchWith);
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
	
	public static List<InputSuggestion> suggestPath(List<String> files, String matchWith) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		List<PatternApplied> allApplied = new ArrayList<>();
		for (String path: files) {
			PatternApplied applied = WildcardUtils.applyPattern(lowerCaseMatchWith, path, false);
			if (applied != null) 
				allApplied.add(applied);
		}
		allApplied.sort((o1, o2) -> o1.getMatchRange().getFrom() - o2.getMatchRange().getFrom());

		Map<String, Set<String>> childrenCache = new HashMap<>();
		Map<String, Range> suggestedInputs = new LinkedHashMap<>();
		for (PatternApplied applied: allApplied) {
			Range matchRange = applied.getMatchRange();
			String suffix = applied.getText().substring(matchRange.getTo());
			int index = suffix.indexOf('/');
			String suggestedInput = applied.getText().substring(0, matchRange.getTo());
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
			suggestedInputs.put(suggestedInput, matchRange);
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
			suggestions.add(new InputSuggestion(text, caret, true, null, entry.getValue()));
		}
		
		return suggestions;		
	}

}
