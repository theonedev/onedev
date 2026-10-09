package io.onedev.server.rest.resource.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.onedev.server.annotation.BranchName;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.CodeIndexingSetting;
import io.onedev.server.model.support.ProjectAiSetting;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.model.support.build.DefaultFixedIssueFilter;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.onedev.server.model.support.wiki.WikiSetting;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;

/** Only settings inherited from the defaults project belong in this API. */
@ClassValidating
public class ProjectDefaults implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private ArrayList<BranchProtection> branchProtections = new ArrayList<>();

	private ArrayList<TagProtection> tagProtections = new ArrayList<>();

	private CodeIndexingSetting codeIndexingSetting = new CodeIndexingSetting();

	private ProjectAiSetting aiSetting = new ProjectAiSetting();

	private ArrayList<WorkspaceSpec> workspaceSpecs = new ArrayList<>();

	private WikiSetting wikiSetting = new WikiSetting();

	private ArrayList<WebHook> webHooks = new ArrayList<>();

	private ArrayList<ContributedProjectSetting> contributedSettings = new ArrayList<>();

	private BuildDefaults buildSetting = new BuildDefaults();

	private IssueDefaults issueSetting = new IssueDefaults();

	private PullRequestDefaults pullRequestSetting = new PullRequestDefaults();

	@NotNull
	public ArrayList<@NotNull @Valid BranchProtection> getBranchProtections() {
		return branchProtections;
	}

	public void setBranchProtections(ArrayList<BranchProtection> branchProtections) {
		this.branchProtections = branchProtections;
	}

	@NotNull
	public ArrayList<@NotNull @Valid TagProtection> getTagProtections() {
		return tagProtections;
	}

	public void setTagProtections(ArrayList<TagProtection> tagProtections) {
		this.tagProtections = tagProtections;
	}

	@Valid
	@NotNull
	public CodeIndexingSetting getCodeIndexingSetting() {
		return codeIndexingSetting;
	}

	public void setCodeIndexingSetting(CodeIndexingSetting codeIndexingSetting) {
		this.codeIndexingSetting = codeIndexingSetting;
	}

	@Valid
	@NotNull
	public ProjectAiSetting getAiSetting() {
		return aiSetting;
	}

	public void setAiSetting(ProjectAiSetting aiSetting) {
		this.aiSetting = aiSetting;
	}

	@NotNull
	public ArrayList<@NotNull @Valid WorkspaceSpec> getWorkspaceSpecs() {
		return workspaceSpecs;
	}

	public void setWorkspaceSpecs(ArrayList<WorkspaceSpec> workspaceSpecs) {
		this.workspaceSpecs = workspaceSpecs;
	}

	@Valid
	@NotNull
	public WikiSetting getWikiSetting() {
		return wikiSetting;
	}

	public void setWikiSetting(WikiSetting wikiSetting) {
		this.wikiSetting = wikiSetting;
	}

	@NotNull
	public ArrayList<@NotNull @Valid WebHook> getWebHooks() {
		return webHooks;
	}

	public void setWebHooks(ArrayList<WebHook> webHooks) {
		this.webHooks = webHooks;
	}

	@NotNull
	public ArrayList<@NotNull @Valid ContributedProjectSetting> getContributedSettings() {
		return contributedSettings;
	}

	public void setContributedSettings(ArrayList<ContributedProjectSetting> contributedSettings) {
		this.contributedSettings = contributedSettings;
	}

	@Valid
	@NotNull
	public BuildDefaults getBuildSetting() {
		return buildSetting;
	}

	public void setBuildSetting(BuildDefaults buildSetting) {
		this.buildSetting = buildSetting;
	}

	@Valid
	@NotNull
	public IssueDefaults getIssueSetting() {
		return issueSetting;
	}

	public void setIssueSetting(IssueDefaults issueSetting) {
		this.issueSetting = issueSetting;
	}

	@Valid
	@NotNull
	public PullRequestDefaults getPullRequestSetting() {
		return pullRequestSetting;
	}

	public void setPullRequestSetting(PullRequestDefaults pullRequestSetting) {
		this.pullRequestSetting = pullRequestSetting;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		var names = new HashSet<String>();
		if (workspaceSpecs != null) {
			for (var spec: workspaceSpecs) {
				if (spec != null && spec.getName() != null && !names.add(spec.getName())) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("Workspace spec names must be unique")
							.addPropertyNode("workspaceSpecs").addConstraintViolation();
					return false;
				}
			}
		}
		return true;
	}

	public void populate(Project project) {
		project.setBranchProtections(getBranchProtections());
		project.setTagProtections(getTagProtections());
		project.setCodeIndexingSetting(getCodeIndexingSetting());
		project.setAiSetting(getAiSetting());
		project.setWorkspaceSpecs(getWorkspaceSpecs());
		project.setWikiSetting(getWikiSetting());
		project.setWebHooks(getWebHooks());
		var settings = new LinkedHashMap<String, ContributedProjectSetting>();
		for (var setting: getContributedSettings())
			settings.put(setting.getClass().getName(), setting);
		project.setContributedSettings(settings);
		getBuildSetting().populate(project.getBuildSetting());
		getIssueSetting().populate(project.getIssueSetting());
		getPullRequestSetting().populate(project.getPullRequestSetting());
	}

	public static ProjectDefaults from(Project project) {
		var defaults = new ProjectDefaults();
		defaults.setBranchProtections(project.getBranchProtections());
		defaults.setTagProtections(project.getTagProtections());
		defaults.setCodeIndexingSetting(project.getCodeIndexingSetting());
		defaults.setAiSetting(project.getAiSetting());
		defaults.setWorkspaceSpecs(project.getWorkspaceSpecs());
		defaults.setWikiSetting(project.getWikiSetting());
		defaults.setWebHooks(project.getWebHooks());
		defaults.getContributedSettings().addAll(project.getContributedSettings().values());
		defaults.setBuildSetting(BuildDefaults.from(project.getBuildSetting()));
		defaults.setIssueSetting(IssueDefaults.from(project.getIssueSetting()));
		defaults.setPullRequestSetting(PullRequestDefaults.from(project.getPullRequestSetting()));
		return defaults;
	}

	public static class BuildDefaults implements Serializable {

		private static final long serialVersionUID = 1L;

		private List<JobProperty> jobProperties = new ArrayList<>();

		private List<JobSecret> jobSecrets = new ArrayList<>();

		private List<BuildPreservation> buildPreservations = new ArrayList<>();

		private List<DefaultFixedIssueFilter> defaultFixedIssueFilters = new ArrayList<>();

		private Integer cachePreserveDays;

		@NotNull
		public List<@NotNull @Valid JobProperty> getJobProperties() {
			return jobProperties;
		}

		public void setJobProperties(List<JobProperty> jobProperties) {
			this.jobProperties = jobProperties;
		}

		@NotNull
		public List<@NotNull @Valid JobSecret> getJobSecrets() {
			return jobSecrets;
		}

		public void setJobSecrets(List<JobSecret> jobSecrets) {
			this.jobSecrets = jobSecrets;
		}

		@NotNull
		public List<@NotNull @Valid BuildPreservation> getBuildPreservations() {
			return buildPreservations;
		}

		public void setBuildPreservations(List<BuildPreservation> buildPreservations) {
			this.buildPreservations = buildPreservations;
		}

		@NotNull
		public List<@NotNull @Valid DefaultFixedIssueFilter> getDefaultFixedIssueFilters() {
			return defaultFixedIssueFilters;
		}

		public void setDefaultFixedIssueFilters(List<DefaultFixedIssueFilter> defaultFixedIssueFilters) {
			this.defaultFixedIssueFilters = defaultFixedIssueFilters;
		}

		public Integer getCachePreserveDays() {
			return cachePreserveDays;
		}

		public void setCachePreserveDays(Integer cachePreserveDays) {
			this.cachePreserveDays = cachePreserveDays;
		}

		private void populate(ProjectBuildSetting setting) {
			setting.setJobProperties(getJobProperties());
			setting.setJobSecrets(getJobSecrets());
			setting.setBuildPreservations(getBuildPreservations());
			setting.setDefaultFixedIssueFilters(getDefaultFixedIssueFilters());
			setting.setCachePreserveDays(getCachePreserveDays());
		}

		private static BuildDefaults from(ProjectBuildSetting setting) {
			var defaults = new BuildDefaults();
			defaults.setJobProperties(setting.getJobProperties());
			defaults.setJobSecrets(setting.getJobSecrets());
			defaults.setBuildPreservations(setting.getBuildPreservations());
			defaults.setDefaultFixedIssueFilters(setting.getDefaultFixedIssueFilters());
			defaults.setCachePreserveDays(setting.getCachePreserveDays());
			return defaults;
		}
	}

	public static class IssueDefaults implements Serializable {

		private static final long serialVersionUID = 1L;

		private String branchPrefix;

		@BranchName
		public String getBranchPrefix() {
			return branchPrefix;
		}

		public void setBranchPrefix(String branchPrefix) {
			this.branchPrefix = branchPrefix;
		}

		private void populate(ProjectIssueSetting setting) {
			setting.setBranchPrefix(getBranchPrefix());
		}

		private static IssueDefaults from(ProjectIssueSetting setting) {
			var defaults = new IssueDefaults();
			defaults.setBranchPrefix(setting.getBranchPrefix());
			return defaults;
		}
	}

	public static class PullRequestDefaults implements Serializable {

		private static final long serialVersionUID = 1L;

		private MergeStrategy defaultMergeStrategy;

		private List<String> defaultAssignees = new ArrayList<>();

		private Boolean deleteSourceBranchAfterMerge;

		public MergeStrategy getDefaultMergeStrategy() {
			return defaultMergeStrategy;
		}

		public void setDefaultMergeStrategy(MergeStrategy defaultMergeStrategy) {
			this.defaultMergeStrategy = defaultMergeStrategy;
		}

		@NotNull
		public List<@NotNull String> getDefaultAssignees() {
			return defaultAssignees;
		}

		public void setDefaultAssignees(List<String> defaultAssignees) {
			this.defaultAssignees = defaultAssignees;
		}

		public Boolean getDeleteSourceBranchAfterMerge() {
			return deleteSourceBranchAfterMerge;
		}

		public void setDeleteSourceBranchAfterMerge(Boolean deleteSourceBranchAfterMerge) {
			this.deleteSourceBranchAfterMerge = deleteSourceBranchAfterMerge;
		}

		private void populate(ProjectPullRequestSetting setting) {
			setting.setDefaultMergeStrategy(getDefaultMergeStrategy());
			setting.setDefaultAssignees(getDefaultAssignees());
			setting.setDeleteSourceBranchAfterMerge(getDeleteSourceBranchAfterMerge());
		}

		private static PullRequestDefaults from(ProjectPullRequestSetting setting) {
			var defaults = new PullRequestDefaults();
			defaults.setDefaultMergeStrategy(setting.getDefaultMergeStrategy());
			defaults.setDefaultAssignees(setting.getDefaultAssignees());
			defaults.setDeleteSourceBranchAfterMerge(setting.getDeleteSourceBranchAfterMerge());
			return defaults;
		}
	}
}
