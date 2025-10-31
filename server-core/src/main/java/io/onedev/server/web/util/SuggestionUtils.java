package io.onedev.server.web.util;

import static io.onedev.server.web.translation.Translation._T;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.match.PatternApplied;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.JobVariable;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.service.AgentAttributeService;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.BuildMetricService;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.BasePermission;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.ProjectScopedQuery;
import io.onedev.server.util.ScriptContribution;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.xodus.CommitInfoService;

public class SuggestionUtils {
	
	public static List<InputSuggestion> suggest(List<String> candidates, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		for (var candidate: candidates) {
			var match = LinearRange.match(candidate, matchWith);
			if (match != null) 
				suggestions.add(new InputSuggestion(candidate, match));
		}
		
		return sortAndTruncate(suggestions, matchWith);
	}
	
	public static List<InputSuggestion> suggestLabels(String matchWith) {
		var labelNames = OneDev.getInstance(LabelSpecService.class).query().stream()
				.map(it->it.getName())
				.sorted()
				.collect(toList());
		return suggest(labelNames, matchWith);
	}

	public static List<InputSuggestion> suggestPackTypes(String matchWith) {
		List<PackSupport> packSupports = new ArrayList<>(OneDev.getExtensions(PackSupport.class));
		packSupports.sort(comparing(PackSupport::getOrder));
		return suggest(packSupports.stream().map(PackSupport::getPackType).collect(toList()), matchWith);
	}
	
