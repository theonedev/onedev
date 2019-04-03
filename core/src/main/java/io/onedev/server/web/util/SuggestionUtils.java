package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.Range;
import io.onedev.commons.utils.stringmatch.PatternApplied;
import io.onedev.commons.utils.stringmatch.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cache.CacheManager;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Build;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.facade.ConfigurationFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;

public class SuggestionUtils {
	
	public static List<InputSuggestion> suggest(List<String> candidates, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String candidate: candidates) {
			Range match = Range.match(candidate, matchWith, true, false, false);
			if (match != null) 
				suggestions.add(new InputSuggestion(candidate, null, match));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestBranches(Project project, String matchWith) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (RefInfo ref: project.getBranches()) {
			String branch = GitUtils.ref2branch(ref.getRef().getName());
			int index = branch.toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) 
				suggestions.add(new InputSuggestion(branch, new Range(index, index+matchWith.length())));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestProjects(String matchWith) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		User user = SecurityUtils.getUser();
		for (ProjectFacade project: OneDev.getInstance(ProjectManager.class).getAccessibleProjects(user)) {
			int index = project.getName().toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) 
				suggestions.add(new InputSuggestion(project.getName(), new Range(index, index+matchWith.length())));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestTags(Project project, String matchWith) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (RefInfo ref: project.getTags()) {
			String tag = GitUtils.ref2tag(ref.getRef().getName());
			int index = tag.toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) 
				suggestions.add(new InputSuggestion(tag, new Range(index, index+matchWith.length())));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestUsers(String matchWith) {
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
				suggestions.add(new InputSuggestion(user.getName(), description, match));
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestIssues(Project project, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Issue issue: OneDev.getInstance(IssueManager.class).query(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS))
			suggestions.add(new InputSuggestion("#" + issue.getNumber(), issue.getTitle(), null));
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestBuilds(Project project, String matchWith, boolean withConfiguration) {
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
			suggestions.add(suggestion);
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestUsers(Project project, ProjectPrivilege privilege, String matchWith) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (UserFacade user: SecurityUtils.getAuthorizedUsers(project.getFacade(), privilege)) {
			String name = user.getName();
			int index = name.toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				Range match = new Range(index, index+matchWith.length());
				suggestions.add(new InputSuggestion(name, user.getDisplayName(), match));
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestGroups(String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Group group: OneDev.getInstance(GroupManager.class).query()) {
			String name = group.getName();
			int index = name.toLowerCase().indexOf(matchWith);
			if (index != -1) 
				suggestions.add(new InputSuggestion(name, new Range(index, index+matchWith.length())));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestBlobs(Project project, String matchWith) {
		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		return suggestPaths(commitInfoManager.getFiles(project), matchWith);
	}
	
	public static List<InputSuggestion> suggestConfigurations(Project project, String matchWith) {
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
			if (match != null) 
				suggestions.add(new InputSuggestion(configuration.getName(), null, match));
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
	
	public static List<InputSuggestion> suggestPaths(List<String> files, String matchWith) {
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
			suggestions.add(new InputSuggestion(text, caret, null, entry.getValue()));
		}
		
		return suggestions;		
	}

}
