package io.onedev.server.model.support.code;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.*;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.usermatch.Anyone;
import io.onedev.server.util.usermatch.UserMatch;
import io.onedev.server.web.util.SuggestionUtils;
import org.eclipse.jgit.lib.ObjectId;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

@Editable
@Horizontal
public class BranchProtection implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Pattern CONVENTIONAL_COMMIT_SUBJECT = Pattern.compile(
			"^((\\p{L}+)(\\((\\p{L}+([\\-/ ]\\p{L}+)*)\\))?!?: [^ ].+)|^(revert \".*\")", 
			UNICODE_CHARACTER_CLASS | CASE_INSENSITIVE);

	private boolean enabled = true;
	
	private String branches;
	
	private String userMatch = new Anyone().toString();
	
	private boolean preventForcedPush = true;
	
	private boolean preventDeletion = true;
	
	private boolean preventCreation = true;
	
	private boolean commitSignatureRequired = false;

	private boolean enforceConventionalCommits;
	
	private List<String> commitTypes = new ArrayList<>();

	private List<String> commitScopes = new ArrayList<>();

	private boolean checkCommitMessageFooter;
	
	private String commitMessageFooterPattern;
	
	private List<String> commitTypesForFooterCheck = new ArrayList<>();
	
	private Integer maxCommitMessageLineLength;
	
	private String reviewRequirement;
	
	private transient ReviewRequirement parsedReviewRequirement;
	
	private List<String> jobNames = new ArrayList<>();
	
	private List<FileProtection> fileProtections = new ArrayList<>();
	
	private boolean requireStrictBuilds;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100, description="Specify space-separated branches to be protected. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude")
	@Patterns(suggester = "suggestBranches", path=true)
	@NotEmpty
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		return SuggestionUtils.suggestBranches(Project.get(), matchWith);
	}
	
	@Editable(order=150, name="Applicable Users", description="Rule will apply only if the user changing the branch matches criteria specified here")
	@io.onedev.server.annotation.UserMatch
	@NotEmpty(message="may not be empty")
	public String getUserMatch() {
		return userMatch;
	}

	public void setUserMatch(String userMatch) {
		this.userMatch = userMatch;
	}

	@Editable(order=200, description="Check this to prevent forced push")
	public boolean isPreventForcedPush() {
		return preventForcedPush;
	}

	public void setPreventForcedPush(boolean preventForcedPush) {
		this.preventForcedPush = preventForcedPush;
	}

	@Editable(order=300, description="Check this to prevent branch deletion")
	public boolean isPreventDeletion() {
		return preventDeletion;
	}

	public void setPreventDeletion(boolean preventDeletion) {
		this.preventDeletion = preventDeletion;
	}

	@Editable(order=350, description="Check this to prevent branch creation")
	public boolean isPreventCreation() {
		return preventCreation;
	}

	public void setPreventCreation(boolean preventCreation) {
		this.preventCreation = preventCreation;
	}

	@Editable(order=360, description="Check this to require valid signature of head commit")
	public boolean isCommitSignatureRequired() {
		return commitSignatureRequired;
	}

	public void setCommitSignatureRequired(boolean commitSignatureRequired) {
		this.commitSignatureRequired = commitSignatureRequired;
	}

	@Editable(order=370, description = "Check this to require <a href='https://www.conventionalcommits.org' target='_blank'>conventional commits</a>")
	public boolean isEnforceConventionalCommits() {
		return enforceConventionalCommits;
	}

	public void setEnforceConventionalCommits(boolean enforceConventionalCommits) {
		this.enforceConventionalCommits = enforceConventionalCommits;
	}

	private static boolean isEnforceConventionalCommitsEnabled() {
		return (boolean) EditContext.get().getInputValue("enforceConventionalCommits");
	}

	@Editable(order=380, placeholder = "Arbitrary type", description = "Optionally specify valid " +
			"types of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary type")
	@ShowCondition("isEnforceConventionalCommitsEnabled")
	public List<String> getCommitTypes() {
		return commitTypes;
	}

	public void setCommitTypes(List<String> commitTypes) {
		this.commitTypes = commitTypes;
	}

	@Editable(order=390, placeholder = "Arbitrary scope", description = "Optionally specify valid " +
			"scopes of conventional commits (hit ENTER to add value). Leave empty to allow arbitrary scope")
	@ShowCondition("isEnforceConventionalCommitsEnabled")
	public List<String> getCommitScopes() {
		return commitScopes;
	}

	public void setCommitScopes(List<String> commitScopes) {
		this.commitScopes = commitScopes;
	}

	@Editable(order=391)
	@ShowCondition("isEnforceConventionalCommitsEnabled")
	public boolean isCheckCommitMessageFooter() {
		return checkCommitMessageFooter;
	}

	public void setCheckCommitMessageFooter(boolean checkCommitMessageFooter) {
		this.checkCommitMessageFooter = checkCommitMessageFooter;
	}

	private static boolean isCheckCommitMessageFooterEnabled() {
		return (boolean) EditContext.get().getInputValue("checkCommitMessageFooter");
	}
	
	@Editable(order=393, description = "A " +
			"<a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java regular expression</a> " +
			"to validate commit message footer")
	@ShowCondition("isCheckCommitMessageFooterEnabled")
	@NotEmpty
	public String getCommitMessageFooterPattern() {
		return commitMessageFooterPattern;
	}

	public void setCommitMessageFooterPattern(String commitMessageFooterPattern) {
		this.commitMessageFooterPattern = commitMessageFooterPattern;
	}

	@Editable(order=394, description = "Optionally specify applicable commit types for commit message footer check (hit ENTER to add value). " +
			"Leave empty to all types")
	@ShowCondition("isCheckCommitMessageFooterEnabled")
	public List<String> getCommitTypesForFooterCheck() {
		return commitTypesForFooterCheck;
	}

	public void setCommitTypesForFooterCheck(List<String> commitTypesForFooterCheck) {
		this.commitTypesForFooterCheck = commitTypesForFooterCheck;
	}

	@Editable(order=395, placeholder = "No limit")
	@Min(40)
	public Integer getMaxCommitMessageLineLength() {
		return maxCommitMessageLineLength;
	}

	public void setMaxCommitMessageLineLength(Integer maxCommitMessageLineLength) {
		this.maxCommitMessageLineLength = maxCommitMessageLineLength;
	}
	
	@Editable(order=400, name="Required Reviewers", placeholder="No one", description="Optionally specify "
			+ "required reviewers for changes of specified branch")
	@io.onedev.server.annotation.ReviewRequirement
	public String getReviewRequirement() {
		return reviewRequirement;
	}

	public void setReviewRequirement(String reviewRequirement) {
		this.reviewRequirement = reviewRequirement;
	}

	public ReviewRequirement getParsedReviewRequirement() {
		if (parsedReviewRequirement == null)
			parsedReviewRequirement = ReviewRequirement.parse(reviewRequirement);
		return parsedReviewRequirement;
	}
	
	public void setParsedReviewRequirement(ReviewRequirement parsedReviewRequirement) {
		this.parsedReviewRequirement = parsedReviewRequirement;
		reviewRequirement = parsedReviewRequirement.toString();
	}
	
	@Editable(order=500, name="Required Builds", placeholder="No any", description="Optionally choose required builds")
	@JobChoice(tagsMode=true)
	public List<String> getJobNames() {
		return jobNames;
	}

	public void setJobNames(List<String> jobNames) {
		this.jobNames = jobNames;
	}
	
	@Editable(order=700, description="Optionally specify path protection rules")
	@Valid
	public List<FileProtection> getFileProtections() {
		return fileProtections;
	}

	public void setFileProtections(List<FileProtection> fileProtections) {
		this.fileProtections = fileProtections;
	}

	@Editable(order=800, name="Require Strict Pull Request Builds", description = "When target branch of a pull request " +
			"has new commits, merge commit of the pull request will be recalculated, and this option tells whether or " +
			"not to accept pull request builds ran on previous merged commit. If enabled, you will need to re-run " +
			"required builds on the new merge commit. This setting takes effect only when required builds are specified")
	public boolean isRequireStrictBuilds() {
		return requireStrictBuilds;
	}

	public void setRequireStrictBuilds(boolean requireStrictBuilds) {
		this.requireStrictBuilds = requireStrictBuilds;
	}

	public FileProtection getFileProtection(String file) {
		Set<String> jobNames = new HashSet<>();
		ReviewRequirement reviewRequirement = ReviewRequirement.parse(null);
		for (FileProtection protection: fileProtections) {
			if (PatternSet.parse(protection.getPaths()).matches(new PathMatcher(), file)) {
				jobNames.addAll(protection.getJobNames());
				reviewRequirement.mergeWith(protection.getParsedReviewRequirement());
			}
		}
		FileProtection protection = new FileProtection();
		protection.setJobNames(new ArrayList<>(jobNames));
		protection.setParsedReviewRequirement(reviewRequirement);
		return protection;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		userMatch = UserMatch.onRenameGroup(userMatch, oldName, newName);
		reviewRequirement = ReviewRequirement.onRenameGroup(reviewRequirement, oldName, newName);
		
		for (FileProtection fileProtection: getFileProtections()) {
			fileProtection.setReviewRequirement(ReviewRequirement.onRenameGroup(
					fileProtection.getReviewRequirement(), oldName, newName));
		}
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingGroup(userMatch, groupName))
			usage.add("applicable users");
		if (ReviewRequirement.isUsingGroup(reviewRequirement, groupName))
			usage.add("required reviewers");

		for (FileProtection protection: getFileProtections()) {
			if (ReviewRequirement.isUsingGroup(protection.getReviewRequirement(), groupName)) {
				usage.add("file protections");
				break;
			}
		}
		return usage.prefix("code: branch protection '" + getBranches() + "'");
	}
	
	public void onRenameUser(String oldName, String newName) {
		userMatch = UserMatch.onRenameUser(userMatch, oldName, newName);
		reviewRequirement = ReviewRequirement.onRenameUser(reviewRequirement, oldName, newName);
		
		for (FileProtection fileProtection: getFileProtections()) {
			fileProtection.setReviewRequirement(ReviewRequirement.onRenameUser(
					fileProtection.getReviewRequirement(), oldName, newName));
		}	
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingUser(userMatch, userName))
			usage.add("applicable users");
		if (ReviewRequirement.isUsingUser(reviewRequirement, userName))
			usage.add("required reviewers");

		for (FileProtection protection: getFileProtections()) {
			if (ReviewRequirement.isUsingUser(protection.getReviewRequirement(), userName)) {
				usage.add("file protections");
				break;
			}
		}
		return usage.prefix("code: branch protection '" + getBranches() + "'");
	}
	
	/**
	 * Check if specified user can modify specified file in specified branch.
	 *
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param file
	 * 			file to be checked
	 * @return
	 * 			result of the check. 
	 */
	public boolean isReviewRequiredForModification(User user, Project project, 
			String branch, @Nullable String file) {
		ReviewRequirement requirement = getParsedReviewRequirement();
		if (!requirement.getUsers().isEmpty() || !requirement.getGroups().isEmpty()) 
			return true;
		
		if (file != null) {
			requirement = getFileProtection(file).getParsedReviewRequirement();
			return !requirement.getUsers().isEmpty() || !requirement.getGroups().isEmpty();
		} 
		
		return false;
	}
	
	public boolean isBuildRequiredForModification(Project project, String branch, @Nullable String file) {
		return !getJobNames().isEmpty() || file != null && !getFileProtection(file).getJobNames().isEmpty();
	}

	/**
	 * Check if specified user can push specified commit to specified ref.
	 *
	 * @param user
	 * 			user to be checked
	 * @param oldObjectId
	 * 			old object id of the ref
	 * @param newObjectId
	 * 			new object id of the ref
	 * @param gitEnvs
	 * 			git environments
	 * @return
	 * 			result of the check
	 */
	public boolean isReviewRequiredForPush(User user, Project project, String branch, ObjectId oldObjectId, 
			ObjectId newObjectId, Map<String, String> gitEnvs) {
		ReviewRequirement requirement = getParsedReviewRequirement();
		if (!requirement.getUsers().isEmpty() || !requirement.getGroups().isEmpty()) 
			return true;
		
		for (String changedFile: getGitService().getChangedFiles(project, oldObjectId, newObjectId, gitEnvs)) {
			requirement = getFileProtection(changedFile).getParsedReviewRequirement();
			if (!requirement.getUsers().isEmpty() || !requirement.getGroups().isEmpty())
				return true;
		}

		return false;
	}

	public BuildRequirement getBuildRequirement(Project project, ObjectId oldObjectId, ObjectId newObjectId,
												Map<String, String> gitEnvs) {
		Collection<String> requiredJobs = new LinkedHashSet<>(getJobNames());
		for (String changedFile: getGitService().getChangedFiles(project, oldObjectId, newObjectId, gitEnvs)) 
			requiredJobs.addAll(getFileProtection(changedFile).getJobNames());
		return new BuildRequirement(requiredJobs, isRequireStrictBuilds());
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	public boolean isBuildRequiredForPush(Project project, ObjectId oldObjectId, ObjectId newObjectId, 
			Map<String, String> gitEnvs) {
		Collection<String> requiredJobs = getBuildRequirement(project, oldObjectId, newObjectId, gitEnvs).getRequiredJobs();

		Collection<Build> builds = OneDev.getInstance(BuildManager.class).query(project, newObjectId, null);
		for (Build build: builds) {
			if (requiredJobs.contains(build.getJobName()) && build.getStatus() != Status.SUCCESSFUL)
				return true;
		}
		requiredJobs.removeAll(builds.stream().map(it->it.getJobName()).collect(Collectors.toSet()));
		return !requiredJobs.isEmpty();			
	}

	@Nullable
	public String checkCommitMessage(String commitMessage, boolean merged) {
		var lines = Splitter.on('\n').trimResults().splitToList(commitMessage);
		if (lines.isEmpty())
			return "Message is empty";
		
		if (!merged && enforceConventionalCommits) {
			var matcher = CONVENTIONAL_COMMIT_SUBJECT.matcher(lines.get(0));
			if (matcher.matches()) {
				if (matcher.group(1) != null) {
					var type = matcher.group(2);
					if (!commitTypes.isEmpty() && !commitTypes.contains(type))
						return "Line 1: Unexpected type '" + type + "': Should be one of [" + Joiner.on(',').join(commitTypes) + "]";
					var scope = matcher.group(4);
					if (scope != null && !commitScopes.isEmpty() && !commitScopes.contains(scope))
						return "Line 1: Unexpected scope '" + scope + "': Should be one of [" + Joiner.on(',').join(commitScopes) + "]";
					if (checkCommitMessageFooter && (commitTypesForFooterCheck.isEmpty() || commitTypesForFooterCheck.contains(type))) {
						var size = lines.size();
						if (size < 3 || lines.get(size-1).length() == 0 
								|| lines.get(size-2).length() != 0 || lines.get(size-3).length() == 0) {
							return "A footer is expected as last line and exactly one blank line should precede the footer";
						} else if (!lines.get(size-1).matches(commitMessageFooterPattern)) {
							return "Unexpected footer format: Should match pattern '" + commitMessageFooterPattern + "'";
						}
					}
				}
			} else {
				return "Line 1: Subject is expected of either a git revert message, or format: <type>[optional (scope)][!]: <description>";
			}
		}
	
		for (int i=1; i<lines.size(); i++) {
			var line = lines.get(i);
			if (line.length() != 0) {
				if (i != 2) 
					return "One and only one blank line is expected between subject and body/footer";
				break;
			}
		}
		
		if (maxCommitMessageLineLength != null) {
			for (int i=0; i<lines.size(); i++) {
				var line = lines.get(i);
				if (line.length() > maxCommitMessageLineLength) 
					return "Line " + (i+1) + ": Length exceeds " + maxCommitMessageLineLength;
			}
		}
		return null;
	} 
	
}
