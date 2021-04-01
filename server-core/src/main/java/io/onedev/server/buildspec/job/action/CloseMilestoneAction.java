package io.onedev.server.buildspec.job.action;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(name="Close milestone", order=500)
public class CloseMilestoneAction extends PostBuildAction {

	private static final long serialVersionUID = 1L;
	
	private String milestoneName;
	
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
		return BuildSpec.suggestVariables(matchWith);
	}

	@Override
	public void execute(Build build) {
		OneDev.getInstance(TransactionManager.class).run(new Runnable() {

			@Override
			public void run() {
				Project project = build.getProject();
				String milestoneName = getMilestoneName();
				MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
				Milestone milestone = milestoneManager.find(project, milestoneName);
				if (milestone != null) {
					CloseMilestoneAction instance = new CloseMilestoneAction();
					instance.setMilestoneName(getMilestoneName());
					if (project.getBuildSetting().isActionAuthorized(build, instance)) {
						milestone.setClosed(true);
						milestoneManager.save(milestone);
					} else {
						throw new ExplicitException("Closing milestone '" + milestoneName + "' is not allowed in this build");
					}
				} else {
					throw new ExplicitException("Unable to find milestone '" + milestoneName + "'");
				}
			}
			
		});
	}

	@Override
	public String getDescription() {
		return "Close milestone";
	}

}
