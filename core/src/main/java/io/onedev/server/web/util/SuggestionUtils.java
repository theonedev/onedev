package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.facade.ConfigurationFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.utils.Range;
import io.onedev.utils.stringmatch.PatternApplied;
import io.onedev.utils.stringmatch.WildcardUtils;

public class SuggestionUtils {
	
	public static List<InputSuggestion> suggest(List<String> candidates, String matchWith, @Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String candidate: candidates) {
			Range match = Range.match(candidate, matchWith, true, false, true);
			if (match != null) {
				InputSuggestion suggestion = new InputSuggestion(candidate, null, match); 
				if (escapeChars != null)
					suggestion = suggestion.escape(escapeChars);
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestBranch(Project project, String matchWith, @Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (RefInfo ref: project.getBranches()) {
			String branch = GitUtils.ref2branch(ref.getRef().getName());
			int index = branch.toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range match = new Range(index, index+matchWith.length());
				InputSuggestion suggestion = new InputSuggestion(branch, match);
				if (escapeChars != null)
					suggestion = suggestion.escape(escapeChars);
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestTag(Project project, String matchWith, @Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (RefInfo ref: project.getTags()) {
			String tag = GitUtils.ref2tag(ref.getRef().getName());
			int index = tag.toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range match = new Range(index, index+matchWith.length());
				InputSuggestion suggestion = new InputSuggestion(tag, match); 
				if (escapeChars != null)
					suggestions.add(suggestion.escape(escapeChars));
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestUser(String matchWith, @Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (UserFacade user: OneDev.getInstance(CacheManager.class).getUsers().values()) {
			Range match = Range.match(user.getName(), matchWith, true, false, true);
			if (match != null) {
				String description;
				if (!user.getDisplayName().equals(user.getName()))
					description = user.getDisplayName();
				else
					description = null;
				suggestions.add(new InputSuggestion(user.getName(), description, match).escape(escapeChars));
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestIssue(Project project, String matchWith, @Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Issue issue: OneDev.getInstance(IssueManager.class).query(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS))
			suggestions.add(new InputSuggestion("#" + issue.getNumber(), issue.getTitle(), null).escape(escapeChars));
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestBuild(Project project, String matchWith, boolean withConfiguration, @Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Build build: OneDev.getInstance(BuildManager.class).query(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS)) {
			InputSuggestion suggestion;
			if (withConfiguration) {
				if (matchWith.contains(Build.FQN_SEPARATOR)) 
					matchWith = StringUtils.substringAfter(matchWith, Build.FQN_SEPARATOR);
				Range match = Range.match(build.getVersion(), matchWith, true, false, true);
				if (match != null) {
					int offset = build.getConfiguration().getName().length()+1;
					match = new Range(match.getFrom() + offset, match.getTo() + offset);
				}
				suggestion = new InputSuggestion(build.getFQN(), null, match);
			} else {
				Range match = Range.match(build.getVersion(), matchWith, true, false, true);
				suggestion = new InputSuggestion(build.getVersion(), null, match);
			}
			if (escapeChars != null)
				suggestion = suggestion.escape(escapeChars);
			suggestions.add(suggestion);
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestUser(Project project, ProjectPrivilege privilege, String matchWith, 
			@Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (UserFacade user: SecurityUtils.getAuthorizedUsers(project.getFacade(), privilege)) {
			String name = user.getName();
			int index = name.toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range match = new Range(index, index+matchWith.length());
				InputSuggestion suggestion = new InputSuggestion(name, user.getDisplayName(), match); 
				if (escapeChars != null)
					suggestion = suggestion.escape(escapeChars);
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestGroup(String matchWith, @Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Group group: OneDev.getInstance(GroupManager.class).query()) {
			String name = group.getName();
			int index = name.toLowerCase().indexOf(matchWith);
			if (index != -1) {
				Range match = new Range(index, index+matchWith.length());
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
	
	public static List<InputSuggestion> suggestConfiguration(Project project, String matchWith, @Nullable String escapeChars) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		List<ConfigurationFacade> configurations = new ArrayList<>(OneDev.getInstance(CacheManager.class).getConfigurations().values());
		Collections.sort(configurations, new Comparator<ConfigurationFacade>() {

			@Override
			public int compare(ConfigurationFacade o1, ConfigurationFacade o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		for (ConfigurationFacade configuration: configurations) {
			if (matchWith.contains(Build.FQN_SEPARATOR)) 
				matchWith = StringUtils.substringAfter(matchWith, Build.FQN_SEPARATOR);
			Range match = Range.match(configuration.getName(), matchWith, true, false, true);
			if (match != null) {
				InputSuggestion suggestion = new InputSuggestion(configuration.getName(), null, match);
				if (escapeChars != null)
					suggestion = suggestion.escape(escapeChars);
				suggestions.add(suggestion);
			}
		}
		return suggestions;
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
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		List<PatternApplied> allApplied = new ArrayList<>();
		for (String path: files) {
			PatternApplied applied = WildcardUtils.applyPattern(matchWith, path, false);
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
