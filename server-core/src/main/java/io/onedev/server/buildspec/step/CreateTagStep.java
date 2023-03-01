package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Editable(name="Create Tag", order=300)
public class CreateTagStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
	
	private String tagName;
	
	private String tagMessage;
	
	private String accessTokenSecret;
	
	@Editable(order=1000, description="Specify name of the tag")
	@Interpolative(variableSuggester="suggestVariables")
	@TagName
	@NotEmpty
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
	@Editable(order=1050, description="Optionally specify message of the tag")
	@Multiline
	@Interpolative(variableSuggester="suggestVariables")
	public String getTagMessage() {
		return tagMessage;
	}

	public void setTagMessage(String tagMessage) {
		this.tagMessage = tagMessage;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Editable(order=1060, description="Specify a secret to be used as access token. This access token " +
			"should have permission to create above tag in the project")
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
		PersonIdent taggerIdent = OneDev.getInstance(UserManager.class).getSystem().asPerson();
		Project project = build.getProject();
		String tagName = getTagName();
		
		if (!Repository.isValidRefName(GitUtils.tag2ref(tagName)))
			throw new ExplicitException("Invalid tag name: " + tagName);

		// Access token is left empty if we migrate from old version
		if (getAccessTokenSecret() == null)
			throw new ExplicitException("Access token secret not specified");
		
		if (build.canCreateTag(getAccessTokenSecret(), tagName)) {
			RefFacade tagRef = project.getTagRef(tagName);
			if (tagRef != null) 
				OneDev.getInstance(ProjectManager.class).deleteTag(project, tagName);
			OneDev.getInstance(GitService.class).createTag(project, tagName, build.getCommitHash(), 
					taggerIdent, getTagMessage(), false);
		} else {
			throw new ExplicitException("This build is not authorized to create tag '" + tagName + "'");
		}
		
		return null;
	}

}
