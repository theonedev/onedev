package io.onedev.server.web.component.milestone.burndown;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.web.util.WicketUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class BurndownIndicators {

	public static final String ISSUE_COUNT = "Issue Count";

	public static final String REMAINING_TIME = "Remaining Time";

	public static final String ESTIMATED_TIME = "Estimated Time";

	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public static List<String> getChoices(Project project) {
		var choices = new ArrayList<String>();
		if (project.isTimeTracking() && WicketUtils.isSubscriptionActive()) {
			choices.add(REMAINING_TIME);
			choices.add(ESTIMATED_TIME);
		}
		choices.add(ISSUE_COUNT);

		choices.addAll(getIssueSetting().getFieldSpecs().stream()
				.filter(it -> it.getType().equals(InputSpec.INTEGER) || it.getType().equals(InputSpec.WORKING_PERIOD))
				.map(FieldSpec::getName).collect(toList()));

		return choices;
	}

	public static String getDefault(Project project) {
		if (project.isTimeTracking() && WicketUtils.isSubscriptionActive())
			return REMAINING_TIME;
		else
			return ISSUE_COUNT;
	}
	
}
