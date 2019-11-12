package io.onedev.server.web.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.match.PatternApplied;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobVariable;
import io.onedev.server.ci.job.NamedFunction;
import io.onedev.server.ci.job.VariableInterpolator;
import io.onedev.server.ci.job.paramspec.ParamSpec;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Build;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.Secret;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.scriptidentity.ScriptIdentity;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;

public class SuggestionUtils {
	
	public static List<InputSuggestion> suggest(List<String> candidates, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String candidate: candidates) {
			LinearRange match = LinearRange.match(candidate, matchWith, true, false, false);
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
				suggestions.add(new InputSuggestion(branch, new LinearRange(index, index+matchWith.length())));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestProjects(String matchWith) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		User user = SecurityUtils.getUser();
		for (Project project: OneDev.getInstance(ProjectManager.class).getPermittedProjects(user, new AccessProject())) {
			int index = project.getName().toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) 
				suggestions.add(new InputSuggestion(project.getName(), new LinearRange(index, index+matchWith.length())));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestVariables(Project project, @Nullable ObjectId commitId, 
			Job job, String matchWith) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();
		
		Map<String, String> variables = new LinkedHashMap<>();
		for (JobVariable var: JobVariable.values()) 
			variables.put(var.name().toLowerCase(), null);
		for (ParamSpec paramSpec: job.getParamSpecs()) 
			variables.put(VariableInterpolator.PARAMS_PREFIX + paramSpec.getName(), paramSpec.getDescription());
		for (Secret secret: project.getSecrets()) {
			String varName = VariableInterpolator.SECRETS_PREFIX + secret.getName();
			if (!variables.containsKey(varName) 
					&& (commitId != null && secret.isAuthorized(project, commitId) || commitId == null && secret.isAuthorized(project, "master"))) {
				variables.put(varName, null);
			}
		}
		for (GroovyScript script: OneDev.getInstance(SettingManager.class).getGroovyScripts()) {
			String varName = VariableInterpolator.SCRIPTS_PREFIX + script.getName();
			if (!variables.containsKey(varName) && script.isAuthorized(ScriptIdentity.get()))
				variables.put(varName, null);
		}
		for (NamedFunction function: OneDev.getExtensions(NamedFunction.class)) {
			String varName = VariableInterpolator.FUNCTIONS_PREFIX + function.getName();
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
	
	public static List<InputSuggestion> suggestSecrets(Project project, String matchWith) {
		matchWith = matchWith.toLowerCase();
		int numSuggestions = 0;
		List<InputSuggestion> suggestions = new ArrayList<>();

		for (Secret secret: project.getSecrets()) {
			int index = secret.getName().toLowerCase().indexOf(matchWith);
			if (index != -1 && numSuggestions++<InputAssistBehavior.MAX_SUGGESTIONS) 
				suggestions.add(new InputSuggestion(secret.getName(), new LinearRange(index, index+matchWith.length())));
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
				suggestions.add(new InputSuggestion(tag, new LinearRange(index, index+matchWith.length())));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestUsers(String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (User user: OneDev.getInstance(UserManager.class).query()) {
			LinearRange match = LinearRange.match(user.getName(), matchWith, true, false, true);
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
	
	public static List<InputSuggestion> suggestPullRequests(Project project, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (PullRequest request: OneDev.getInstance(PullRequestManager.class).query(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS))
			suggestions.add(new InputSuggestion("#" + request.getNumber(), request.getTitle(), null));
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestBuilds(Project project, String matchWith) {
		matchWith = matchWith.toLowerCase();
		if (matchWith.startsWith("#"))
			matchWith = matchWith.substring(1);
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (Build build: OneDev.getInstance(BuildManager.class).query(project, SecurityUtils.getUser(), matchWith, InputAssistBehavior.MAX_SUGGESTIONS)) {
			InputSuggestion suggestion;
			
			String description;
			if (build.getVersion() != null) 
				description = build.getVersion() + " : " + build.getJobName();
			else
				description = build.getJobName();
			
			suggestion = new InputSuggestion("#" + build.getNumber(), description, null);
			
			suggestions.add(suggestion);
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
				suggestions.add(new InputSuggestion(name, new LinearRange(index, index+matchWith.length())));
		}
		return suggestions;
	}
	
	public static List<InputSuggestion> suggestBlobs(Project project, String matchWith) {
		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		return suggestPaths(commitInfoManager.getFiles(project), matchWith);
	}
	
	public static List<InputSuggestion> suggestJobs(Project project, String matchWith) {
		matchWith = matchWith.toLowerCase();
		List<InputSuggestion> suggestions = new ArrayList<>();
		for (String jobName: project.getJobNames()) {
			LinearRange match = LinearRange.match(jobName, matchWith, true, false, true);
			if (match != null) 
				suggestions.add(new InputSuggestion(jobName, null, match));
		}
		return suggestions;
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

}
