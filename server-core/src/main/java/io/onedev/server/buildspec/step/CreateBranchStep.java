package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.util.SuggestionUtils;
import org.eclipse.jgit.lib.Repository;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Editable(name="Create Branch", order=280)
public class CreateBranchStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
	
	private String branchName;
	
	private String branchRevision;
	
	private String accessTokenSecret;
	
	@Editable(order=1000, description="Specify name of the branch")
	@Interpolative(variableSuggester="suggestVariables")
	@BranchName
	@NotEmpty
	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	@Editable(order=1100, placeholder = "Build Commit", description="Optionally specify revision " +
			"to create branch from. Leave empty to create from build commit")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestRevisions")
	@NotEmpty
	public String getBranchRevision() {
		return branchRevision;
	}

	public void setBranchRevision(String branchRevision) {
		this.branchRevision = branchRevision;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	private static List<InputSuggestion> suggestRevisions(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestRevisions(project, matchWith);
		else
			return new ArrayList<>();
	}
	
	@Editable(order=1060, description="Specify a secret to be used as access token. This access token " +
			"should have permission to create above branch in the project")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@NotEmpty
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		Project project = build.getProject();
		String branchName = getBranchName();
		
		if (!Repository.isValidRefName(GitUtils.branch2ref(branchName)))
			throw new ExplicitException("Invalid branch name: " + branchName);

		if (build.canCreateBranch(getAccessTokenSecret(), branchName)) {
			RefFacade branchRef = project.getBranchRef(branchName);
			if (branchRef != null) {
				logger.warning("Branch '" + branchName + "' already exists");
			} else {
				String branchRevision = getBranchRevision();
				if (branchRevision == null)
					branchRevision = build.getCommitHash();
				OneDev.getInstance(GitService.class).createBranch(project, branchName, branchRevision);
			}
		} else {
			throw new ExplicitException("This build is not authorized to create branch '" + branchName + "'");
		}
		
		return null;
	}

}
