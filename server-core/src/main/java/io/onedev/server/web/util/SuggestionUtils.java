package io.onedev.server.web.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.Property;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobVariable;
import io.onedev.server.buildspec.job.VariableInterpolator;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.match.PatternApplied;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.util.script.ScriptContribution;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;

public class SuggestionUtils {
	
	public static List<InputSuggestion> suggest(List<String> candidates, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String candidate: candidates) {
			LinearRange match = LinearRange.match(candidate, matchWith);
			if (match != null) {
				suggestions.add(new InputSuggestion(candidate, null, match));
				if (suggestions.size() >= InputAssistBehavior.MAX_SUGGESTIONS)
					break;
			}
		}
		return suggestions;
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
	
	public static List<InputSuggestion> suggestProjects(String matchWith) {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		List<String> projectNames = projectManager.getPermittedProjects(new AccessProject())
				.stream()
				.map(it->it.getName())
				.sorted()
				.collect(Collectors.toList());
		return suggest(projectNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestVariables(Project project, @Nullable ObjectId commitId, 
			BuildSpec buildSpec, Job job, String matchWith) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		Map<String, String> variables = new LinkedHashMap<>();
		for (JobVariable var: JobVariable.values()) 
			variables.put(var.name().toLowerCase(), null);
		for (ParamSpec paramSpec: job.getParamSpecs()) 
			variables.put(VariableInterpolator.PREFIX_PARAMS + paramSpec.getName(), paramSpec.getDescription());
		for (Property property: buildSpec.getProperties())
			variables.put(VariableInterpolator.PREFIX_PROPERTIES + property.getName(), null);
		for (JobSecret secret: project.getBuildSetting().getJobSecrets())
			variables.put(VariableInterpolator.PREFIX_SECRETS + secret.getName(), null);
		for (GroovyScript script: OneDev.getInstance(SettingManager.class).getGroovyScripts()) 
			variables.put(VariableInterpolator.PREFIX_SCRIPTS + script.getName(), null);
		
		for (ScriptContribution contribution: OneDev.getExtensions(ScriptContribution.class)) {
			String varName = VariableInterpolator.PREFIX_SCRIPTS + GroovyScript.BUILTIN_PREFIX 
					+ contribution.getScript().getName();
			if (!variables.containsKey(varName))
				variables.put(varName, null);
		}
		
		for (Map.Entry<String, String> entry: variables.entrySet()) {
			int index = entry.getKey().toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) {
				suggestions.add(new InputSuggestion(entry.getKey(), entry.getValue(), 
						new LinearRange(index, index+matchWith.length())));
			}
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestUsers(String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();

		EntityCriteria<User> criteria = EntityCriteria.of(User.class);
		criteria.add(Restrictions.or(
				Restrictions.ilike(User.PROP_NAME, "%" + matchWith + "%"), 
				Restrictions.ilike(User.PROP_EMAIL, "%" + matchWith + "%"),
				Restrictions.ilike(User.PROP_FULL_NAME, "%" + matchWith + "%")));
		criteria.add(Restrictions.not(Restrictions.eq("id", User.SYSTEM_ID)));
		
		for (User user: OneDev.getInstance(UserManager.class).query(criteria)) {
			LinearRange match = LinearRange.match(user.getName(), matchWith);
			String description;
			if (!user.getDisplayName().equals(user.getName()))
				description = user.getDisplayName();
			else
				description = null;
			suggestions.add(new InputSuggestion(user.getName(), description, match));
			if (suggestions.size() >= InputAssistBehavior.MAX_SUGGESTIONS)
				break;
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestIssues(@Nullable Project project, String matchWith, int count) {
		return suggest(project, matchWith, new ProjectScopedSuggester() {
			
			@Override
			public List<InputSuggestion> suggest(Project project, String matchWith) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				if (SecurityUtils.canAccess(project)) {
					for (Issue issue: OneDev.getInstance(IssueManager.class).query(project, matchWith, count))
						suggestions.add(new InputSuggestion("#" + issue.getNumber(), issue.getTitle(), null));
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
					for (PullRequest request: pullRequestManager.query(project, matchWith, count))
						suggestions.add(new InputSuggestion("#" + request.getNumber(), request.getTitle(), null));
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
		return suggestPaths(commitInfoManager.getFiles(project), matchWith);
	}
	
	private static List<InputSuggestion> suggest(@Nullable Project project, String matchWith, 
			ProjectScopedSuggester projectScopedSuggester, String scopeSeparator) {
		if (project == null) {
			ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
			if (matchWith.contains(scopeSeparator)) {
				String projectName = StringUtils.substringBefore(matchWith, scopeSeparator);
				matchWith = StringUtils.substringAfter(matchWith, scopeSeparator);
				project = projectManager.find(projectName);
				if (project != null) {
					List<InputSuggestion> projectScopedSuggestions = projectScopedSuggester.suggest(project, matchWith);
					if (projectScopedSuggestions != null) {
						List<InputSuggestion> suggestions = new ArrayList<>();
						for (InputSuggestion suggestion: projectScopedSuggestions) {
							LinearRange match = suggestion.getMatch();
							if (suggestion.getContent().startsWith(scopeSeparator)) {
								if (match != null) {
									int length = project.getName().length();
									match = new LinearRange(match.getFrom() + length, match.getTo() + length);
								}
								suggestions.add(new InputSuggestion(
										project.getName() + suggestion.getContent(), 
										suggestion.getDescription(), 
										match));
							} else {
								if (match != null) {
									int length = project.getName().length() + scopeSeparator.length();
									match = new LinearRange(match.getFrom() + length, match.getTo() + length);
								}
								suggestions.add(new InputSuggestion(
										project.getName() + scopeSeparator + suggestion.getContent(), 
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
				for (Project each: projectManager.getPermittedProjects(new AccessProject())) {
					LinearRange match = LinearRange.match(each.getName() + scopeSeparator, matchWith);
					if (match != null) {
						suggestions.add(new InputSuggestion(
								each.getName() + scopeSeparator, 
								each.getName().length() + scopeSeparator.length(), 
								"select project first", 
								match));
						if (suggestions.size() >= InputAssistBehavior.MAX_SUGGESTIONS)
							break;
					}
				}
				return suggestions;
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
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestMilestones(Project project, String matchWith) {
		List<String> milestoneNames = project.getMilestones()
				.stream()
				.map(it->it.getName())
				.sorted()
				.collect(Collectors.toList());
		return suggest(milestoneNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestArtifacts(Build build, String matchWith) {
		return LockUtils.read(build.getArtifactsLockKey(), new Callable<List<InputSuggestion>>() {

			@Override
			public List<InputSuggestion> call() throws Exception {
				List<String> paths = new ArrayList<>();
				File artifactsDir = build.getArtifactsDir();
				if (artifactsDir.exists()) {
					int baseLen = artifactsDir.getAbsolutePath().length()+1;
					for (File file: FileUtils.listFiles(artifactsDir, Lists.newArrayList("**"), new ArrayList<>())) 
						paths.add(file.getAbsolutePath().substring(baseLen));
				}
				return suggestPaths(paths, matchWith);
			}
			
		});
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
	
	public static List<InputSuggestion> suggestPaths(List<String> paths, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		List<PatternApplied> allApplied = new ArrayList<>();
		for (String path: paths) {
			PatternApplied applied = WildcardUtils.applyPattern(matchWith, path, false);
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
