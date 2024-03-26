package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Editable(name="Close Milestone", order=400)
public class CloseMilestoneStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
	
	private String milestoneName;
	
	private String accessTokenSecret;
	
	@Editable(order=1000, description="Specify name of the milestone")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getMilestoneName() {
		return milestoneName;
	}

	public void setMilestoneName(String milestoneName) {
		this.milestoneName = milestoneName;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Editable(order=1060, description="For build commit not reachable from default branch, " +
			"an access token with manage issue permission is required")
	@ChoiceProvider("getAccessTokenSecretChoices")
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
	public Map<String, byte[]> run(Long buildId, File inputDir, TaskLogger logger) {
		OneDev.getInstance(TransactionManager.class).run(() -> {
			var build = OneDev.getInstance(BuildManager.class).load(buildId);
			Project project = build.getProject();
			String milestoneName = getMilestoneName();
			MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
			Milestone milestone = milestoneManager.findInHierarchy(project, milestoneName);
			if (milestone != null) {
				if (build.canCloseMilestone(getAccessTokenSecret())) {
					milestone.setClosed(true);
					milestoneManager.createOrUpdate(milestone);
				} else {
					throw new ExplicitException("This build is not authorized to close milestone '" + milestoneName + "'");
				}
			} else {
				logger.warning("Unable to find milestone '" + milestoneName + "' to close. Ignored.");
			}
		});
		return null;
	}

}