	private static List<InputSuggestion> sortAndTruncate(List<InputSuggestion> suggestions, String matchWith) {
		if (matchWith.length() != 0) 
			suggestions.sort(comparingInt(o -> o.getContent().length()));
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
		List<InputSuggestion> suggestions = new ArrayList<>();
		var scopedQuery = ProjectScopedQuery.of(project, matchWith, ':', null);
		if (scopedQuery != null) {
			if (SecurityUtils.canReadCode(scopedQuery.getProject())) {
				List<String> branchNames = scopedQuery.getProject().getBranchRefs()
						.stream()
						.map(it -> GitUtils.ref2branch(it.getName()))
						.sorted()
						.collect(toList());
				suggestions = SuggestionUtils.suggest(branchNames, scopedQuery.getQuery());
				if (project == null)
					suggestions = prefix(suggestions, scopedQuery.getProject().getPath() + ":");
			}
		}
		if (suggestions.isEmpty() && project == null && matchWith.length() == 0) 
			suggestions.add(new InputSuggestion("path/to/project:mybranch", "An example branch", null));
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestTags(@Nullable Project project, String matchWith) {
		List<InputSuggestion> suggestions = new ArrayList<>();
		var scopedQuery = ProjectScopedQuery.of(project, matchWith, ':', null);
		if (scopedQuery != null) {
			if (SecurityUtils.canReadCode(scopedQuery.getProject())) {
				List<String> tags = scopedQuery.getProject().getTagRefs()
						.stream()
						.sorted()
						.map(it -> GitUtils.ref2tag(it.getName()))
						.collect(toList());
				Collections.reverse(tags);
				suggestions = SuggestionUtils.suggest(tags, scopedQuery.getQuery());
				if (project == null)
					suggestions = prefix(suggestions, scopedQuery.getProject().getPath() + ":");
			}
		}
		if (suggestions.isEmpty() && project == null && matchWith.length() == 0)
			suggestions.add(new InputSuggestion("path/to/project:mytag", "An example tag", null));
		return suggestions;
	}

	public static List<InputSuggestion> suggestRevisions(Project project, String matchWith) {
		List<InputSuggestion> suggestions = new ArrayList<>();
		var scopedQuery = ProjectScopedQuery.of(project, matchWith, ':', null);
		if (SecurityUtils.canReadCode(scopedQuery.getProject())) {
			List<String> branches = scopedQuery.getProject().getBranchRefs()
					.stream()
					.sorted()
					.map(it -> GitUtils.ref2branch(it.getName()))
					.collect(toList());
			Collections.reverse(branches);
			if (scopedQuery.getProject().getDefaultBranch() != null) {
				branches.remove(scopedQuery.getProject().getDefaultBranch());
				branches.add(0, scopedQuery.getProject().getDefaultBranch());
			}

			List<String> tags = scopedQuery.getProject().getTagRefs()
					.stream()
					.sorted()
					.map(it -> GitUtils.ref2tag(it.getName()))
					.collect(toList());
			Collections.reverse(tags);

			List<String> revisions = new ArrayList<>();
			revisions.addAll(branches);
			revisions.addAll(tags);

			suggestions = SuggestionUtils.suggest(revisions, scopedQuery.getQuery());
			if (project == null)
				suggestions = prefix(suggestions, scopedQuery.getProject().getPath() + ":");
		}
		if (suggestions.isEmpty() && project == null && matchWith.length() == 0)
			suggestions.add(new InputSuggestion("path/to/project:branch-or-tag", "An example revision", null));
		return suggestions;
	}

	private static List<InputSuggestion> prefix(List<InputSuggestion> suggestions, String prefix) {
		var prefixedSuggestions = new ArrayList<InputSuggestion>();
		for (var suggestion: suggestions) {
			var match = suggestion.getMatch();
			if (match != null)
				match = new LinearRange(match.getFrom() + prefix.length(), match.getTo() + prefix.length());
			var caret = suggestion.getCaret();
			if (caret != -1)
				caret += prefix.length();
			suggestion = new InputSuggestion(prefix + suggestion.getContent(), caret, suggestion.getDescription(), match);
			prefixedSuggestions.add(suggestion);
		}
		return prefixedSuggestions;
	}

	private static ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	public static List<InputSuggestion> suggestProjectPaths(String matchWith) {
		return suggestProjectPaths(matchWith, new AccessProject());
	}
	
	public static List<InputSuggestion> suggestProjectPaths(String matchWith, BasePermission permission) {
		Collection<Project> projects = SecurityUtils.getAuthorizedProjects(permission);
		ProjectCache cache = getProjectService().cloneCache();
		
		List<String> projectPaths = projects.stream()
				.map(it->cache.get(it.getId()).getPath())
				.sorted()
				.collect(toList());
		return suggest(projectPaths, matchWith);
	}
	
	public static List<InputSuggestion> suggestProjectNames(String matchWith) {
		Collection<Project> projects = SecurityUtils.getAuthorizedProjects(new AccessProject());
		ProjectCache cache = getProjectService().cloneCache();
		
		List<String> projectNames = projects.stream()
				.map(it->cache.get(it.getId()).getName())
				.sorted()
				.collect(toList());
		return suggest(projectNames, matchWith);
	}

	public static List<InputSuggestion> suggestProjectKeys(String matchWith) {
		Collection<Project> projects = SecurityUtils.getAuthorizedProjects(new AccessProject());
		ProjectCache cache = getProjectService().cloneCache();

		var projectKeys = new ArrayList<String>();
		for (var project: projects) {
			var key = cache.get(project.getId()).getKey();
			if (key != null)
				projectKeys.add(key);
		}
		Collections.sort(projectKeys);
		return suggest(projectKeys, matchWith);
	}
	
	public static List<InputSuggestion> suggestAgents(String matchWith) {
		List<String> agentNames = OneDev.getInstance(AgentService.class).query()
				.stream()
				.map(it->it.getName())
				.sorted()
				.collect(toList());
		return suggest(agentNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestVariables(Project project, BuildSpec buildSpec, 
			@Nullable List<ParamSpec> paramSpecs, String matchWith, boolean withBuildVersion, 
			boolean withDynamicVariables, boolean withPauseCommand) {
		String lowerCaseMatchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		Map<String, String> variables = new LinkedHashMap<>();
		for (JobVariable var: JobVariable.values()) {
			if (var != JobVariable.BUILD_VERSION || withBuildVersion)
				variables.put(var.name().toLowerCase(), null);
		}
		if (paramSpecs != null) {
			for (ParamSpec paramSpec: paramSpecs) 
				variables.put(VariableInterpolator.PREFIX_PARAM + paramSpec.getName(), null);
		}
		for (String propertyName: buildSpec.getPropertyMap().keySet())
			variables.put(VariableInterpolator.PREFIX_PROPERTY + propertyName, null);
		for (JobProperty property: project.getHierarchyJobProperties()) {
			if (!buildSpec.getPropertyMap().containsKey(property.getName()))
				variables.put(VariableInterpolator.PREFIX_PROPERTY + property.getName(), null);
		}
		for (JobSecret secret: project.getHierarchyJobSecrets())
			variables.put(VariableInterpolator.PREFIX_SECRET + secret.getName(), null);

		if (withDynamicVariables) {
			var attributeNames = new ArrayList<>(OneDev.getInstance(AgentAttributeService.class).getAttributeNames());
			sort(attributeNames);
			for (String attributeName: attributeNames) 
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
		
		for (GroovyScript script: OneDev.getInstance(SettingService.class).getGroovyScripts())
			variables.put(VariableInterpolator.PREFIX_SCRIPT + script.getName(), null);
		
		for (ScriptContribution contribution: OneDev.getExtensions(ScriptContribution.class)) {
			String varName = VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX 
					+ contribution.getScript().getName();
			if (!variables.containsKey(varName))
				variables.put(varName, null);
		}
		
		if (withPauseCommand)
			variables.put(KubernetesHelper.PAUSE, "Pause execution of the script");
		
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

		UserCache cache = OneDev.getInstance(UserService.class).cloneCache();
		var users = cache.values().stream().filter(it -> !it.isDisabled()).collect(toList());
		users.sort(Comparator.comparing(it -> it.getDisplayName()));
		for (UserFacade user: users) {
			LinearRange match = LinearRange.match(user.getName(), matchWith);
			if (match != null) {
				String description;
				if (!user.getDisplayName().equals(user.getName()))
					description = user.getDisplayName();
				else
					description = null;
				suggestions.add(new InputSuggestion(user.getName(), description, match));
			} else if (user.getFullName() != null) {
				match = LinearRange.match(user.getFullName(), matchWith);
				if (match != null) 
					suggestions.add(new InputSuggestion(user.getName(), user.getFullName(), null));
			}
		}
		
		return sortAndTruncate(suggestions, matchWith);
	}
	
	public static List<InputSuggestion> suggestLinkSpecs(String matchWith) {
		List<String> linkNames = new ArrayList<>();
		List<LinkSpec> linkSpecs = OneDev.getInstance(LinkSpecService.class).queryAndSort();
		for (LinkSpec link: linkSpecs) {
			linkNames.add(link.getName());
			if (link.getOpposite() != null)
				linkNames.add(link.getOpposite().getName());
		}
		return suggest(linkNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestNumber(String matchWith, String suggestDescription) {
		if (matchWith.startsWith("#"))
			matchWith = matchWith.substring(1);
		if (NumberUtils.isDigits(matchWith)) {
			var suggestions = new ArrayList<InputSuggestion>();
			suggestions.add(new InputSuggestion(matchWith, suggestDescription, null));
			return suggestions;
		} else {
			return null;
		}
	}
	
	public static List<InputSuggestion> suggestIssues(@Nullable Project project, String matchWith, int count) {
		List<InputSuggestion> suggestions = new ArrayList<>();
		var scopedQuery = ProjectScopedQuery.of(project, matchWith, '#', '-');
		var subject = SecurityUtils.getSubject();
		if (scopedQuery != null && SecurityUtils.canAccessProject(subject, scopedQuery.getProject())) {
			var projectScope = new ProjectScope(scopedQuery.getProject(), false, false);
			var issueQuery = new IssueQuery(new io.onedev.server.search.entity.issue.FuzzyCriteria(scopedQuery.getQuery()));
			for (var issue : OneDev.getInstance(IssueService.class).query(subject, projectScope, issueQuery, false, 0, count)) {
				var title = Emojis.getInstance().apply(issue.getTitle());
				suggestions.add(new InputSuggestion(issue.getReference().toString(project), title, null));	
			}
		}
		if (suggestions.isEmpty() && matchWith.length() == 0) {
			suggestions.add(new InputSuggestion("PROJECTKEY-100", "An example issue", null));
			suggestions.add(new InputSuggestion("path/to/project#100", "An example issue", null));
			if (project != null)
				suggestions.add(new InputSuggestion("#100", "An example issue", null));				
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestPullRequests(@Nullable Project project, String matchWith, int count) {
		List<InputSuggestion> suggestions = new ArrayList<>();
		var scopedQuery = ProjectScopedQuery.of(project, matchWith, '#', '-');
		if (scopedQuery != null && SecurityUtils.canReadCode(scopedQuery.getProject())) {
			PullRequestService pullRequestService = OneDev.getInstance(PullRequestService.class);
			var requestQuery = new PullRequestQuery(new io.onedev.server.search.entity.pullrequest.FuzzyCriteria(scopedQuery.getQuery()));
			for (var request: pullRequestService.query(SecurityUtils.getSubject(), scopedQuery.getProject(), requestQuery, false, 0, count)) {
				var title = Emojis.getInstance().apply(request.getTitle());
				suggestions.add(new InputSuggestion(request.getReference().toString(project), title, null));
			}
		}
		if (suggestions.isEmpty() && matchWith.length() == 0) {
			suggestions.add(new InputSuggestion("PROJECTKEY-100", "An example pull request", null));
			suggestions.add(new InputSuggestion("path/to/project#100", "An example pull request", null));
			if (project != null)
				suggestions.add(new InputSuggestion("#100", "An example pull request", null));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestBuilds(@Nullable Project project, String matchWith, int count) {
		List<InputSuggestion> suggestions = new ArrayList<>();
		var scopedQuery = ProjectScopedQuery.of(project, matchWith, '#', '-');
		if (scopedQuery != null) {
			var subject = SecurityUtils.getSubject();
			for (Build build: OneDev.getInstance(BuildService.class).query(subject, scopedQuery.getProject(), scopedQuery.getQuery(), count)) {
				String description = build.getJobName();
				if (build.getVersion() != null)
					description += " - " + build.getVersion();
				suggestions.add(new InputSuggestion(build.getReference().toString(project), description, null));
			}
		}
		if (suggestions.isEmpty() && matchWith.length() == 0) {
			suggestions.add(new InputSuggestion("PROJECTKEY-100", "An example build", null));
			suggestions.add(new InputSuggestion("path/to/project#100", "An example build", null));
			if (project != null)
				suggestions.add(new InputSuggestion("#100", "An example build", null));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestGroups(String matchWith) {
		List<String> groupNames = OneDev.getInstance(GroupService.class).query()
				.stream()
				.map(it->it.getName())
				.sorted()
				.collect(toList());
		return suggest(groupNames, matchWith);
	}

	public static List<InputSuggestion> suggestBlobs(Project project, String matchWith) {
		CommitInfoService commitInfoService = OneDev.getInstance(CommitInfoService.class);
		return suggestPathsByPathPattern(commitInfoService.getFiles(project.getId()), matchWith, false);
	}
	
	public static List<InputSuggestion> suggestJobs(Project project, String matchWith) {
		List<String> jobNames = new ArrayList<>(OneDev.getInstance(BuildService.class)
				.getAccessibleJobNames(SecurityUtils.getSubject(), project));
		Collections.sort(jobNames);
		return suggest(jobNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestReports(
			Project project, Class<? extends AbstractEntity> metricClass, String matchWith) {
		Map<String, Collection<String>> accessibleReportNames = OneDev.getInstance(BuildMetricService.class)
				.getAccessibleReportNames(project, metricClass);
		Collection<String> setOfReportNames = new HashSet<>();
		
		for (Map.Entry<String, Collection<String>> entry: accessibleReportNames.entrySet())
			setOfReportNames.addAll(entry.getValue());
		
		List<String> listOfReportNames = new ArrayList<>(setOfReportNames);
		Collections.sort(listOfReportNames);
		
		return suggest(listOfReportNames, matchWith);
	}
	
	public static List<InputSuggestion> suggestBuildVersions(Project project, String matchWith) {
		Collection<String> buildVersions = OneDev.getInstance(BuildService.class).queryVersions(
				SecurityUtils.getSubject(), project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String buildVersion: buildVersions) {
			LinearRange match = LinearRange.match(buildVersion, matchWith);
			suggestions.add(new InputSuggestion(buildVersion, null, match));
		}
		
		return sortAndTruncate(suggestions, matchWith);
	}

	public static List<InputSuggestion> suggestPackProps(Project project, String propName, String matchWith) {
		Collection<String> packProps = OneDev.getInstance(PackService.class).queryProps(
				project, propName, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String packProp: packProps) {
			LinearRange match = LinearRange.match(packProp, matchWith);
			suggestions.add(new InputSuggestion(packProp, null, match));
		}

		return sortAndTruncate(suggestions, matchWith);
	}
	
	public static List<InputSuggestion> suggestIterations(Project project, String matchWith) {
		List<String> iterationNames = project.getHierarchyIterations()
				.stream()
				.map(it->it.getName())
				.sorted()
				.collect(toList());
		return suggest(iterationNames, matchWith);
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
	
	public static List<InputSuggestion> suggestPathsByPathPattern(List<String> paths, String pattern, boolean includeExts) {
		pattern = pattern.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();

		if (includeExts)
			suggestions.addAll(suggestExts(paths, pattern, "**/*."));
		
		List<PatternApplied> allApplied = new ArrayList<>();
		for (String path: paths) {
			PatternApplied applied = WildcardUtils.applyPathPattern(pattern, path, false);
			if (applied != null) 
				allApplied.add(applied);
		}
		allApplied.sort(comparingInt(o -> o.getMatch().getFrom()));
		suggestions.addAll(suggestPaths(allApplied));
		return suggestions;		
	}

	public static List<InputSuggestion> suggestPathsByStringPattern(List<String> paths, String pattern, boolean includeExts) {
		pattern = pattern.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();

		if (includeExts)
			suggestions.addAll(suggestExts(paths, pattern, "*."));		
		List<PatternApplied> allApplied = new ArrayList<>();
		for (String path: paths) {
			PatternApplied applied = WildcardUtils.applyStringPattern(pattern, path, false);
			if (applied != null) 
				allApplied.add(applied);
		}
		allApplied.sort(comparingInt(o -> o.getMatch().getFrom()));
		suggestions.addAll(suggestPaths(allApplied));
		return suggestions;		
	}

	private static List<InputSuggestion> suggestExts(List<String> paths, String pattern, String extPrefix) {
		var exts = new TreeSet<String>();
		for (var path: paths) {
			var ext = StringUtils.substringAfterLast(Paths.get(path).getFileName().toFile().getName(), ".");
			if (ext.length() != 0)
				exts.add(ext);
		}
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (var ext: exts) {
			var suggestContent = extPrefix + ext;
			var index = suggestContent.indexOf(pattern);
			if (index != -1)
				suggestions.add(new InputSuggestion(suggestContent, -1, MessageFormat.format(_T("files with ext \"{0}\""), ext), new LinearRange(index, index + pattern.length())));
		}
		return suggestions;
	}
	
	private static List<InputSuggestion> suggestPaths(List<PatternApplied> patternApplieds) {
		List<InputSuggestion> suggestions = new ArrayList<>();
		Map<String, Set<String>> childrenCache = new HashMap<>();
		Map<String, LinearRange> suggestedInputs = new LinkedHashMap<>();
		for (PatternApplied applied: patternApplieds) {
			LinearRange match = applied.getMatch();
			String suffix = applied.getText().substring(match.getTo());
			int index = suffix.indexOf('/');
			String suggestedInput = applied.getText().substring(0, match.getTo());
			if (index != -1) {
				suggestedInput += suffix.substring(0, index) + "/";
				while (true) {
					Set<String> children = childrenCache.get(suggestedInput);
					if (children == null) {
						children = getChildren(patternApplieds, suggestedInput);
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
		
}
