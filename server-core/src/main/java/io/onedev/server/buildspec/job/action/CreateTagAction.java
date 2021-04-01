package io.onedev.server.buildspec.job.action;

import java.util.List;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Multiline;

@Editable(name="Create tag", order=400)
public class CreateTagAction extends PostBuildAction {

	private static final long serialVersionUID = 1L;
	
	private String tagName;
	
	private String tagMessage;
	
	@Editable(order=1000, description="Specify name of the tag")
	@Interpolative(variableSuggester="suggestVariables")
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
		return BuildSpec.suggestVariables(matchWith);
	}

	@Override
	public void execute(Build build) {
		PersonIdent tagIdent = OneDev.getInstance(UserManager.class).getSystem().asPerson();
		Project project = build.getProject();
		String tagName = getTagName();

		CreateTagAction instance = new CreateTagAction();
		instance.setTagName(tagName);
		if (project.getBuildSetting().isActionAuthorized(build, instance)) {
			Ref tagRef = project.getTagRef(tagName);
			if (tagRef != null) {
				OneDev.getInstance(ProjectManager.class).deleteTag(project, tagName);
				project.createTag(tagName, build.getCommitHash(), tagIdent, getTagMessage());
			} else {
				project.createTag(tagName, build.getCommitHash(), tagIdent, getTagMessage());
			}
		} else {
			throw new ExplicitException("Creating tag '" + tagName + "' is not allowed in this build");
		}
	}

	@Override
	public String getDescription() {
		return "Create tag";
	}

}
