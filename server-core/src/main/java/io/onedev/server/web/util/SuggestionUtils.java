package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.JobVariable;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.util.match.PatternApplied;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.util.script.ScriptContribution;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;

public class SuggestionUtils {
	
	public static List<InputSuggestion> suggest(List<String> candidates, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		for (String candidate: candidates) {
			LinearRange match = LinearRange.match(candidate, matchWith);
			if (match != null) 
				suggestions.add(new InputSuggestion(candidate, null, match));
		}
		
		return sortAndTruncate(suggestions, matchWith);
	}
	
	private static List<InputSuggestion> sortAndTruncate(List<InputSuggestion> suggestions, String matchWith) {
		if (matchWith.length() != 0) {
			suggestions.sort(new Comparator<InputSuggestion>() {

				@Override
				public int compare(InputSuggestion o1, InputSuggestion o2) {
					return o1.getContent().length() - o2.getContent().length();
				}
				
			});
		}
		if (suggestions.size() > InputAssistBehavior.MAX_SUGGESTIONS)
			return suggestions.subList(0, InputAssistBehavior.MAX_SUGGESTIONS);
		else
			return suggestions;
	}
	
	public static List<InputSuggestion> suggest(Map<String, String> candidates, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Map.Entry<String, String> entry: candidates.entrySet()) {
			LinearRange match = LinearRange.match(entry.getKey(), matchWith);
			if (match != null) 
				suggestions.add(new InputSuggestion(entry.getKey(), entry.getValue(), match));
		}
		return sortAndTruncate(suggestions, matchWith);
	}
	
	public static List<InputSuggestion> suggestBranches(@Nullable Project project, String matchWith) {
		return suggest(project, matchWith, new ProjectScopedSuggester() {
			
			@Override
			public List<InputSuggestion> suggest(Project project, String matchWith) {
				if (SecurityUtils.canReadCode(project)) {
					List<String> branchNames = project.getBranchRefInfos()
							.stream()
							.map(it->GitUtils.ref2branch(it.getRef().getName()))
							.sorted()
							.collect(Collectors.toList());
					return SuggestionUtils.suggest(branchNames, matchWith);
				} else {
					return new ArrayList<>();
				}
			}
			
		}, ":");
	}
	
	public static List<InputSuggestion> suggestTags(@Nullable Project project, String matchWith) {
		return suggest(project, matchWith, new ProjectScopedSuggester() {
			
			@Override
			public List<InputSuggestion> suggest(Project project, String matchWith) {
				if (SecurityUtils.canReadCode(project)) {
					List<String> tagNames = project.getTagRefInfos()
							.stream()
							.map(it->GitUtils.ref2tag(it.getRef().getName()))
							.sorted()
							.collect(Collectors.toList());
					return SuggestionUtils.suggest(tagNames, matchWith);
				} else {
					return new ArrayList<>();
				}
			}
			
		}, ":");
	}
	
	public static List<InputSuggestion> suggestCommits(@Nullable Project project, String matchWith) {
		return suggest(project, matchWith, new ProjectScopedSuggester() {
			
			@Override
			public List<InputSuggestion> suggest(Project project, String matchWith) {
				if (SecurityUtils.canReadCode(project)) 
					return null;
				else 
					return new ArrayList<>();
			}
			
		}, ":");
	}
	
	private static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	public static List<InputSuggestion> suggestProjectPaths(String matchWith) {
		Collection<Project> projects = getProjectManager().getPermittedProjects(new AccessProject());
		ProjectCache cache = getProjectManager().cloneCache();
		
		List<String> projectPaths = projects.stream()
				.map(it->cache.getPath(it.getId()))
				.sorted()
				.collect(Collectors.toList());
		return suggest(projectPaths, matchWith);
	}
	
	public static List<InputSuggestion> suggestProjectNames(String matchWith) {
		Collection<Project> projects = getProjectManager().getPermittedProjects(new AccessProject());
		ProjectCache cache = getProjectManager().cloneCache();
		
		List<String> projectNames = projects.stream()
				.map(it->cache.get(it.getId()).getName())
				.sorted()
				.collect(Collectors.toList());
		return suggest(projectNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestAgents(String matchWith) {
		List<String> agentNames = OneDev.getInstance(AgentManager.class).query()
				.stream()
				.map(it->it.getName())
				.sorted()
				.collect(Collectors.toList());
		return suggest(agentNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestVariables(Project project, BuildSpec buildSpec, 
			@Nullable List<ParamSpec> paramSpecs, String matchWith, boolean withBuildVersion, 
			boolean withDynamicVariables) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		Map<String, String> variables = new LinkedHashMap<>();
		for (JobVariable var: JobVariable.values()) {
			if (var != JobVariable.BUILD_VERSION || withBuildVersion)
				variables.put(var.name().toLowerCase(), null);
		}
		if (paramSpecs != null) {
			for (ParamSpec paramSpec: paramSpecs) 
				variables.put(VariableInterpolator.PREFIX_PARAM + paramSpec.getName(), paramSpec.getDescription());
		}
		for (String propertyName: buildSpec.getPropertyMap().keySet())
			variables.put(VariableInterpolator.PREFIX_PROPERTY + propertyName, null);
		for (JobSecret secret: project.getHierarchyJobSecrets())
			variables.put(VariableInterpolator.PREFIX_SECRET + secret.getName(), null);

		if (withDynamicVariables) {
			for (String attributeName: OneDev.getInstance(AgentAttributeManager.class).getAttributeNames()) 
				variables.put(VariableInterpolator.PREFIX_ATTRIBUTE + attributeName, "Use value of specified agent attribute");
			
			String filePath;
			if (lowerCaseMatchWith.startsWith(VariableInterpolator.PREFIX_FILE))
				filePath = matchWith.substring(VariableInterpolator.PREFIX_FILE.length());
			else
				filePath = "";
			if (filePath.length() == 0)
				filePath = "example.txt";
			variables.put(VariableInterpolator.PREFIX_FILE + filePath, "Use content of specified file");
		}
		
		for (GroovyScript script: OneDev.getInstance(SettingManager.class).getGroovyScripts()) 
			variables.put(VariableInterpolator.PREFIX_SCRIPT + script.getName(), null);
		
		for (ScriptContribution contribution: OneDev.getExtensions(ScriptContribution.class)) {
			String varName = VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX 
					+ contribution.getScript().getName();
			if (!variables.containsKey(varName))
				variables.put(varName, null);
		}
		
		for (Map.Entry<String, String> entry: variables.entrySet()) {
			int index = entry.getKey().toLowerCase().indexOf(lowerCaseMatchWith);
			if (index != -1) {
				suggestions.add(new InputSuggestion(entry.getKey(), entry.getValue(), 
						new LinearRange(index, index+lowerCaseMatchWith.length())));
			}
		}
		
		return sortAndTruncate(suggestions, matchWith);
	}
	
	public static List<InputSuggestion> suggestUsers(String matchWith) {
		List<InputSuggestion> suggestions = new ArrayList<>();

		UserCache cache = OneDev.getInstance(UserManager.class).cloneCache();
		List<UserFacade> users = new ArrayList<>(cache.values());
		users.sort(new Comparator<UserFacade>() {

			@Override
			public int compare(UserFacade o1, UserFacade o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		for (UserFacade user: users) {
			LinearRange match = LinearRange.match(user.getName(), matchWith);
			if (match != null) {
				String description;
				if (!user.getDisplayName().equals(user.getName()))
					description = user.getDisplayName();
				else
					description = null;
				suggestions.add(new InputSuggestion(user.getName(), description, match));
			}
		}
		
		return sortAndTruncate(suggestions, matchWith);
	}
	
	public static List<InputSuggestion> suggestLinkSpecs(String matchWith) {
		List<String> linkNames = new ArrayList<>();
		List<LinkSpec> linkSpecs = OneDev.getInstance(LinkSpecManager.class).queryAndSort();
		for (LinkSpec link: linkSpecs) {
			linkNames.add(link.getName());
			if (link.getOpposite() != null)
				linkNames.add(link.getOpposite().getName());
		}
		return suggest(linkNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestIssues(@Nullable Project project, String matchWith, int count) {
		return suggest(project, matchWith, new ProjectScopedSuggester() {
			
			@Override
			public List<InputSuggestion> suggest(Project project, String matchWith) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				if (SecurityUtils.canAccess(project)) {
					for (Issue issue: OneDev.getInstance(IssueManager.class).query(null, project, matchWith, count)) {
						String title = Emojis.getInstance().apply(issue.getTitle());
						suggestions.add(new InputSuggestion("#" + issue.getNumber(), title, null));
					}
				}				
				return suggestions;
			}
			
		}, "#");
	}
	
	public static List<InputSuggestion> suggestPullRequests(@Nullable Project project, String matchWith, int count) {
		return suggest(project, matchWith, new ProjectScopedSuggester() {
			
			@Override
			public List<InputSuggestion> suggest(Project project, String matchWith) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				if (SecurityUtils.canReadCode(project)) {
					PullRequestManager pullRequestManager = OneDev.getInstance(PullRequestManager.class);
					for (PullRequest request: pullRequestManager.query(project, matchWith, count)) {
						String title = Emojis.getInstance().apply(request.getTitle());
						suggestions.add(new InputSuggestion("#" + request.getNumber(), title, null));
					}
				}
				return suggestions;
			}
			
		}, "#");
	}
	
	public static List<InputSuggestion> suggestBuilds(@Nullable Project project, String matchWith, int count) {
		return suggest(project, matchWith, new ProjectScopedSuggester() {
			
			@Override
			public List<InputSuggestion> suggest(Project project, String matchWith) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				for (Build build: OneDev.getInstance(BuildManager.class)
						.query(project, matchWith, count)) {
					String description;
					if (build.getVersion() != null) 
						description = "(" + build.getVersion() + ") " + build.getJobName();
					else
						description = build.getJobName();
					suggestions.add(new InputSuggestion("#" + build.getNumber(), description, null));
				}
				return suggestions;
			}
			
		}, "#");
	}
	
	public static List<InputSuggestion> suggestGroups(String matchWith) {
		List<String> groupNames = OneDev.getInstance(GroupManager.class).query()
				.stream()
				.map(it->it.getName())
				.sorted()
				.collect(Collectors.toList());
		return suggest(groupNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestBlobs(Project project, String matchWith) {
		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		return suggestByPattern(commitInfoManager.getFiles(project), matchWith);
	}
	
	private static List<InputSuggestion> suggest(@Nullable Project project, String matchWith, 
			ProjectScopedSuggester projectScopedSuggester, String scopeSeparator) {
		if (project == null) {
			if (matchWith.contains(scopeSeparator)) {
				String projectName = StringUtils.substringBefore(matchWith, scopeSeparator);
				matchWith = StringUtils.substringAfter(matchWith, scopeSeparator);
				project = getProjectManager().findByPath(projectName);
				if (project != null) {
					List<InputSuggestion> projectScopedSuggestions = projectScopedSuggester.suggest(project, matchWith);
					if (projectScopedSuggestions != null) {
						List<InputSuggestion> suggestions = new ArrayList<>();
						for (InputSuggestion suggestion: projectScopedSuggestions) {
							LinearRange match = suggestion.getMatch();
							if (suggestion.getContent().startsWith(scopeSeparator)) {
								if (match != null) {
									int length = project.getPath().length();
									match = new LinearRange(match.getFrom() + length, match.getTo() + length);
								}
								suggestions.add(new InputSuggestion(
										project.getPath() + suggestion.getContent(), 
										suggestion.getDescription(), 
										match));
							} else {
								if (match != null) {
									int length = project.getPath().length() + scopeSeparator.length();
									match = new LinearRange(match.getFrom() + length, match.getTo() + length);
								}
								suggestions.add(new InputSuggestion(
										project.getPath() + scopeSeparator + suggestion.getContent(), 
										suggestion.getDescription(), 
										match));
							}
						}
						return suggestions;
					} else {
						return null;
					}
				} else {
					return new ArrayList<>();
				}
			} else {
				List<InputSuggestion> suggestions = new ArrayList<>();
				ProjectCache cache = getProjectManager().cloneCache();
				List<String> projectPaths = getProjectManager().getPermittedProjects(new AccessProject()).stream()
						.map(it->cache.getPath(it.getId()))
						.collect(Collectors.toList());
				Collections.sort(projectPaths);
				
				for (String projectPath: projectPaths) {
					LinearRange match = LinearRange.match(projectPath + scopeSeparator, matchWith);
					if (match != null) {
						suggestions.add(new InputSuggestion(
								projectPath + scopeSeparator, 
								projectPath.length() + scopeSeparator.length(), 
								"select project first", 
								match));
					}
				}
				return sortAndTruncate(suggestions, matchWith);
			}
		} else {
			return projectScopedSuggester.suggest(project, matchWith);
		}
	}
	
	public static List<InputSuggestion> suggestJobs(Project project, String matchWith) {
		List<String> jobNames = new ArrayList<>(OneDev.getInstance(BuildManager.class)
				.getAccessibleJobNames(project));
		Collections.sort(jobNames);
		return suggest(jobNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestReports(
			Project project, Class<? extends AbstractEntity> metricClass, String matchWith) {
		Map<String, Collection<String>> accessibleReportNames = OneDev.getInstance(BuildMetricManager.class)
				.getAccessibleReportNames(project, metricClass);
		Collection<String> setOfReportNames = new HashSet<>();
		
		for (Map.Entry<String, Collection<String>> entry: accessibleReportNames.entrySet())
			setOfReportNames.addAll(entry.getValue());
		
		List<String> listOfReportNames = new ArrayList<>(setOfReportNames);
		Collections.sort(listOfReportNames);
		
		return suggest(listOfReportNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestBuildVersions(Project project, String matchWith) {
		Collection<String> buildVersions = OneDev.getInstance(BuildManager.class).queryVersions(
				project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String buildVersion: buildVersions) {
			LinearRange match = LinearRange.match(buildVersion, matchWith);
			suggestions.add(new InputSuggestion(buildVersion, null, match));
		}
		
		return sortAndTruncate(suggestions, matchWith);
	}
	
	public static List<InputSuggestion> suggestMilestones(Project project, String matchWith) {
		List<String> milestoneNames = project.getHierarchyMilestones()
				.stream()
				.map(it->it.getName())
				.sorted()
				.collect(Collectors.toList());
		return suggest(milestoneNames, matchWith);
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
	
	public static List<InputSuggestion> suggestByPattern(List<String> paths, String pattern) {
		pattern = pattern.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		List<PatternApplied> allApplied = new ArrayList<>();
		for (String path: paths) {
			PatternApplied applied = WildcardUtils.applyPathPattern(pattern, path, false);
			if (applied != null) 
				allApplied.add(applied);
		}
		allApplied.sort((o1, o2) -> o1.getMatch().getFrom() - o2.getMatch().getFrom());

		Map<String, Set<String>> childrenCache = new HashMap<>();
		Map<String, LinearRange> suggestedInputs = new LinkedHashMap<>();
		for (PatternApplied applied: allApplied) {
			LinearRange match = applied.getMatch();
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
		
		for (Map.Entry<String, LinearRange> entry: suggestedInputs.entrySet()) { 
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

	static interface ProjectScopedSuggester {
		
		@Nullable
		List<InputSuggestion> suggest(Project project, String matchWith);
		
	}
	
}
